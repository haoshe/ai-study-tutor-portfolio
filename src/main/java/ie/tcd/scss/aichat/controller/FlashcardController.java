package ie.tcd.scss.aichat.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ie.tcd.scss.aichat.dto.Flashcard;
import ie.tcd.scss.aichat.dto.FlashcardRequest;
import ie.tcd.scss.aichat.model.User;
import ie.tcd.scss.aichat.service.AuthService;
import ie.tcd.scss.aichat.dto.FlashcardResponse;
import ie.tcd.scss.aichat.dto.FlashcardSetResponse;
import ie.tcd.scss.aichat.model.FlashcardSet;
import ie.tcd.scss.aichat.model.User;
import ie.tcd.scss.aichat.repository.FlashcardSetRepository;
import ie.tcd.scss.aichat.repository.UserRepository;
import ie.tcd.scss.aichat.service.FlashcardService;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for flashcard generation
 * Provides endpoints for generating flashcards from study material
 */
@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
public class FlashcardController {
    
    private final FlashcardService flashcardService;
    private final UserRepository userRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    
    /**
     * Generate flashcards from study material
     * 
     * POST /api/flashcards/generate
     * Request body:
     * {
     *   "studyMaterial": "Your study content here...",
     *   "count": 5
     * }
     * 
     * @param request FlashcardRequest containing study material and count
     * @param authHeader JWT token from Authorization header
     * @return List of generated flashcards
     */
    @PostMapping("/generate")
    public ResponseEntity<List<Flashcard>> generateFlashcards(
            @RequestBody FlashcardRequest request,
            Authentication authentication) {
        // Validate input
        if (request.getStudyMaterial() == null || request.getStudyMaterial().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Extract user from authenticated security context
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String title = "AI Generated Flashcards";
        
        // Generate flashcards using AI
        List<Flashcard> flashcards = flashcardService.generateFlashcards(
            request.getStudyMaterial(),
            request.getCount(),
            user.getId(),
            title
        );
        
        return ResponseEntity.ok(flashcards);
    }
    
    /**
     * Simple test endpoint to verify the controller is working
     * 
     * GET /api/flashcards/test
     * 
     * @return A simple test message
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Flashcard API is working!");
    }
    
    /**
     * Get user's flashcard history (all saved flashcard sets)
     * 
     * GET /api/flashcards/history
     * 
     * @param authentication Authenticated user from JWT
     * @return List of user's flashcard sets
     */
    @GetMapping("/history")
    public ResponseEntity<List<FlashcardSetResponse>> getHistory(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<FlashcardSetResponse> history = flashcardSetRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(history);
    }
    
    /**
     * Get a specific flashcard set by ID
     * 
     * GET /api/flashcards/{id}
     * 
     * @param id Flashcard set ID
     * @param authentication Authenticated user from JWT
     * @return Flashcard set if owned by user, 403 otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getFlashcardSet(@PathVariable Long id, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        FlashcardSet flashcardSet = flashcardSetRepository.findById(id)
                .orElse(null);
        
        if (flashcardSet == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Ownership check
        if (!flashcardSet.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(convertToDto(flashcardSet));
    }
    
    /**
     * Delete a flashcard set by ID
     * 
     * DELETE /api/flashcards/{id}
     * 
     * @param id Flashcard set ID
     * @param authentication Authenticated user from JWT
     * @return 204 if deleted, 403 if not owned by user, 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFlashcardSet(@PathVariable Long id, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        FlashcardSet flashcardSet = flashcardSetRepository.findById(id)
                .orElse(null);
        
        if (flashcardSet == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Ownership check
        if (!flashcardSet.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        flashcardSetRepository.delete(flashcardSet);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Convert FlashcardSet entity to DTO
     */
    private FlashcardSetResponse convertToDto(FlashcardSet set) {
        List<FlashcardResponse> flashcardDtos = set.getFlashcards().stream()
                .map(f -> new FlashcardResponse(
                    f.getId(),
                    f.getQuestion(),
                    f.getAnswer(),
                    f.getPosition()
                ))
                .collect(Collectors.toList());
        
        return new FlashcardSetResponse(
            set.getId(),
            set.getUser().getId(),
            set.getUser().getUsername(),
            set.getTitle(),
            set.getStudyMaterial(),
            set.getCreatedAt(),
            set.getUpdatedAt(),
            flashcardDtos
        );
    }
}