package ie.tcd.scss.aichat.service;

import ie.tcd.scss.aichat.dto.QuizQuestion;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for generating multiple-choice quiz questions from study material using OpenAI
 */
@Service
public class QuizService {
    
    private final ChatClient chatClient;
    
    public QuizService(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }
    
    /**
     * Generate multiple-choice quiz questions from study material using AI
     * 
     * @param studyMaterial The text content to generate quiz from
     * @param count Number of questions to generate (default: 5)
     * @param difficulty Difficulty level: "easy", "medium", or "hard" (default: "medium")
     * @return List of generated quiz questions
     */
    public List<QuizQuestion> generateQuiz(String studyMaterial, Integer count, String difficulty) {
        // Default to 5 questions if count is not specified
        int numberOfQuestions = (count != null && count > 0) ? count : 5;
        
        // Default to medium difficulty if not specified
        String difficultyLevel = (difficulty != null) ? difficulty : "medium";
        
        // Build the prompt for AI
        String prompt = buildQuizPrompt(studyMaterial, numberOfQuestions, difficultyLevel);
        
        // Call OpenAI API
        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        
        // Parse AI response into quiz question objects
        return parseQuizQuestions(aiResponse);
    }
    
    /**
     * Build the prompt for OpenAI to generate quiz questions
     */
    private String buildQuizPrompt(String studyMaterial, int count, String difficulty) {
        String difficultyInstructions = getDifficultyInstructions(difficulty);
        
        return String.format("""
                Generate %d multiple-choice quiz questions from the following study material.
                
                Study Material:
                %s
                
                Requirements:
                - Each question should have exactly 4 options (A, B, C, D)
                - Only ONE option is correct
                - Wrong answers (distractors) must be plausible but clearly incorrect
                - Distractors should be related to the topic (not obviously wrong)
                - %s
                - Include a brief explanation for why the correct answer is right
                
                Format each question EXACTLY like this:
                Q: [Your question here]
                A: [Option A]
                B: [Option B]
                C: [Option C]
                D: [Option D]
                CORRECT: [A/B/C/D]
                EXPLAIN: [Explanation of correct answer]
                
                Generate %d questions now:
                """, count, studyMaterial, difficultyInstructions, count);
    }
    
    /**
     * Get difficulty-specific instructions for the AI prompt
     */
    private String getDifficultyInstructions(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "easy" -> "Questions should test basic recall and recognition of facts";
            case "hard" -> "Questions should require analysis, evaluation, and deep understanding";
            default -> "Questions should test understanding and application of concepts";
        };
    }
    
    /**
     * Parse AI response into list of QuizQuestion objects
     * Expected format:
     * Q: question text
     * A: option A
     * B: option B
     * C: option C
     * D: option D
     * CORRECT: A/B/C/D
     * EXPLAIN: explanation text
     */
    private List<QuizQuestion> parseQuizQuestions(String aiResponse) {
        List<QuizQuestion> questions = new ArrayList<>();
        
        // Pattern to match the quiz format
        // Captures: question, optionA, optionB, optionC, optionD, correct, explanation
        Pattern pattern = Pattern.compile(
            "Q:\\s*(.+?)\\s*A:\\s*(.+?)\\s*B:\\s*(.+?)\\s*C:\\s*(.+?)\\s*D:\\s*(.+?)\\s*CORRECT:\\s*([A-D])\\s*EXPLAIN:\\s*(.+?)(?=Q:|$)",
            Pattern.DOTALL
        );
        
        Matcher matcher = pattern.matcher(aiResponse);
        
        while (matcher.find()) {
            String question = matcher.group(1).trim();
            String optionA = matcher.group(2).trim();
            String optionB = matcher.group(3).trim();
            String optionC = matcher.group(4).trim();
            String optionD = matcher.group(5).trim();
            String correctLetter = matcher.group(6).trim();
            String explanation = matcher.group(7).trim();
            
            // Convert letter to index (A=0, B=1, C=2, D=3)
            int correctIndex = correctLetter.charAt(0) - 'A';
            
            // Create options list
            List<String> options = Arrays.asList(optionA, optionB, optionC, optionD);
            
            questions.add(new QuizQuestion(question, options, correctIndex, explanation));
        }
        
        // Fallback: try simpler parsing if regex fails
        if (questions.isEmpty()) {
            questions = parseQuizQuestionsSimple(aiResponse);
        }
        
        return questions;
    }
    
    /**
     * Simple fallback parser for quiz questions
     */
    private List<QuizQuestion> parseQuizQuestionsSimple(String aiResponse) {
        List<QuizQuestion> questions = new ArrayList<>();
        String[] lines = aiResponse.split("\n");
        
        String currentQuestion = null;
        List<String> currentOptions = new ArrayList<>();
        int correctIndex = -1;
        String explanation = null;
        
        for (String line : lines) {
            line = line.trim();
            
            if (line.startsWith("Q:")) {
                // Save previous question if exists
                if (currentQuestion != null && currentOptions.size() == 4) {
                    questions.add(new QuizQuestion(currentQuestion, new ArrayList<>(currentOptions), correctIndex, explanation));
                }
                // Start new question
                currentQuestion = line.substring(2).trim();
                currentOptions.clear();
                correctIndex = -1;
                explanation = null;
                
            } else if (line.startsWith("A:")) {
                currentOptions.add(line.substring(2).trim());
            } else if (line.startsWith("B:")) {
                currentOptions.add(line.substring(2).trim());
            } else if (line.startsWith("C:")) {
                currentOptions.add(line.substring(2).trim());
            } else if (line.startsWith("D:")) {
                currentOptions.add(line.substring(2).trim());
            } else if (line.startsWith("CORRECT:")) {
                String correctLetter = line.substring(8).trim();
                if (!correctLetter.isEmpty()) {
                    correctIndex = correctLetter.charAt(0) - 'A';
                }
            } else if (line.startsWith("EXPLAIN:")) {
                explanation = line.substring(8).trim();
            }
        }
        
        // Add last question if exists
        if (currentQuestion != null && currentOptions.size() == 4) {
            questions.add(new QuizQuestion(currentQuestion, new ArrayList<>(currentOptions), correctIndex, explanation));
        }
        
        return questions;
    }
}