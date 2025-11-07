package ie.tcd.scss.aichat.controller;

import ie.tcd.scss.aichat.dto.Flashcard;
import ie.tcd.scss.aichat.dto.FlashcardRequest;
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
     * @return List of generated flashcards
     */
    @PostMapping("/generate")
    public ResponseEntity<List<Flashcard>> generateFlashcards(@RequestBody FlashcardRequest request) {
        // Validate input
        if (request.getStudyMaterial() == null || request.getStudyMaterial().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Generate flashcards using AI
        List<Flashcard> flashcards = flashcardService.generateFlashcards(
            request.getStudyMaterial(),
            request.getCount()
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