package ie.tcd.scss.aichat.service;

import ie.tcd.scss.aichat.dto.Flashcard;
import ie.tcd.scss.aichat.model.FlashcardSet;
import ie.tcd.scss.aichat.model.User;
import ie.tcd.scss.aichat.repository.FlashcardRepository;
import ie.tcd.scss.aichat.repository.FlashcardSetRepository;
import ie.tcd.scss.aichat.repository.UserRepository;
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
    private final FlashcardRepository flashcardRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    private final UserRepository userRepository;

    public FlashcardService(ChatModel chatModel, FlashcardRepository flashcardRepository, 
                          FlashcardSetRepository flashcardSetRepository, UserRepository userRepository) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.flashcardRepository = flashcardRepository;
        this.flashcardSetRepository = flashcardSetRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Generate flashcards from study material using AI
     * 
     * @param studyMaterial The text content to generate flashcards from
     * @param count Number of flashcards to generate (default: 5)
     * @param userId The ID of the user creating the flashcards
     * @param title The title for the flashcard set
     * @return List of generated flashcards
     */
    public List<Flashcard> generateFlashcards(String studyMaterial, Integer count, Long userId, String title) {
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
        List<Flashcard> flashcards = parseFlashcards(aiResponse);
        
        // Save flashcards to database
        saveFlashcardsToDatabase(flashcards, studyMaterial, userId, title);
        
        return flashcards;
    }
    
    /**
     * Save generated flashcards to database
     */
    private void saveFlashcardsToDatabase(List<Flashcard> flashcardDTOs, String studyMaterial, Long userId, String title) {
        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Create FlashcardSet
        FlashcardSet flashcardSet = new FlashcardSet();
        flashcardSet.setUser(user);
        flashcardSet.setTitle(title != null ? title : "AI Generated Flashcards");
        flashcardSet.setStudyMaterial(studyMaterial);
        
        // Save FlashcardSet first to get ID
        FlashcardSet savedSet = flashcardSetRepository.save(flashcardSet);
        
        // Create and save individual flashcards
        for (int i = 0; i < flashcardDTOs.size(); i++) {
            Flashcard dto = flashcardDTOs.get(i);
            ie.tcd.scss.aichat.model.Flashcard entity = new ie.tcd.scss.aichat.model.Flashcard();
            entity.setFlashcardSet(savedSet);
            entity.setQuestion(dto.getQuestion());
            entity.setAnswer(dto.getAnswer());
            entity.setPosition(i);
            flashcardRepository.save(entity);
        }
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