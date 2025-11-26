package ie.tcd.scss.aichat.controller;

import ie.tcd.scss.aichat.dto.Flashcard;
import ie.tcd.scss.aichat.model.User;
import ie.tcd.scss.aichat.repository.UserRepository;
import ie.tcd.scss.aichat.service.AuthService;
import ie.tcd.scss.aichat.service.FlashcardService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")  
class FlashcardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlashcardService flashcardService;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    
    UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(testUser, null, List.of());
    SecurityContextHolder.getContext().setAuthentication(authentication);
    
    // Add this
    when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(testUser));
    when(userRepository.findById(1L))
            .thenReturn(Optional.of(testUser));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGenerateFlashcards_Success() throws Exception {
        List<Flashcard> mockFlashcards = List.of(
            new Flashcard("What is Spring Boot?", "An open-source Java framework for building production-ready applications"),
            new Flashcard("What does @Autowired do?", "It enables automatic dependency injection in Spring"),
            new Flashcard("What is Inversion of Control?", "A design principle where the framework controls object creation and lifecycle")
        );

        when(flashcardService.generateFlashcards(anyString(), eq(3), eq(1L), anyString()))
            .thenReturn(mockFlashcards);

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
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].question", is("What is Spring Boot?")))
                .andExpect(jsonPath("$[0].answer", is("An open-source Java framework for building production-ready applications")))
                .andExpect(jsonPath("$[1].question", is("What does @Autowired do?")))
                .andExpect(jsonPath("$[1].answer", is("It enables automatic dependency injection in Spring")))
                .andExpect(jsonPath("$[2].question", is("What is Inversion of Control?")))
                .andExpect(jsonPath("$[2].answer", is("A design principle where the framework controls object creation and lifecycle")));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGenerateFlashcards_WithNullCount() throws Exception {
        List<Flashcard> mockFlashcards = List.of(
            new Flashcard("Question 1?", "Answer 1"),
            new Flashcard("Question 2?", "Answer 2")
        );

        when(flashcardService.generateFlashcards(anyString(), eq(null), eq(1L), anyString()))
            .thenReturn(mockFlashcards);

        mockMvc.perform(post("/api/flashcards/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "studyMaterial": "Test material"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGenerateFlashcards_EmptyStudyMaterial_ReturnsBadRequest() throws Exception {
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
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGenerateFlashcards_NullStudyMaterial_ReturnsBadRequest() throws Exception {
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
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGenerateFlashcards_WithWhitespaceOnly_ReturnsBadRequest() throws Exception {
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
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGenerateFlashcards_InvalidJson_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/flashcards/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testTestEndpoint_ReturnsSuccess() throws Exception {
        mockMvc.perform(get("/api/flashcards/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Flashcard API is working!"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGenerateFlashcards_LongStudyMaterial() throws Exception {
        String longMaterial = "Spring Boot provides auto-configuration for rapid development. ".repeat(50);

        List<Flashcard> mockFlashcards = List.of(
            new Flashcard("What does Spring Boot provide?", "Auto-configuration for rapid development")
        );

        when(flashcardService.generateFlashcards(eq(longMaterial), eq(1), eq(1L), anyString()))
            .thenReturn(mockFlashcards);

        mockMvc.perform(post("/api/flashcards/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                    {
                      "studyMaterial": "%s",
                      "count": 1
                    }
                    """, longMaterial)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
