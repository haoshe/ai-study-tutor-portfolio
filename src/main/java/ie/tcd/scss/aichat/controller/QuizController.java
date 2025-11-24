package ie.tcd.scss.aichat.controller;

import ie.tcd.scss.aichat.dto.QuizQuestion;
import ie.tcd.scss.aichat.model.User;
import ie.tcd.scss.aichat.service.AuthService;
import ie.tcd.scss.aichat.service.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@CrossOrigin(origins = "*")
public class QuizController {
    
    private final QuizService quizService;
    private final AuthService authService;
    
    public QuizController(QuizService quizService, AuthService authService) {
        this.quizService = quizService;
        this.authService = authService;
    }
    
    @PostMapping("/generate")
    public ResponseEntity<?> generateQuiz(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // Validation
        String studyMaterial = (String) request.get("studyMaterial");
        if (studyMaterial == null || studyMaterial.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Study material is required and cannot be empty"));
        }
        
        // Support both "count" and "questionCount" for backward compatibility
        Integer questionCount = request.containsKey("count") ? 
            (Integer) request.get("count") : 
            (Integer) request.get("questionCount");
        String difficulty = (String) request.get("difficulty");
        
        // Validate difficulty if provided
        if (difficulty != null && !Arrays.asList("easy", "medium", "hard").contains(difficulty.toLowerCase())) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid difficulty. Must be 'easy', 'medium', or 'hard'"));
        }
        
        // Extract user from JWT token (temporary: use default user if not authenticated)
        Long userId = null;
        String title = "AI Generated Quiz";
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                User user = authService.getUserFromToken(token);
                userId = user.getId();
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
            }
        } else {
            // For now, allow unauthenticated requests for testing (use user ID 1)
            userId = 1L;
        }
        
        try {
            List<QuizQuestion> questions = quizService.generateQuiz(studyMaterial, questionCount, difficulty, userId, title);
            Map<String, Object> response = new HashMap<>();
            response.put("questions", questions);

            // Optional warning if fewer questions generated than requested
            if (questionCount != null && questions.size() < questionCount) {
                response.put("warning", String.format(
                    "You requested %d questions but only %d could be generated.",
                    questionCount, questions.size()
                ));
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to generate quiz: " + e.getMessage()));
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testEndpoint() {
        return ResponseEntity.ok(Map.of("message", "Quiz API is working!"));
    }
}