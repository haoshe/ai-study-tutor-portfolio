package ie.tcd.scss.aichat.dto;

import lombok.Data;

/**
 * Request DTO for quiz generation
 * Accepts study material text and number of questions to generate
 */
@Data
public class QuizRequest {
    private String studyMaterial;  // The text content to generate quiz from
    private Integer count;          // Number of questions to generate (default: 5)
    private String difficulty;      // Optional: "easy", "medium", "hard" (default: "medium")
}
