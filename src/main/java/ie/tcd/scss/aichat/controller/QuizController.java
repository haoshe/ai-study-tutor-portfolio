package ie.tcd.scss.aichat.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ie.tcd.scss.aichat.dto.QuizQuestion;
import ie.tcd.scss.aichat.dto.QuizQuestionResponse;
import ie.tcd.scss.aichat.dto.QuizSetResponse;
import ie.tcd.scss.aichat.model.QuizSet;
import ie.tcd.scss.aichat.model.User;
import ie.tcd.scss.aichat.repository.QuizSetRepository;
import ie.tcd.scss.aichat.repository.UserRepository;
import ie.tcd.scss.aichat.service.QuizService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/quiz")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class QuizController {
    
    private final QuizService quizService;
    private final UserRepository userRepository;
    private final QuizSetRepository quizSetRepository;
    
    @PostMapping("/generate")
    public ResponseEntity<?> generateQuiz(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
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
        
        // Extract user from authenticated security context
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Long userId = user.getId();
        String title = "AI Generated Quiz";

        try {
            List<QuizQuestion> questions = quizService.generateQuiz(
                    studyMaterial,
                    questionCount,
                    difficulty,
                    userId,
                    title
            );

            if (questions == null) {
                questions = List.of();
            }

            return ResponseEntity.ok(questions);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to generate quiz: " + e.getMessage()));
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testEndpoint() {
        return ResponseEntity.ok(Map.of("message", "Quiz API is working!"));
    }
    
    /**
     * Get user's quiz history (all saved quiz sets)
     * 
     * GET /api/quiz/history
     * 
     * @param authentication Authenticated user from JWT
     * @return List of user's quiz sets
     */
    @GetMapping("/history")
    public ResponseEntity<List<QuizSetResponse>> getHistory(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<QuizSetResponse> history = quizSetRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(history);
    }
    
    /**
     * Get a specific quiz set by ID
     * 
     * GET /api/quiz/{id}
     * 
     * @param id Quiz set ID
     * @param authentication Authenticated user from JWT
     * @return Quiz set if owned by user, 403 otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getQuizSet(@PathVariable Long id, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        QuizSet quizSet = quizSetRepository.findById(id)
                .orElse(null);
        
        if (quizSet == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Ownership check
        if (!quizSet.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(convertToDto(quizSet));
    }
    
    /**
     * Delete a quiz set by ID
     * 
     * DELETE /api/quiz/{id}
     * 
     * @param id Quiz set ID
     * @param authentication Authenticated user from JWT
     * @return 204 if deleted, 403 if not owned by user, 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuizSet(@PathVariable Long id, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        QuizSet quizSet = quizSetRepository.findById(id)
                .orElse(null);
        
        if (quizSet == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Ownership check
        if (!quizSet.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        quizSetRepository.delete(quizSet);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Convert QuizSet entity to DTO
     */
    private QuizSetResponse convertToDto(QuizSet set) {
        List<QuizQuestionResponse> questionDtos = set.getQuestions().stream()
                .map(q -> new QuizQuestionResponse(
                    q.getId(),
                    q.getQuestion(),
                    q.getOptionA(),
                    q.getOptionB(),
                    q.getOptionC(),
                    q.getOptionD(),
                    q.getCorrectAnswer(),
                    q.getExplanation(),
                    q.getPosition()
                ))
                .collect(Collectors.toList());
        
        return new QuizSetResponse(
            set.getId(),
            set.getUser().getId(),
            set.getUser().getUsername(),
            set.getTitle(),
            set.getStudyMaterial(),
            set.getDifficulty(),
            set.getCreatedAt(),
            set.getUpdatedAt(),
            questionDtos
        );
    }
}
