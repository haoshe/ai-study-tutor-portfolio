package ie.tcd.scss.aichat.controller;

import ie.tcd.scss.aichat.dto.Flashcard;
import ie.tcd.scss.aichat.dto.FlashcardRequest;
import ie.tcd.scss.aichat.dto.FlashcardResponse;
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
     * @return FlashcardResponse with generated flashcards and optional warning
     */
    @PostMapping("/generate")
    public ResponseEntity<FlashcardResponse> generateFlashcards(@RequestBody FlashcardRequest request) {
        // Validate input
        if (request.getStudyMaterial() == null || request.getStudyMaterial().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        int requestedCount = request.getCount() != null ? request.getCount() : 5;
        
        // Generate flashcards using AI
        List<Flashcard> flashcards = flashcardService.generateFlashcards(
            request.getStudyMaterial(),
            requestedCount
        );
        
        // Determine warning message
        String warning = null;
        if (flashcards.isEmpty()) {
            warning = "Unable to generate flashcards. The study material may be too short, repetitive, or lack educational content. Please provide more substantial material.";
        } else if (flashcards.size() < requestedCount) {
            warning = String.format(
                "You requested %d flashcards, but we could only generate %d based on your study material. To get more flashcards, please provide more content.",
                requestedCount, flashcards.size()
            );
        }
        
        // Create response with flashcards and optional warning
        FlashcardResponse response = new FlashcardResponse(flashcards, warning);
        return ResponseEntity.ok(response);
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