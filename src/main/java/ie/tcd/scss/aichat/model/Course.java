package ie.tcd.scss.aichat.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<Material> materials;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<QuizSet> quizzeSets;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Flashcard> flashcards;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
