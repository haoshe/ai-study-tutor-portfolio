package ie.tcd.scss.aichat.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sources")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sources {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;        // user who uploaded/created the source

    private String name;        // filename or "Text Source"

    private String type;        // "pdf", "ppt", "text", "demo"

    @Column(columnDefinition = "LONGTEXT")
    private String content;     // extracted text or user-entered text

    private LocalDateTime createdAt = LocalDateTime.now();
}
