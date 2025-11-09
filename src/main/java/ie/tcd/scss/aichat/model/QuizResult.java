package ie.tcd.scss.aichat.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private Integer totalQuestions;

    @Column(nullable = false)
    private LocalDateTime takenAt;

    @Column(nullable = false)
    private LocalDateTime completedAt;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @PrePersist
    protected void onCreate() {
        completedAt = LocalDateTime.now();
    }
    
    public double getPercentage() {
        if (totalQuestions == 0) return 0.0;
        return (score * 100.0) / totalQuestions;
    }
}
