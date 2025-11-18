package ie.tcd.scss.aichat.controller;

import ie.tcd.scss.aichat.dto.QuizQuestion;
import ie.tcd.scss.aichat.dto.QuizRequest;
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
     * @return List of generated quiz questions
     */
    @PostMapping("/generate")
    public ResponseEntity<List<QuizQuestion>> generateQuiz(@RequestBody QuizRequest request) {
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
        
        // Generate quiz using AI
        List<QuizQuestion> questions = quizService.generateQuiz(
            request.getStudyMaterial(),
            request.getCount(),
            request.getDifficulty()
        );
         // ⭐ FIX: ensure returned value is always a List, never a Map/null
        if (questions == null) {        // ⭐ added
            questions = List.of();      // ⭐ added
        }                                // ⭐ added

        
        return ResponseEntity.ok(questions);
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
