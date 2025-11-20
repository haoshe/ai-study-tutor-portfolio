package ie.tcd.scss.aichat.controller;

import ie.tcd.scss.aichat.dto.QuizQuestion;
import ie.tcd.scss.aichat.model.User;
import ie.tcd.scss.aichat.service.AuthService;
import ie.tcd.scss.aichat.service.QuizService;
import org.springframework.web.bind.annotation.*;

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
    public List<QuizQuestion> generateQuiz(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String studyMaterial = (String) request.get("studyMaterial");
        Integer questionCount = (Integer) request.get("questionCount");
        String difficulty = (String) request.get("difficulty");
        
        // Extract user from JWT token (temporary: use default user if not authenticated)
        Long userId = null;
        String title = "AI Generated Quiz";
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                User user = authService.getUserFromToken(token);
                userId = user.getId();
            } catch (Exception e) {
                throw new RuntimeException("Unauthorized");
            }
        } else {
            // For now, allow unauthenticated requests for testing (use user ID 1)
            userId = 1L;
        }
        
        return quizService.generateQuiz(studyMaterial, questionCount, difficulty, userId, title);
    }
}
