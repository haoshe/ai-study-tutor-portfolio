package ie.tcd.scss.aichat.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash; // Should be hashed (BCrypt)

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    // private List<Course> courses;

    // @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    // private List<QuizResult> quizResults;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
