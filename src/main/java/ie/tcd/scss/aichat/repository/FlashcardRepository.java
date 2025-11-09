package ie.tcd.scss.aichat.repository;
import ie.tcd.scss.aichat.model.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    List<Flashcard> findByCourseId(Long courseId);

    List<Flashcard> findByMaterialId(Long materialId);

    long countByCourseId(Long courseId);

    List<Flashcard> findByCourseIdOrderByCreatedAtDesc(Long courseId);
}
