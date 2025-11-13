package ie.tcd.scss.aichat.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import ie.tcd.scss.aichat.dto.SlideDocument;

class DocumentParsingServiceTest {

    private DocumentParsingService documentParsingService;

    @BeforeEach
    void setUp() {
        documentParsingService = new DocumentParsingService();
    }

    @Test
    void isPdfFile_WithValidPdf_ReturnsTrue() {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.pdf",
            "application/pdf",
            "dummy content".getBytes()
        );
        assertTrue(documentParsingService.isPdfFile(file));
    }

    @Test
    void isPdfFile_WithNonPdfFile_ReturnsFalse() {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.txt",
            "text/plain",
            "dummy content".getBytes()
        );
        assertFalse(documentParsingService.isPdfFile(file));
    }

    private byte[] createTestPdf(String content) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText(content);
                contentStream.endText();
            }

            Path tempFile = Files.createTempFile("test", ".pdf");
            document.save(tempFile.toFile());
            byte[] pdfContent = Files.readAllBytes(tempFile);
            Files.delete(tempFile);
            return pdfContent;
        }
    }

    @Test
    void processDocument_WithValidPdf_ExtractsText() throws IOException {
        String expectedText = "Hello, this is a test PDF file!";
        byte[] pdfContent = createTestPdf(expectedText);
        
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.pdf",
            "application/pdf",
            pdfContent
        );

        SlideDocument document = documentParsingService.processDocument(file);
        assertNotNull(document);
        assertEquals("PDF", document.getFileType());
        assertNotNull(document.getId());
        assertEquals("test.pdf", document.getTitle());
    }

    @Test
    void processDocument_WithInvalidFile_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "This is not a valid document file".getBytes()
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            documentParsingService.processDocument(file);
        });
        assertTrue(exception.getMessage().contains("Unsupported file type"));
    }
}