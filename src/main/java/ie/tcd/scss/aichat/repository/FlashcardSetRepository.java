package ie.tcd.scss.aichat.repository;

import ie.tcd.scss.aichat.model.FlashcardSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashcardSetRepository extends JpaRepository<FlashcardSet, Long> {

    @Query("SELECT fs FROM FlashcardSet fs WHERE fs.user.id = :userId ORDER BY fs.createdAt DESC")
    List<FlashcardSet> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);
}
