package ie.tcd.scss.aichat.dto;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
@Builder
public class SlideDocument {
    private String id;
    private String title;
    private String fileType;  // "PDF" or "PPT"
    private LocalDateTime uploadedAt;
    private List<SlideSection> sections;

    // Required for Builder pattern
    public static class SlideDocumentBuilder {
        private List<SlideSection> sections = new ArrayList<>();
    }

    public SlideDocument() {
        this.sections = new ArrayList<>();
        this.uploadedAt = LocalDateTime.now();
    }

    public SlideDocument(String id, String title, String fileType, LocalDateTime uploadedAt, List<SlideSection> sections) {
        this.id = id;
        this.title = title;
        this.fileType = fileType;
        this.uploadedAt = uploadedAt != null ? uploadedAt : LocalDateTime.now();
        this.sections = sections != null ? sections : new ArrayList<>();
    }
}