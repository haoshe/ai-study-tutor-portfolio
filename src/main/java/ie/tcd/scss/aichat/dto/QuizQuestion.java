package ie.tcd.scss.aichat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a multiple-choice quiz question
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizQuestion {
    private String question;           // The question text
    private List<String> options;      // List of answer options (typically 4)
    private int correctAnswer;         // Index of correct answer (0-3)
    private String explanation;        // Explanation of why the answer is correct
}
