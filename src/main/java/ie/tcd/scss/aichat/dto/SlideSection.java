package ie.tcd.scss.aichat.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class SlideSection {
    private String title;
    private String content;
    private int pageNumber;
    private LearningMaterial learningMaterial;

    public SlideSection() {
    }

    public SlideSection(String title, String content, int pageNumber, LearningMaterial learningMaterial) {
        this.title = title;
        this.content = content;
        this.pageNumber = pageNumber;
        this.learningMaterial = learningMaterial;
    }
}