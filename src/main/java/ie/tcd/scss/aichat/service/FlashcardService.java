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
    
    public FlashcardService(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }
    
    /**
     * Generate flashcards from study material using AI
     * 
     * @param studyMaterial The text content to generate flashcards from
     * @param count Number of flashcards to generate (default: 5)
     * @return List of generated flashcards
     */
    public List<Flashcard> generateFlashcards(String studyMaterial, Integer count) {
        // Default to 5 flashcards if count is not specified
        int numberOfCards = (count != null && count > 0) ? count : 5;
        
        // Build the prompt for AI
        String prompt = buildFlashcardPrompt(studyMaterial, numberOfCards);
        
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
                Generate UP TO %d flashcards from the following study material.
                
                Study Material:
                %s
                
                Instructions:
                - **CRITICAL: Only use information from the provided study material above**
                - **DO NOT use outside knowledge or general topics**
                - **CRITICAL: DO NOT generate repetitive or duplicate flashcards**
                - **CRITICAL: If you can only generate fewer than %d UNIQUE flashcards from the material, generate only as many as you can without repetition**
                - **Quality over quantity - it's better to generate 2 unique flashcards than 8 repetitive ones**
                - Create clear, concise questions that test key concepts FROM THE MATERIAL
                - Provide accurate, complete answers BASED ONLY ON THE MATERIAL
                - Focus on the most important information IN THE PROVIDED TEXT
                - Make questions specific and unambiguous
                - Each flashcard should test a DIFFERENT concept or piece of information
                - If the study material doesn't contain educational content, return an error message
                
                Format each flashcard EXACTLY like this:
                Q: [Your question here]
                A: [Your answer here]
                
                Generate UP TO %d unique, non-repetitive flashcards now:
                """, count, studyMaterial, count, count);
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