package ie.tcd.scss.aichat.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String optionA;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String optionB;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String optionC;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String optionD;

    @Column(nullable = false)
    private String correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String explanation;
    
    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
}
