package ie.tcd.scss.aichat.dto;

import lombok.Data;

/**
 * Request DTO for flashcard generation
 * Accepts study material text and number of flashcards to generate
 */
@Data
public class FlashcardRequest {
    private String studyMaterial;  // The text content to generate flashcards from
    private Integer count;          // Number of flashcards to generate (default: 5)
}