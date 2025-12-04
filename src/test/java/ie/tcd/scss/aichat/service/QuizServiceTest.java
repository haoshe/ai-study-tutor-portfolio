package ie.tcd.scss.aichat.service;

import ie.tcd.scss.aichat.dto.QuizQuestion;
import ie.tcd.scss.aichat.model.User;
import ie.tcd.scss.aichat.repository.QuizSetRepository;
import ie.tcd.scss.aichat.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test for QuizService
 * Uses Mockito to mock the ChatClient and avoid real API calls
 */
@ExtendWith(MockitoExtension.class)
class QuizServiceTest {
    
    @Mock
    private ChatModel chatModel;
    
    @Mock
    private QuizSetRepository quizSetRepository;
    
    @Mock
    private UserRepository userRepository;
    
    private QuizService quizService;
    
    @BeforeEach
    void setUp() {
        quizService = new QuizService(chatModel, quizSetRepository, userRepository);
        
        // Mock user repository to return a test user
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        // Mock repository saves
        lenient().when(quizSetRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }
    
    @Test
    void testGenerateQuiz_Success() {
        // Given: Mock AI response with proper quiz format
        String mockAiResponse = """
                Q: What is Spring Boot?
                A: An open-source Java framework for building production-ready applications
                B: A JavaScript testing framework
                C: A CSS preprocessor
                D: A database management system
                CORRECT: A
                EXPLAIN: Spring Boot is an open-source Java framework that simplifies the development of production-ready applications.
                
                Q: What does @Autowired do?
                A: Enables automatic dependency injection
                B: Creates REST endpoints
                C: Configures the database
                D: Handles HTTP requests
                CORRECT: A
                EXPLAIN: The @Autowired annotation enables automatic dependency injection in Spring.
                
                Q: What does @RestController combine?
                A: @Controller and @ResponseBody
                B: @Service and @Component
                C: @Repository and @Entity
                D: @Bean and @Configuration
                CORRECT: A
                EXPLAIN: @RestController is a convenience annotation that combines @Controller and @ResponseBody.
                """;
        
        when(chatModel.call(any(Prompt.class))).thenReturn(
            new ChatResponse(List.of(new Generation(new AssistantMessage(mockAiResponse))))
        );
        
        // When: Generate quiz
        String studyMaterial = "Spring Boot is an open-source Java framework...";
        List<QuizQuestion> questions = quizService.generateQuiz(studyMaterial, 3, "medium", 1L, "Spring Boot Quiz");
        
        // Then: Verify results
        assertNotNull(questions);
        assertEquals(3, questions.size());
        
        // Verify first question
        QuizQuestion first = questions.get(0);
        assertEquals("What is Spring Boot?", first.getQuestion());
        assertEquals(4, first.getOptions().size());
        assertEquals("An open-source Java framework for building production-ready applications", first.getOptions().get(0));
        assertEquals(0, first.getCorrectAnswer());
        assertTrue(first.getExplanation().contains("Spring Boot"));
        
        // Verify second question
        QuizQuestion second = questions.get(1);
        assertEquals("What does @Autowired do?", second.getQuestion());
        assertEquals(0, second.getCorrectAnswer());
        
        // Verify third question
        QuizQuestion third = questions.get(2);
        assertEquals("What does @RestController combine?", third.getQuestion());
        assertEquals(0, third.getCorrectAnswer());
        
        // Verify the ChatModel was called exactly once
        verify(chatModel, times(1)).call(any(Prompt.class));
    }
    
    @Test
    void testGenerateQuiz_WithNullCount_UsesDefault() {
        // Given: Mock AI response
        String mockAiResponse = """
                Q: What is dependency injection?
                A: A design pattern for managing dependencies
                B: A database query language
                C: A testing framework
                D: A build tool
                CORRECT: A
                EXPLAIN: Dependency injection is a design pattern used in Spring.
                """;
        
        when(chatModel.call(any(Prompt.class))).thenReturn(
            new ChatResponse(List.of(new Generation(new AssistantMessage(mockAiResponse))))
        );
        
        // When: Generate with null count (should default to 5)
        List<QuizQuestion> questions = quizService.generateQuiz("test material", null, "medium", 1L, "Test Quiz");
        
        // Then: Should work and return parsed questions
        assertNotNull(questions);
        assertTrue(questions.size() >= 1);
    }
    
    @Test
    void testGenerateQuiz_WithNullDifficulty_UsesDefault() {
        // Given: Mock AI response
        String mockAiResponse = """
                Q: What is Spring?
                A: A Java framework
                B: A season
                C: A water source
                D: A coil
                CORRECT: A
                EXPLAIN: Spring is a comprehensive Java framework.
                """;
        
        when(chatModel.call(any(Prompt.class))).thenReturn(
            new ChatResponse(List.of(new Generation(new AssistantMessage(mockAiResponse))))
        );
        
        // When: Generate with null difficulty (should default to "medium")
        List<QuizQuestion> questions = quizService.generateQuiz("test", 1, null, 1L, "Test Quiz");
        
        // Then: Should work
        assertNotNull(questions);
        assertEquals(1, questions.size());
    }
    
    @Test
    void testGenerateQuiz_WithDifferentDifficulties() {
        // Given: Mock AI response
        String mockAiResponse = """
                Q: Test question?
                A: Answer A
                B: Answer B
                C: Answer C
                D: Answer D
                CORRECT: B
                EXPLAIN: Test explanation
                """;
        
        when(chatModel.call(any(Prompt.class))).thenReturn(
            new ChatResponse(List.of(new Generation(new AssistantMessage(mockAiResponse))))
        );
        
        // When: Test each difficulty level
        List<QuizQuestion> easyQuiz = quizService.generateQuiz("test", 1, "easy", 1L, "Easy Quiz");
        List<QuizQuestion> mediumQuiz = quizService.generateQuiz("test", 1, "medium", 1L, "Medium Quiz");
        List<QuizQuestion> hardQuiz = quizService.generateQuiz("test", 1, "hard", 1L, "Hard Quiz");
        
        // Then: All should work
        assertNotNull(easyQuiz);
        assertNotNull(mediumQuiz);
        assertNotNull(hardQuiz);
        assertEquals(1, easyQuiz.size());
        assertEquals(1, mediumQuiz.size());
        assertEquals(1, hardQuiz.size());
    }
    
    @Test
    void testParseQuizQuestions_WithDifferentCorrectAnswers() {
        // Given: Mock AI response with different correct answers (B, C, D)
        String mockAiResponse = """
                Q: Question with B correct?
                A: Wrong answer
                B: Correct answer
                C: Wrong answer
                D: Wrong answer
                CORRECT: B
                EXPLAIN: B is correct
                
                Q: Question with C correct?
                A: Wrong answer
                B: Wrong answer
                C: Correct answer
                D: Wrong answer
                CORRECT: C
                EXPLAIN: C is correct
                
                Q: Question with D correct?
                A: Wrong answer
                B: Wrong answer
                C: Wrong answer
                D: Correct answer
                CORRECT: D
                EXPLAIN: D is correct
                """;
        
        when(chatModel.call(any(Prompt.class))).thenReturn(
            new ChatResponse(List.of(new Generation(new AssistantMessage(mockAiResponse))))
        );
        
        // When
        List<QuizQuestion> questions = quizService.generateQuiz("test", 3, "medium", 1L, "Test Quiz");
        
        // Then: Verify correct answer indices
        assertNotNull(questions);
        assertEquals(3, questions.size());
        assertEquals(1, questions.get(0).getCorrectAnswer()); // B = index 1
        assertEquals(2, questions.get(1).getCorrectAnswer()); // C = index 2
        assertEquals(3, questions.get(2).getCorrectAnswer()); // D = index 3
    }
    
    @Test
    void testParseQuizQuestions_EmptyResponse_ReturnsEmptyList() {
        // Given: Empty AI response
        when(chatModel.call(any(Prompt.class))).thenReturn(
            new ChatResponse(List.of(new Generation(new AssistantMessage(""))))
        );
        
        // When
        List<QuizQuestion> questions = quizService.generateQuiz("test", 1, "medium", 1L, "Test Quiz");
        
        // Then: Should return empty list, not crash
        assertNotNull(questions);
        assertTrue(questions.isEmpty());
    }
    
    @Test
    void testParseQuizQuestions_WithExtraWhitespace() {
        // Given: AI response with extra whitespace
        String mockAiResponse = """
                
                Q:   What is Spring Boot?   
                A:   A Java framework   
                B:   A JavaScript library   
                C:   A database   
                D:   A CSS framework   
                CORRECT:   A   
                EXPLAIN:   Spring Boot is a Java framework   
                
                """;
        
        when(chatModel.call(any(Prompt.class))).thenReturn(
            new ChatResponse(List.of(new Generation(new AssistantMessage(mockAiResponse))))
        );
        
        // When
        List<QuizQuestion> questions = quizService.generateQuiz("test", 1, "medium", 1L, "Test Quiz");
        
        // Then: Should trim whitespace properly
        assertNotNull(questions);
        assertEquals(1, questions.size());
        assertEquals("What is Spring Boot?", questions.get(0).getQuestion());
        assertEquals("A Java framework", questions.get(0).getOptions().get(0));
    }
}
