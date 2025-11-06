package ie.tcd.scss.aichat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single flashcard with a question and answer
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Flashcard {
    private String question;  // The question side of the flashcard
    private String answer;    // The answer side of the flashcard
}