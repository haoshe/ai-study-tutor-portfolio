package ie.tcd.scss.aichat.repository;
import ie.tcd.scss.aichat.model.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    // Flashcard entity has flashcardSet (set_id), not courseId
    // Custom queries can be added here if needed
}
