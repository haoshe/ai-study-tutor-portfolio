package ie.tcd.scss.aichat.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LearningSession {
    private String id;
    private String userId;  // Will be hardcoded for MVP
    private String documentContent;  // Extracted text from PDF
    private LocalDateTime createdAt;
    private List<QuestionRecord> questions;
    private int correctAnswers;
    private int totalAnswers;

    public LearningSession() {
        this.questions = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.correctAnswers = 0;
        this.totalAnswers = 0;
    }

    @Builder
    public LearningSession(String id, String userId, String documentContent, LocalDateTime createdAt,
                          List<QuestionRecord> questions, int correctAnswers, int totalAnswers) {
        this.id = id;
        this.userId = userId;
        this.documentContent = documentContent;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.questions = questions != null ? questions : new ArrayList<>();
        this.correctAnswers = correctAnswers;
        this.totalAnswers = totalAnswers;
    }

    public void addQuestion(QuestionRecord question) {
        if (this.questions == null) {
            this.questions = new ArrayList<>();
        }
        this.questions.add(question);
    }

    public void updateStats(boolean wasCorrect) {
        this.totalAnswers++;
        if (wasCorrect) {
            this.correctAnswers++;
        }
    }
}