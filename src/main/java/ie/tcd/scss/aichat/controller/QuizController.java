package ie.tcd.scss.aichat.controller;

import ie.tcd.scss.aichat.dto.QuizQuestion;
import ie.tcd.scss.aichat.dto.QuizRequest;
import ie.tcd.scss.aichat.dto.QuizResponse;
import ie.tcd.scss.aichat.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for quiz generation
 * Provides endpoints for generating multiple-choice quizzes from study material
 */
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {
    
    private final QuizService quizService;
    
    /**
     * Generate multiple-choice quiz questions from study material
     * 
     * POST /api/quiz/generate
     * Request body:
     * {
     *   "studyMaterial": "Your study content here...",
     *   "count": 5,
     *   "difficulty": "medium"
     * }
     * 
     * @param request QuizRequest containing study material, count, and difficulty
     * @return QuizResponse with generated quiz questions and optional warning
     */
    @PostMapping("/generate")
    public ResponseEntity<QuizResponse> generateQuiz(@RequestBody QuizRequest request) {
        // Validate input
        if (request.getStudyMaterial() == null || request.getStudyMaterial().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Validate difficulty if provided
        if (request.getDifficulty() != null) {
            String difficulty = request.getDifficulty().toLowerCase();
            if (!difficulty.equals("easy") && !difficulty.equals("medium") && !difficulty.equals("hard")) {
                return ResponseEntity.badRequest().build();
            }
        }
        
        int requestedCount = request.getCount() != null ? request.getCount() : 5;
        
        // Generate quiz using AI
        List<QuizQuestion> questions = quizService.generateQuiz(
            request.getStudyMaterial(),
            requestedCount,
            request.getDifficulty()
        );
        
        // Determine warning message
        String warning = null;
        if (questions.isEmpty()) {
            warning = "Unable to generate quiz questions. The study material may be too short, repetitive, or lack educational content. Please provide more substantial material.";
        } else if (questions.size() < requestedCount) {
            warning = String.format(
                "You requested %d questions, but we could only generate %d based on your study material. To get more questions, please provide more content.",
                requestedCount, questions.size()
            );
        }
        
        // Create response with questions and optional warning
        QuizResponse response = new QuizResponse(questions, warning);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Simple test endpoint to verify the controller is working
     * 
     * GET /api/quiz/test
     * 
     * @return A simple test message
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Quiz API is working!");
    }
}