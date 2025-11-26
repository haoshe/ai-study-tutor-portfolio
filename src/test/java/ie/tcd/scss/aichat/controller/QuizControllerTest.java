package ie.tcd.scss.aichat.controller;

import ie.tcd.scss.aichat.dto.QuizQuestion;
import ie.tcd.scss.aichat.model.User;
import ie.tcd.scss.aichat.repository.UserRepository;  // ADD THIS IMPORT
import ie.tcd.scss.aichat.service.AuthService;
import ie.tcd.scss.aichat.service.QuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;  // ADD THIS IMPORT

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")  
class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuizService quizService;

    @MockBean
    private AuthService authService;
    
    @MockBean  
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create a test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        
        // Set up authentication with the real User entity
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // ADD THIS: Mock the UserRepository to return the test user
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));
    }

    @Test
    void testGenerateQuiz_Success() throws Exception {
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

        when(quizService.generateQuiz(anyString(), eq(2), eq("medium"), eq(1L), anyString()))
            .thenReturn(mockQuestions);

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
        List<QuizQuestion> mockQuestions = List.of(
            new QuizQuestion(
                "What is dependency injection?",
                Arrays.asList("A design pattern", "A database", "A framework", "A language"),
                0,
                "Dependency injection is a design pattern."
            )
        );

        when(quizService.generateQuiz(anyString(), isNull(), eq("medium"), eq(1L), anyString()))
            .thenReturn(mockQuestions);

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
        List<QuizQuestion> mockQuestions = List.of(
            new QuizQuestion(
                "Test question?",
                Arrays.asList("A", "B", "C", "D"),
                0,
                "Explanation"
            )
        );

        when(quizService.generateQuiz(anyString(), eq(1), isNull(), eq(1L), anyString()))
            .thenReturn(mockQuestions);

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
        List<QuizQuestion> mockQuestions = List.of(
            new QuizQuestion("Easy question?", Arrays.asList("A", "B", "C", "D"), 0, "Easy")
        );

        when(quizService.generateQuiz(anyString(), eq(1), eq("easy"), eq(1L), anyString()))
            .thenReturn(mockQuestions);

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
        List<QuizQuestion> mockQuestions = List.of(
            new QuizQuestion("Hard question?", Arrays.asList("A", "B", "C", "D"), 0, "Hard")
        );

        when(quizService.generateQuiz(anyString(), eq(1), eq("hard"), eq(1L), anyString()))
            .thenReturn(mockQuestions);

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
        mockMvc.perform(post("/api/quiz/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testTestEndpoint_ReturnsSuccess() throws Exception {
        mockMvc.perform(get("/api/quiz/test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Quiz API is working!")));
    }

    @Test
    void testGenerateQuiz_LargeCount() throws Exception {
        List<QuizQuestion> mockQuestions = List.of(
            new QuizQuestion("Q1?", Arrays.asList("A", "B", "C", "D"), 0, "E1"),
            new QuizQuestion("Q2?", Arrays.asList("A", "B", "C", "D"), 1, "E2"),
            new QuizQuestion("Q3?", Arrays.asList("A", "B", "C", "D"), 2, "E3"),
            new QuizQuestion("Q4?", Arrays.asList("A", "B", "C", "D"), 3, "E4"),
            new QuizQuestion("Q5?", Arrays.asList("A", "B", "C", "D"), 0, "E5")
        );

        when(quizService.generateQuiz(anyString(), eq(10), any(), eq(1L), anyString()))
            .thenReturn(mockQuestions);

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