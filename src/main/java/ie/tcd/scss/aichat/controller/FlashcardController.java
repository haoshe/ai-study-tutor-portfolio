package ie.tcd.scss.aichat.controller;

import ie.tcd.scss.aichat.dto.Flashcard;
import ie.tcd.scss.aichat.dto.FlashcardRequest;
import ie.tcd.scss.aichat.model.User;
import ie.tcd.scss.aichat.service.AuthService;
import ie.tcd.scss.aichat.service.FlashcardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for flashcard generation
 * Provides endpoints for generating flashcards from study material
 */
@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
public class FlashcardController {
    
    private final FlashcardService flashcardService;
    private final AuthService authService;
    
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
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // Validate input
        if (request.getStudyMaterial() == null || request.getStudyMaterial().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Extract user from JWT token (temporary: use default user if not authenticated)
        Long userId = null;
        String title = "AI Generated Flashcards";
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                User user = authService.getUserFromToken(token);
                userId = user.getId();
            } catch (Exception e) {
                return ResponseEntity.status(401).build();
            }
        } else {
            // For now, allow unauthenticated requests for testing (use user ID 1)
            userId = 1L;
        }
        
        // Generate flashcards using AI
        List<Flashcard> flashcards = flashcardService.generateFlashcards(
            request.getStudyMaterial(),
            request.getCount(),
            userId,
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
}