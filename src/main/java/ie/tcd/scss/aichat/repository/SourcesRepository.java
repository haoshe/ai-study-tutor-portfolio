package ie.tcd.scss.aichat.repository;

import ie.tcd.scss.aichat.model.Sources;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SourcesRepository extends JpaRepository<Sources, Long> {

    List<Sources> findByUserId(Long userId);
}
