package ie.tcd.scss.aichat.repository;

import ie.tcd.scss.aichat.model.QuizSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizSetRepository extends JpaRepository<QuizSet, Long> {
    
    List<QuizSet> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    boolean existsByIdAndUserId(Long id, Long userId);
}
