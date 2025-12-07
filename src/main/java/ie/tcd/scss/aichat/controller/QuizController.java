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
import ie.tcd.scss.aichat.exception.ResourceNotFoundException;
import ie.tcd.scss.aichat.exception.ForbiddenException;
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
            throw new IllegalArgumentException("Study material is required and cannot be empty");
        }
        
        // Check if study material is too large (over 2 million characters ~ 500k tokens)
        if (studyMaterial.length() > 2_000_000) {
            throw new IllegalArgumentException("Study material is too large. Maximum 2 million characters allowed.");
        }
        
        // Support both "count" and "questionCount" for backward compatibility
        Integer questionCount = request.containsKey("count") ? 
            (Integer) request.get("count") : 
            (Integer) request.get("questionCount");
        String difficulty = (String) request.get("difficulty");
        
        // Validate difficulty if provided
        if (difficulty != null && !Arrays.asList("easy", "medium", "hard").contains(difficulty.toLowerCase())) {
            throw new IllegalArgumentException("Invalid difficulty. Must be 'easy', 'medium', or 'hard'");
        }
        
        // Extract user from authenticated security context
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", userDetails.getUsername()));
        
        Long userId = user.getId();
        String title = "AI Generated Quiz";

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
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", userDetails.getUsername()));
        
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
    public ResponseEntity<QuizSetResponse> getQuizSet(@PathVariable Long id, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", userDetails.getUsername()));
        
        QuizSet quizSet = quizSetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuizSet", "id", id));
        
        // Ownership check
        if (!quizSet.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to access this quiz set");
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
    public ResponseEntity<Void> deleteQuizSet(@PathVariable Long id, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", userDetails.getUsername()));
        
        QuizSet quizSet = quizSetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuizSet", "id", id));
        
        // Ownership check
        if (!quizSet.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to delete this quiz set");
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
