package ie.tcd.scss.aichat.controller;

import ie.tcd.scss.aichat.dto.Flashcard;
import ie.tcd.scss.aichat.service.FlashcardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration test for FlashcardController
 * Uses MockMvc to test REST API without starting full server
 * 
 */

@WebMvcTest(FlashcardController.class)
// Security filters are disabled for this test class to prevent 401/403 responses during
// FlashcardController tests. This ensures that the controller can be tested without
// authentication requirements.
// Contributor: Tomas A.
@AutoConfigureMockMvc(addFilters = false)
class FlashcardControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private FlashcardService flashcardService;
    
    @Test
    void testGenerateFlashcards_Success() throws Exception {
        // Given: Mock service response
        List<Flashcard> mockFlashcards = List.of(
            new Flashcard("What is Spring Boot?", "An open-source Java framework for building production-ready applications"),
            new Flashcard("What does @Autowired do?", "It enables automatic dependency injection in Spring"),
            new Flashcard("What is Inversion of Control?", "A design principle where the framework controls object creation and lifecycle")
        );
        
        when(flashcardService.generateFlashcards(any(String.class), eq(3)))
            .thenReturn(mockFlashcards);
        
        // When & Then: Make POST request and verify response
        mockMvc.perform(post("/api/flashcards/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "studyMaterial": "Spring Boot is an open-source Java framework for building production-ready applications.",
                      "count": 3
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flashcards", hasSize(3)))
                .andExpect(jsonPath("$.flashcards[0].question", is("What is Spring Boot?")))
                .andExpect(jsonPath("$.flashcards[0].answer", is("An open-source Java framework for building production-ready applications")))
                .andExpect(jsonPath("$.flashcards[1].question", is("What does @Autowired do?")))
                .andExpect(jsonPath("$.flashcards[1].answer", is("It enables automatic dependency injection in Spring")))
                .andExpect(jsonPath("$.flashcards[2].question", is("What is Inversion of Control?")))
                .andExpect(jsonPath("$.flashcards[2].answer", is("A design principle where the framework controls object creation and lifecycle")))
                .andExpect(jsonPath("$.warning").doesNotExist());
    }
    
    @Test
    void testGenerateFlashcards_WithNullCount() throws Exception {
        // Given: Mock service response with default count
        List<Flashcard> mockFlashcards = List.of(
            new Flashcard("Question 1?", "Answer 1"),
            new Flashcard("Question 2?", "Answer 2")
        );
        
        when(flashcardService.generateFlashcards(any(String.class), eq(5)))
            .thenReturn(mockFlashcards);
        
        // When & Then: Request without count field
        mockMvc.perform(post("/api/flashcards/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "studyMaterial": "Test material"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flashcards", hasSize(2)));
    }
    
    @Test
    void testGenerateFlashcards_EmptyStudyMaterial_ReturnsBadRequest() throws Exception {
        // When & Then: Request with empty study material
        mockMvc.perform(post("/api/flashcards/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "studyMaterial": "",
                      "count": 3
                    }
                    """))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testGenerateFlashcards_NullStudyMaterial_ReturnsBadRequest() throws Exception {
        // When & Then: Request without study material field
        mockMvc.perform(post("/api/flashcards/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "count": 3
                    }
                    """))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testGenerateFlashcards_WithWhitespaceOnly_ReturnsBadRequest() throws Exception {
        // When & Then: Request with only whitespace
        mockMvc.perform(post("/api/flashcards/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "studyMaterial": "   ",
                      "count": 3
                    }
                    """))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testGenerateFlashcards_InvalidJson_ReturnsBadRequest() throws Exception {
        // When & Then: Request with invalid JSON
        mockMvc.perform(post("/api/flashcards/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testTestEndpoint_ReturnsSuccess() throws Exception {
        // When & Then: Test the /test endpoint
        mockMvc.perform(get("/api/flashcards/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Flashcard API is working!"));
    }
    
    @Test
    void testGenerateFlashcards_LongStudyMaterial() throws Exception {
        // Given: Long study material
        String longMaterial = "Spring Boot provides auto-configuration for rapid development. ".repeat(50);
        
        List<Flashcard> mockFlashcards = List.of(
            new Flashcard("What does Spring Boot provide?", "Auto-configuration for rapid development")
        );
        
        when(flashcardService.generateFlashcards(eq(longMaterial), eq(1)))
            .thenReturn(mockFlashcards);
        
        // When & Then: Should handle long text
        mockMvc.perform(post("/api/flashcards/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                    {
                      "studyMaterial": "%s",
                      "count": 1
                    }
                    """, longMaterial)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flashcards", hasSize(1)));
    }
}