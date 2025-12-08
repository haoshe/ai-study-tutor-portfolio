package ie.tcd.scss.aichat.service;

import ie.tcd.scss.aichat.model.Sources;
import ie.tcd.scss.aichat.repository.SourcesRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SourcesService {

    private final SourcesRepository repository;

    public SourcesService(SourcesRepository repository) {
        this.repository = repository;
    }

    public Sources save(Sources src) {
        return repository.save(src);
    }

    public List<Sources> getUserSources(Long userId) {
        return repository.findByUserId(userId);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
