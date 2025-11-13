package ie.tcd.scss.aichat.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuestionRecord {
    private String id;
    private String question;
    private String userAnswer;
    private AnswerEvaluation evaluation;
    private String followUpQuestion;
    private LocalDateTime timestamp;

    public QuestionRecord() {
        this.timestamp = LocalDateTime.now();
        this.evaluation = AnswerEvaluation.PENDING;
    }

    @Builder
    public QuestionRecord(String id, String question, String userAnswer, 
                         AnswerEvaluation evaluation, String followUpQuestion, LocalDateTime timestamp) {
        this.id = id;
        this.question = question;
        this.userAnswer = userAnswer;
        this.evaluation = evaluation != null ? evaluation : AnswerEvaluation.PENDING;
        this.followUpQuestion = followUpQuestion;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }
}