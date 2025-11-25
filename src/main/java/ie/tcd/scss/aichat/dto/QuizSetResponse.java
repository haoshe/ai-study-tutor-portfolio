package ie.tcd.scss.aichat.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning quiz set data without circular references
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizSetResponse {
    private Long id;
    private Long userId;
    private String username;
    private String title;
    private String studyMaterial;
    private String difficulty;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<QuizQuestionResponse> questions;
}
