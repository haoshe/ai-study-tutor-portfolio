package ie.tcd.scss.aichat.service;

import ie.tcd.scss.aichat.dto.Flashcard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test for FlashcardService
 * Uses Mockito to mock the ChatClient and avoid real API calls
 */
@ExtendWith(MockitoExtension.class)
class FlashcardServiceTest {
    
    @Mock
    private ChatModel chatModel;
    
    private FlashcardService flashcardService;
    
    @BeforeEach
    void setUp() {
        // Create service with mocked ChatModel
        flashcardService = new FlashcardService(chatModel);
    }
    
    @Test
    void testGenerateFlashcards_Success() {
        // Given: Mock AI response with proper Q&A format
        String mockAiResponse = """
                Q: What is Spring Boot?
                A: An open-source Java framework for building production-ready applications
                
                Q: What does @Autowired do?
                A: It enables automatic dependency injection in Spring
                
                Q: What is Inversion of Control?
                A: A design principle where the framework controls object creation and lifecycle
                """;
        
        // Mock the ChatModel response
        when(chatModel.call(any(Prompt.class))).thenReturn(
            new ChatResponse(List.of(new Generation(new AssistantMessage(mockAiResponse))))
        );
        
        // When: Generate flashcards
        String studyMaterial = """
                Spring Boot is an open-source Java framework used to build production-ready applications.
                It uses dependency injection with @Autowired annotation.
                Spring follows the Inversion of Control principle.
                """;
        List<Flashcard> flashcards = flashcardService.generateFlashcards(studyMaterial, 3);
        
        // Then: Verify results
        assertNotNull(flashcards);
        assertEquals(3, flashcards.size());
        
        // Verify first flashcard
        Flashcard first = flashcards.get(0);
        assertEquals("What is Spring Boot?", first.getQuestion());
        assertEquals("An open-source Java framework for building production-ready applications", first.getAnswer());
        
        // Verify second flashcard
        Flashcard second = flashcards.get(1);
        assertEquals("What does @Autowired do?", second.getQuestion());
        assertEquals("It enables automatic dependency injection in Spring", second.getAnswer());
        
        // Verify third flashcard
        Flashcard third = flashcards.get(2);
        assertEquals("What is Inversion of Control?", third.getQuestion());
        assertEquals("A design principle where the framework controls object creation and lifecycle", third.getAnswer());
        
        // Verify the ChatModel was called exactly once
        verify(chatModel, times(1)).call(any(Prompt.class));
    }
    
    @Test
    void testGenerateFlashcards_WithNullCount_UsesDefault() {
        // Given: Mock AI response
        String mockAiResponse = """
                Q: Test question 1?
                A: Test answer 1
                
                Q: Test question 2?
                A: Test answer 2
                """;
        
        when(chatModel.call(any(Prompt.class))).thenReturn(
            new ChatResponse(List.of(new Generation(new AssistantMessage(mockAiResponse))))
        );
        
        // When: Generate with null count (should default to 5)
        List<Flashcard> flashcards = flashcardService.generateFlashcards("test material", null);
        
        // Then: Verify parsing works
        assertNotNull(flashcards);
        assertEquals(2, flashcards.size());
    }
    
    @Test
    void testGenerateFlashcards_WithZeroCount_UsesDefault() {
        // Given: Mock AI response
        String mockAiResponse = """
                Q: Question?
                A: Answer
                """;
        
        when(chatModel.call(any(Prompt.class))).thenReturn(
            new ChatResponse(List.of(new Generation(new AssistantMessage(mockAiResponse))))
        );
        
        // When: Generate with 0 count (should default to 5)
        List<Flashcard> flashcards = flashcardService.generateFlashcards("test", 0);
        
        // Then: Should still work
        assertNotNull(flashcards);
        assertEquals(1, flashcards.size());
    }
    
    @Test
    void testParseFlashcards_HandlesMultilineAnswers() {
        // Given: Mock AI response with multi-line answers
        String mockAiResponse = """
                Q: What is Spring MVC?
                A: Spring MVC is a web framework for building web applications.
                It follows the Model-View-Controller design pattern.
                
                Q: What is @RestController?
                A: An annotation that combines @Controller and @ResponseBody
                """;
        
        when(chatModel.call(any(Prompt.class))).thenReturn(
            new ChatResponse(List.of(new Generation(new AssistantMessage(mockAiResponse))))
        );
        
        // When
        List<Flashcard> flashcards = flashcardService.generateFlashcards("Spring MVC study material", 2);
        
        // Then: Should handle multi-line answers
        assertNotNull(flashcards);
        assertEquals(2, flashcards.size());
        
        Flashcard first = flashcards.get(0);
        assertTrue(first.getAnswer().contains("web framework"));
        assertTrue(first.getAnswer().contains("Model-View-Controller"));
    }
    
    @Test
    void testParseFlashcards_EmptyResponse_ReturnsEmptyList() {
        // Given: Empty AI response
        when(chatModel.call(any(Prompt.class))).thenReturn(
            new ChatResponse(List.of(new Generation(new AssistantMessage(""))))
        );
        
        // When
        List<Flashcard> flashcards = flashcardService.generateFlashcards("test", 1);
        
        // Then: Should return empty list, not crash
        assertNotNull(flashcards);
        assertTrue(flashcards.isEmpty());
    }
    
    @Test
    void testParseFlashcards_WithExtraWhitespace() {
        // Given: AI response with extra whitespace
        String mockAiResponse = """
                
                Q:   What is @Bean?   
                A:   An annotation that indicates a method produces a bean   
                
                
                Q:   What is ApplicationContext?   
                A:   The central interface for Spring IoC container   
                
                """;
        
        when(chatModel.call(any(Prompt.class))).thenReturn(
            new ChatResponse(List.of(new Generation(new AssistantMessage(mockAiResponse))))
        );
        
        // When
        List<Flashcard> flashcards = flashcardService.generateFlashcards("Spring annotations", 2);
        
        // Then: Should trim whitespace
        assertNotNull(flashcards);
        assertEquals(2, flashcards.size());
        
        assertEquals("What is @Bean?", flashcards.get(0).getQuestion());
        assertEquals("An annotation that indicates a method produces a bean", flashcards.get(0).getAnswer());
    }
}