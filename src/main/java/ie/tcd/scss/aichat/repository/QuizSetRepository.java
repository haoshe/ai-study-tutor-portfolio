package ie.tcd.scss.aichat.repository;

import ie.tcd.scss.aichat.model.QuizSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizSetRepository extends JpaRepository<QuizSet, Long> {

    @Query("SELECT qs FROM QuizSet qs WHERE qs.user.id = :userId ORDER BY qs.createdAt DESC")
    List<QuizSet> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);
}
