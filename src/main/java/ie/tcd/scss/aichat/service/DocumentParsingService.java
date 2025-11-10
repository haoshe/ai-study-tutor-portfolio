package ie.tcd.scss.aichat.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ie.tcd.scss.aichat.dto.LearningMaterial;
import ie.tcd.scss.aichat.dto.SlideDocument;
import ie.tcd.scss.aichat.dto.SlideSection;

@Service
public class DocumentParsingService {

    /**
     * Processes an uploaded file (PDF or PowerPoint) and returns a structured document.
     *
     * @param file The uploaded file
     * @return A structured SlideDocument containing the parsed content
     * @throws IOException If there's an error reading the file
     */
    public SlideDocument processDocument(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        
        if (isPdfFile(file)) {
            return processPdfDocument(file);
        } else if (isPowerPointFile(file)) {
            return processPowerPointDocument(file);
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + contentType);
        }
    }

    private SlideDocument processPdfDocument(MultipartFile file) throws IOException {
        byte[] content = file.getBytes();
        try (ByteArrayInputStream bis = new ByteArrayInputStream(content);
             PDDocument document = PDDocument.load(bis)) {
            
            SlideDocument slideDoc = SlideDocument.builder()
                    .id(UUID.randomUUID().toString())
                    .title(file.getOriginalFilename())
                    .fileType("PDF")
                    .sections(new ArrayList<>())
                    .build();

            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();
            
            // Extract text page by page
            for (int i = 1; i <= totalPages; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String pageText = stripper.getText(document);
                
                SlideSection section = SlideSection.builder()
                        .pageNumber(i)
                        .content(pageText.trim())
                        .learningMaterial(new LearningMaterial())
                        .build();
                slideDoc.getSections().add(section);
            }
            
            return slideDoc;
        }
    }

    private SlideDocument processPowerPointDocument(MultipartFile file) throws IOException {
        byte[] content = file.getBytes();
        try (ByteArrayInputStream bis = new ByteArrayInputStream(content);
             XMLSlideShow ppt = new XMLSlideShow(bis)) {
            
            SlideDocument document = SlideDocument.builder()
                    .id(UUID.randomUUID().toString())
                    .title(file.getOriginalFilename())
                    .fileType("PPT")
                    .sections(new ArrayList<>())
                    .build();

            for (XSLFSlide slide : ppt.getSlides()) {
                String slideText = extractTextFromSlide(slide);
                SlideSection section = SlideSection.builder()
                        .pageNumber(slide.getSlideNumber())
                        .content(slideText)
                        .learningMaterial(new LearningMaterial())
                        .build();
                document.getSections().add(section);
            }
            
            return document;
        }
    }

    private String extractTextFromSlide(XSLFSlide slide) {
        StringBuilder text = new StringBuilder();
        for (XSLFShape shape : slide.getShapes()) {
            if (shape instanceof XSLFTextShape textShape) {
                text.append(textShape.getText()).append("\n");
            }
        }
        return text.toString().trim();
    }

    public boolean isPdfFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.equals("application/pdf");
    }

    public boolean isPowerPointFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && 
               (contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation") ||
                contentType.equals("application/vnd.ms-powerpoint"));
    }
}