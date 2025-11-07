package ie.tcd.scss.aichat.controller;

import ie.tcd.scss.aichat.dto.QuizQuestion;
import ie.tcd.scss.aichat.service.QuizService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration test for QuizController
 * Uses MockMvc to test REST API without starting full server
 */
@WebMvcTest(QuizController.class)
class QuizControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private QuizService quizService;
    
    @Test
    void testGenerateQuiz_Success() throws Exception {
        // Given: Mock service response
        List<QuizQuestion> mockQuestions = Arrays.asList(
            new QuizQuestion(
                "What is Spring Boot?",
                Arrays.asList(
                    "An open-source Java framework",
                    "A JavaScript library",
                    "A database system",
                    "A CSS preprocessor"
                ),
                0,
                "Spring Boot is an open-source Java framework for building production-ready applications."
            ),
            new QuizQuestion(
                "What does @Autowired do?",
                Arrays.asList(
                    "Enables automatic dependency injection",
                    "Creates REST endpoints",
                    "Configures database connections",
                    "Handles HTTP requests"
                ),
                0,
                "The @Autowired annotation enables automatic dependency injection in Spring."
            )
        );
        
        when(quizService.generateQuiz(anyString(), eq(2), eq("medium")))
            .thenReturn(mockQuestions);
        
        // When & Then: Make POST request and verify response
        mockMvc.perform(post("/api/quiz/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "studyMaterial": "Spring Boot is an open-source Java framework...",
                      "count": 2,
                      "difficulty": "medium"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].question", is("What is Spring Boot?")))
                .andExpect(jsonPath("$[0].options", hasSize(4)))
                .andExpect(jsonPath("$[0].correctAnswer", is(0)))
                .andExpect(jsonPath("$[0].explanation", containsString("Spring Boot")))
                .andExpect(jsonPath("$[1].question", is("What does @Autowired do?")))
                .andExpect(jsonPath("$[1].correctAnswer", is(0)));
    }
    
    @Test
    void testGenerateQuiz_WithoutCount() throws Exception {
        // Given: Mock service response with default count
        List<QuizQuestion> mockQuestions = List.of(
            new QuizQuestion(
                "What is dependency injection?",
                Arrays.asList("A design pattern", "A database", "A framework", "A language"),
                0,
                "Dependency injection is a design pattern."
            )
        );
        
        when(quizService.generateQuiz(anyString(), isNull(), eq("medium")))
            .thenReturn(mockQuestions);
        
        // When & Then: Request without count field
        mockMvc.perform(post("/api/quiz/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "studyMaterial": "Test material",
                      "difficulty": "medium"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
    
    @Test
    void testGenerateQuiz_WithoutDifficulty() throws Exception {
        // Given: Mock service response with default difficulty
        List<QuizQuestion> mockQuestions = List.of(
            new QuizQuestion(
                "Test question?",
                Arrays.asList("A", "B", "C", "D"),
                0,
                "Explanation"
            )
        );
        
        when(quizService.generateQuiz(anyString(), eq(1), isNull()))
            .thenReturn(mockQuestions);
        
        // When & Then: Request without difficulty field
        mockMvc.perform(post("/api/quiz/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "studyMaterial": "Test material",
                      "count": 1
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
    
    @Test
    void testGenerateQuiz_WithEasyDifficulty() throws Exception {
        // Given: Mock service response
        List<QuizQuestion> mockQuestions = List.of(
            new QuizQuestion("Easy question?", Arrays.asList("A", "B", "C", "D"), 0, "Easy")
        );
        
        when(quizService.generateQuiz(anyString(), eq(1), eq("easy")))
            .thenReturn(mockQuestions);
        
        // When & Then
        mockMvc.perform(post("/api/quiz/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "studyMaterial": "Test",
                      "count": 1,
                      "difficulty": "easy"
                    }
                    """))
                .andExpect(status().isOk());
    }
    
    @Test
    void testGenerateQuiz_WithHardDifficulty() throws Exception {
        // Given: Mock service response
        List<QuizQuestion> mockQuestions = List.of(
            new QuizQuestion("Hard question?", Arrays.asList("A", "B", "C", "D"), 0, "Hard")
        );
        
        when(quizService.generateQuiz(anyString(), eq(1), eq("hard")))
            .thenReturn(mockQuestions);
        
        // When & Then
        mockMvc.perform(post("/api/quiz/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "studyMaterial": "Test",
                      "count": 1,
                      "difficulty": "hard"
                    }
                    """))
                .andExpect(status().isOk());
    }
    
    @Test
    void testGenerateQuiz_EmptyStudyMaterial_ReturnsBadRequest() throws Exception {
        // When & Then: Request with empty study material
        mockMvc.perform(post("/api/quiz/generate")
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
    void testGenerateQuiz_NullStudyMaterial_ReturnsBadRequest() throws Exception {
        // When & Then: Request without study material field
        mockMvc.perform(post("/api/quiz/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "count": 3
                    }
                    """))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testGenerateQuiz_WhitespaceOnlyStudyMaterial_ReturnsBadRequest() throws Exception {
        // When & Then: Request with only whitespace
        mockMvc.perform(post("/api/quiz/generate")
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
    void testGenerateQuiz_InvalidDifficulty_ReturnsBadRequest() throws Exception {
        // When & Then: Request with invalid difficulty
        mockMvc.perform(post("/api/quiz/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "studyMaterial": "Test material",
                      "count": 3,
                      "difficulty": "impossible"
                    }
                    """))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testGenerateQuiz_InvalidJson_ReturnsBadRequest() throws Exception {
        // When & Then: Request with invalid JSON
        mockMvc.perform(post("/api/quiz/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testTestEndpoint_ReturnsSuccess() throws Exception {
        // When & Then: Test the /test endpoint
        mockMvc.perform(get("/api/quiz/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Quiz API is working!"));
    }
    
    @Test
    void testGenerateQuiz_LargeCount() throws Exception {
        // Given: Mock service response for large count
        List<QuizQuestion> mockQuestions = List.of(
            new QuizQuestion("Q1?", Arrays.asList("A", "B", "C", "D"), 0, "E1"),
            new QuizQuestion("Q2?", Arrays.asList("A", "B", "C", "D"), 1, "E2"),
            new QuizQuestion("Q3?", Arrays.asList("A", "B", "C", "D"), 2, "E3"),
            new QuizQuestion("Q4?", Arrays.asList("A", "B", "C", "D"), 3, "E4"),
            new QuizQuestion("Q5?", Arrays.asList("A", "B", "C", "D"), 0, "E5")
        );
        
        when(quizService.generateQuiz(anyString(), eq(10), any()))
            .thenReturn(mockQuestions);
        
        // When & Then: Should handle large count
        mockMvc.perform(post("/api/quiz/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "studyMaterial": "Spring Boot provides auto-configuration...",
                      "count": 10
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));
    }
}