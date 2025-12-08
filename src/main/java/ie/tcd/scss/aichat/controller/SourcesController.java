package ie.tcd.scss.aichat.controller;

import ie.tcd.scss.aichat.model.Sources;
import ie.tcd.scss.aichat.service.SourcesService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sources")
@CrossOrigin(origins = "*")
public class SourcesController {

    private final SourcesService service;

    public SourcesController(SourcesService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Sources> createSource(@RequestBody Sources src) {
        Sources saved = service.save(src);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Sources>> getSources(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getUserSources(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSource(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}
