package ie.tcd.scss.aichat.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.stream.Collectors;


import ie.tcd.scss.aichat.dto.SlideDocument;
import ie.tcd.scss.aichat.model.Sources;
import ie.tcd.scss.aichat.service.DocumentParsingService;
import ie.tcd.scss.aichat.service.SourcesService;

@RestController
@RequestMapping("/api/slides")
public class DocumentController {

    private final DocumentParsingService documentParsingService;
    private final SourcesService sourcesService;

    public DocumentController(DocumentParsingService documentParsingService, SourcesService sourcesService) {
        this.documentParsingService = documentParsingService;
        this.sourcesService = sourcesService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Sources> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            if (!documentParsingService.isPdfFile(file) && !documentParsingService.isPowerPointFile(file)) {
                return ResponseEntity.badRequest().build();
            }

            SlideDocument parsed = documentParsingService.processDocument(file);

            Sources src = new Sources();
            src.setUserId(userId);
            src.setName(file.getOriginalFilename());
            String type = file.getOriginalFilename().toLowerCase().endsWith(".pdf")
                ? "pdf"
                : "ppt";
            src.setType(type);

           src.setContent(
                parsed.getSections()
                    .stream()
                    .map(section -> section.getContent())
                    .reduce("", (a, b) -> a + "\n\n" + b)
                    );

            Sources saved = sourcesService.save(src);

            return ResponseEntity.ok(saved);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
