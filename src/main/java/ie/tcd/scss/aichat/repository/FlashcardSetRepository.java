package ie.tcd.scss.aichat.repository;

import ie.tcd.scss.aichat.model.FlashcardSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashcardSetRepository extends JpaRepository<FlashcardSet, Long> {
    
    List<FlashcardSet> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    boolean existsByIdAndUserId(Long id, Long userId);
}
