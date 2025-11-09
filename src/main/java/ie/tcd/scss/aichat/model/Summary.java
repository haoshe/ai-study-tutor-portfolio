package ie.tcd.scss.aichat.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "summaries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Summary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne //TODO: Check if ManyToOne is correct here
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
