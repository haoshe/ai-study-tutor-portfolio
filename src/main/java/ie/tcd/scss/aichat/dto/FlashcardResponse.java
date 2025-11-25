package ie.tcd.scss.aichat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning individual flashcard data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardResponse {
    private Long id;
    private String question;
    private String answer;
    private Integer position;
}
