package ie.tcd.scss.aichat.repository;
import ie.tcd.scss.aichat.model.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    // List<QuizQuestion> findByQuizId(Long quizId);

    // long countByQuizId(Long quizId);
 }
