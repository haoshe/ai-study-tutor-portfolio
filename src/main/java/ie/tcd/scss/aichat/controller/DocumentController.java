package ie.tcd.scss.aichat.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ie.tcd.scss.aichat.dto.SlideDocument;
import ie.tcd.scss.aichat.service.DocumentParsingService;

@RestController
@RequestMapping("/api/slides")
public class DocumentController {

    private final DocumentParsingService documentParsingService;

    public DocumentController(DocumentParsingService documentParsingService) {
        this.documentParsingService = documentParsingService;
    }

    @PostMapping("/upload")
    public ResponseEntity<SlideDocument> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            if (!documentParsingService.isPdfFile(file) && !documentParsingService.isPowerPointFile(file)) {
                return ResponseEntity.badRequest().build();
            }

            SlideDocument document = documentParsingService.processDocument(file);
            return ResponseEntity.ok(document);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}