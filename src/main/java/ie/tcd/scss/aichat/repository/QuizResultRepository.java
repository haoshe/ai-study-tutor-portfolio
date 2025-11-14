package ie.tcd.scss.aichat.repository;
import ie.tcd.scss.aichat.model.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    // List<QuizResult> findByUserId(Long userId);
    
    // List<QuizResult> findByUserIdOrderByCompletedAtDesc(Long userId);

    // List<QuizResult> findByQuizId(Long quizId);
    
    // long countByUserId(Long userId);

//     @Query("SELECT AVG(qr.score * 100.0 / qr.totalQuestions) FROM QuizResult qr WHERE qr.user.id = :userId")
//     Double findAverageScoreByUserId(@Param("userId") Long userId);
    
//     List<QuizResult> findByUserIdAndQuizId(Long userId, Long quizId);
}