package ie.tcd.scss.aichat.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning flashcard set data without circular references
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardSetResponse {
    private Long id;
    private Long userId;
    private String username;
    private String title;
    private String studyMaterial;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<FlashcardResponse> flashcards;
}
