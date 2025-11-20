package ie.tcd.scss.aichat.service;

import ie.tcd.scss.aichat.dto.Flashcard;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for generating flashcards from study material using OpenAI
 */
@Service
public class FlashcardService {
    
    private final ChatClient chatClient;
    private static final int MAX_TOKENS_PER_CHUNK = 20000; // Safe universal default for all GPT models
    private static final int CHARS_PER_TOKEN = 4; // Rough estimate: 1 token ≈ 4 chars
    
    public FlashcardService(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    /**
     * Split text into chunks to ensure compatibility with all OpenAI models
     * Always chunks input for consistent behavior and quality
     */
    private List<String> splitIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        int maxChars = MAX_TOKENS_PER_CHUNK * CHARS_PER_TOKEN;
        
        // If text is small enough, return as single chunk
        if (text.length() <= maxChars) {
            chunks.add(text);
            return chunks;
        }
        
        // Split into chunks at natural boundaries
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxChars, text.length());
            
            // Try to break at paragraph or sentence boundary
            if (end < text.length()) {
                int lastParagraph = text.lastIndexOf("\n\n", end);
                int lastSentence = text.lastIndexOf(". ", end);
                
                if (lastParagraph > start + (maxChars / 2)) {
                    end = lastParagraph + 2;
                } else if (lastSentence > start + (maxChars / 2)) {
                    end = lastSentence + 2;
                }
            }
            
            chunks.add(text.substring(start, end));
            start = end;
        }
        
        return chunks;
    }
    
    /**
     * Generate flashcards from study material using AI
     * Handles large documents by splitting into chunks
     * 
     * @param studyMaterial The text content to generate flashcards from
     * @param count Number of flashcards to generate (default: 5)
     * @return List of generated flashcards
     */
    public List<Flashcard> generateFlashcards(String studyMaterial, Integer count) {
        // Default to 5 flashcards if count is not specified
        int numberOfCards = (count != null && count > 0) ? count : 5;
        
        // Split text into manageable chunks
        List<String> chunks = splitIntoChunks(studyMaterial);
        List<Flashcard> allFlashcards = new ArrayList<>();
        
        // Calculate how many flashcards per chunk
        int cardsPerChunk = (int) Math.ceil((double) numberOfCards / chunks.size());
        
        System.out.println("Processing " + chunks.size() + " chunk(s) for " + numberOfCards + " flashcards");
        
        // Generate flashcards from each chunk
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            
            // Last chunk gets remaining cards
            int cardsToGenerate = (i == chunks.size() - 1) 
                ? (numberOfCards - allFlashcards.size()) 
                : cardsPerChunk;
            
            if (cardsToGenerate <= 0) {
                break; // Already have enough flashcards
            }
            
            try {
                System.out.println("Generating " + cardsToGenerate + " flashcards from chunk " + (i + 1));
                List<Flashcard> chunkFlashcards = generateFlashcardsForChunk(chunk, cardsToGenerate);
                allFlashcards.addAll(chunkFlashcards);
                
                if (allFlashcards.size() >= numberOfCards) {
                    break;
                }
            } catch (Exception e) {
                System.err.println("Error generating flashcards for chunk " + (i + 1) + ": " + e.getMessage());
                // Continue with next chunk instead of failing completely
            }
        }
        
        // Return only the requested number of flashcards
        return allFlashcards.subList(0, Math.min(numberOfCards, allFlashcards.size()));
    }
    
    /**
     * Generate flashcards from a single chunk of text
     */
    private List<Flashcard> generateFlashcardsForChunk(String studyMaterial, int count) {
        // Build the prompt for AI
        String prompt = buildFlashcardPrompt(studyMaterial, count);
        
        // Call OpenAI API
        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        
        // Parse AI response into flashcard objects
        return parseFlashcards(aiResponse);
    }
    
    /**
     * Build the prompt for OpenAI to generate flashcards
     */
    private String buildFlashcardPrompt(String studyMaterial, int count) {
        return String.format("""
                Generate %d flashcards from the following study material.
                
                Study Material:
                %s
                
                Instructions:
                - Create clear, concise questions that test key concepts
                - Provide accurate, complete answers
                - Focus on the most important information
                - Make questions specific and unambiguous
                
                Format each flashcard EXACTLY like this:
                Q: [Your question here]
                A: [Your answer here]
                
                Generate %d flashcards now:
                """, count, studyMaterial, count);
    }
    
    /**
     * Parse AI response into list of Flashcard objects
     * Expects format: "Q: question text\nA: answer text"
     */
    private List<Flashcard> parseFlashcards(String aiResponse) {
        List<Flashcard> flashcards = new ArrayList<>();
        
        // Pattern to match Q: ... A: ... format
        Pattern pattern = Pattern.compile("Q:\\s*(.+?)\\s*A:\\s*(.+?)(?=Q:|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(aiResponse);
        
        while (matcher.find()) {
            String question = matcher.group(1).trim();
            String answer = matcher.group(2).trim();
            flashcards.add(new Flashcard(question, answer));
        }
        
        // Fallback: if regex parsing fails, try simple split approach
        if (flashcards.isEmpty()) {
            flashcards = parseFlashcardsSimple(aiResponse);
        }
        
        return flashcards;
    }
    
    /**
     * Simple fallback parser for flashcards
     */
    private List<Flashcard> parseFlashcardsSimple(String aiResponse) {
        List<Flashcard> flashcards = new ArrayList<>();
        String[] lines = aiResponse.split("\n");
        
        String currentQuestion = null;
        
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Q:")) {
                currentQuestion = line.substring(2).trim();
            } else if (line.startsWith("A:") && currentQuestion != null) {
                String answer = line.substring(2).trim();
                flashcards.add(new Flashcard(currentQuestion, answer));
                currentQuestion = null;
            }
        }
        
        return flashcards;
    }
}