package ie.tcd.scss.aichat.repository;
import ie.tcd.scss.aichat.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // List<Course> findByUserId(Long userId);
    
    // List<Course> findByUserIdOrderByCreatedAtDesc(Long userId);

    // long countByUserId(Long userId);
 }