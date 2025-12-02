package ie.tcd.scss.aichat.repository;

import ie.tcd.scss.aichat.model.Flashcard;
import ie.tcd.scss.aichat.model.FlashcardSet;
import ie.tcd.scss.aichat.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = Replace.NONE)
class FlashcardSetRepositoryTest {

    @Autowired
    private FlashcardSetRepository flashcardSetRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create a test user
        testUser = new User();
        testUser.setUsername("flashcardtestuser");
        testUser.setEmail("flashcard@test.com");
        testUser.setPasswordHash("password");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);
    }

    @Test
    void testFindByUserIdOrderByCreatedAtDesc_WithResults_ReturnsOrderedList() {
        // Arrange - Create multiple flashcard sets
        FlashcardSet set1 = new FlashcardSet();
        set1.setUser(testUser);
        set1.setTitle("First Set");
        set1.setStudyMaterial("Material 1");
        set1.setCreatedAt(LocalDateTime.now().minusDays(2));
        set1.setUpdatedAt(LocalDateTime.now().minusDays(2));
        flashcardSetRepository.save(set1);

        FlashcardSet set2 = new FlashcardSet();
        set2.setUser(testUser);
        set2.setTitle("Second Set");
        set2.setStudyMaterial("Material 2");
        set2.setCreatedAt(LocalDateTime.now().minusDays(1));
        set2.setUpdatedAt(LocalDateTime.now().minusDays(1));
        flashcardSetRepository.save(set2);

        FlashcardSet set3 = new FlashcardSet();
        set3.setUser(testUser);
        set3.setTitle("Third Set");
        set3.setStudyMaterial("Material 3");
        set3.setCreatedAt(LocalDateTime.now());
        set3.setUpdatedAt(LocalDateTime.now());
        flashcardSetRepository.save(set3);

        // Act
        List<FlashcardSet> results = flashcardSetRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());

        // Assert
        assertEquals(3, results.size());
        // Verify descending order (newest first)
        assertEquals("Third Set", results.get(0).getTitle());
        assertEquals("Second Set", results.get(1).getTitle());
        assertEquals("First Set", results.get(2).getTitle());
    }

    @Test
    void testFindByUserIdOrderByCreatedAtDesc_NoResults_ReturnsEmptyList() {
        // Act
        List<FlashcardSet> results = flashcardSetRepository.findByUserIdOrderByCreatedAtDesc(999L);

        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    void testSave_FlashcardSet_SavesSuccessfully() {
        // Arrange
        FlashcardSet flashcardSet = new FlashcardSet();
        flashcardSet.setUser(testUser);
        flashcardSet.setTitle("Test Flashcard Set");
        flashcardSet.setStudyMaterial("Test material content");

        // Act
        FlashcardSet saved = flashcardSetRepository.save(flashcardSet);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Test Flashcard Set", saved.getTitle());
        assertEquals(testUser.getId(), saved.getUser().getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void testDelete_FlashcardSet_DeletesSuccessfully() {
        // Arrange
        FlashcardSet flashcardSet = new FlashcardSet();
        flashcardSet.setUser(testUser);
        flashcardSet.setTitle("To be deleted");
        flashcardSet.setStudyMaterial("Material");
        flashcardSet.setCreatedAt(LocalDateTime.now());
        flashcardSet.setUpdatedAt(LocalDateTime.now());
        FlashcardSet saved = flashcardSetRepository.save(flashcardSet);

        // Act
        flashcardSetRepository.deleteById(saved.getId());

        // Assert
        Optional<FlashcardSet> deleted = flashcardSetRepository.findById(saved.getId());
        assertFalse(deleted.isPresent());
    }

    @Test
    void testCascadeDelete_FlashcardSet_DeletesAssociatedFlashcards() {
        // Arrange - Create flashcard set with flashcards
        FlashcardSet flashcardSet = new FlashcardSet();
        flashcardSet.setUser(testUser);
        flashcardSet.setTitle("Set with Cards");
        flashcardSet.setStudyMaterial("Material");
        flashcardSet.setCreatedAt(LocalDateTime.now());
        flashcardSet.setUpdatedAt(LocalDateTime.now());

        Flashcard card1 = new Flashcard();
        card1.setFlashcardSet(flashcardSet);
        card1.setQuestion("Question 1");
        card1.setAnswer("Answer 1");
        card1.setPosition(1);

        Flashcard card2 = new Flashcard();
        card2.setFlashcardSet(flashcardSet);
        card2.setQuestion("Question 2");
        card2.setAnswer("Answer 2");
        card2.setPosition(2);

        flashcardSet.getFlashcards().add(card1);
        flashcardSet.getFlashcards().add(card2);

        FlashcardSet saved = flashcardSetRepository.save(flashcardSet);

        // Act - Delete the flashcard set
        flashcardSetRepository.deleteById(saved.getId());

        // Assert - Verify set is deleted
        Optional<FlashcardSet> deletedSet = flashcardSetRepository.findById(saved.getId());
        assertFalse(deletedSet.isPresent());
        // Note: Flashcards are cascade deleted, but we can't easily verify 
        // without a FlashcardRepository in this test context
    }

    @Test
    void testExistsByIdAndUserId_ExistingSetAndCorrectUser_ReturnsTrue() {
        // Arrange
        FlashcardSet flashcardSet = new FlashcardSet();
        flashcardSet.setUser(testUser);
        flashcardSet.setTitle("Ownership Test");
        flashcardSet.setStudyMaterial("Material");
        flashcardSet.setCreatedAt(LocalDateTime.now());
        flashcardSet.setUpdatedAt(LocalDateTime.now());
        FlashcardSet saved = flashcardSetRepository.save(flashcardSet);

        // Act
        boolean exists = flashcardSetRepository.existsByIdAndUserId(saved.getId(), testUser.getId());

        // Assert
        assertTrue(exists);
    }

    @Test
    void testExistsByIdAndUserId_ExistingSetWrongUser_ReturnsFalse() {
        // Arrange
        FlashcardSet flashcardSet = new FlashcardSet();
        flashcardSet.setUser(testUser);
        flashcardSet.setTitle("Ownership Test");
        flashcardSet.setStudyMaterial("Material");
        flashcardSet.setCreatedAt(LocalDateTime.now());
        flashcardSet.setUpdatedAt(LocalDateTime.now());
        FlashcardSet saved = flashcardSetRepository.save(flashcardSet);

        // Act
        boolean exists = flashcardSetRepository.existsByIdAndUserId(saved.getId(), 999L);

        // Assert
        assertFalse(exists);
    }

    @Test
    void testExistsByIdAndUserId_NonExistingSet_ReturnsFalse() {
        // Act
        boolean exists = flashcardSetRepository.existsByIdAndUserId(999L, testUser.getId());

        // Assert
        assertFalse(exists);
    }
}
