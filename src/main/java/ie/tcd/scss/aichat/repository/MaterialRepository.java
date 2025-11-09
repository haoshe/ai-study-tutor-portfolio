package ie.tcd.scss.aichat.repository;
import ie.tcd.scss.aichat.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    
    List<Material> findByCourseId(Long courseId);
    
    List<Material> findByCourseIdOrderByUploadedAtDesc(Long courseId);
    
    long countByCourseId(Long courseId);
}