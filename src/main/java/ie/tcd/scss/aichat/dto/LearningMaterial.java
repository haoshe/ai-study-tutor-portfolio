package ie.tcd.scss.aichat.dto;

import lombok.Data;
import lombok.Builder;
import java.util.List;
import java.util.ArrayList;

@Data
@Builder
public class LearningMaterial {
    private String topic;
    private List<String> keyPoints;
    private List<String> questions;

    public LearningMaterial() {
        this.keyPoints = new ArrayList<>();
        this.questions = new ArrayList<>();
    }

    public LearningMaterial(String topic, List<String> keyPoints, List<String> questions) {
        this.topic = topic;
        this.keyPoints = keyPoints != null ? keyPoints : new ArrayList<>();
        this.questions = questions != null ? questions : new ArrayList<>();
    }

    public void addKeyPoint(String keyPoint) {
        if (this.keyPoints == null) {
            this.keyPoints = new ArrayList<>();
        }
        this.keyPoints.add(keyPoint);
    }

    public void addQuestion(String question) {
        if (this.questions == null) {
            this.questions = new ArrayList<>();
        }
        this.questions.add(question);
    }
}