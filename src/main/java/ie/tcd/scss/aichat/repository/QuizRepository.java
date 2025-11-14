package ie.tcd.scss.aichat.repository;
import ie.tcd.scss.aichat.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
 public interface QuizRepository extends JpaRepository<Quiz, Long> {
    
    // List<Quiz> findByCourseId(Long courseId);

    // List<Quiz> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    // long countByCourseId(Long courseId);
}
