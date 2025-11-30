package ie.tcd.scss.aichat.repository;

import ie.tcd.scss.aichat.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByUsername_ExistingUser_ReturnsUser() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword123");
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Act
        Optional<User> found = userRepository.findByUsername("testuser");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void testFindByUsername_NonExistingUser_ReturnsEmpty() {
        // Act
        Optional<User> found = userRepository.findByUsername("nonexistent");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByEmail_ExistingUser_ReturnsUser() {
        // Arrange
        User user = new User();
        user.setUsername("emailtest");
        user.setEmail("email@example.com");
        user.setPasswordHash("hashedPassword456");
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Act
        Optional<User> found = userRepository.findByEmail("email@example.com");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("emailtest", found.get().getUsername());
        assertEquals("email@example.com", found.get().getEmail());
    }

    @Test
    void testFindByEmail_NonExistingUser_ReturnsEmpty() {
        // Act
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByUsername_ExistingUser_ReturnsTrue() {
        // Arrange
        User user = new User();
        user.setUsername("existstest");
        user.setEmail("exists@example.com");
        user.setPasswordHash("password");
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Act
        boolean exists = userRepository.existsByUsername("existstest");

        // Assert
        assertTrue(exists);
    }

    @Test
    void testExistsByUsername_NonExistingUser_ReturnsFalse() {
        // Act
        boolean exists = userRepository.existsByUsername("doesnotexist");

        // Assert
        assertFalse(exists);
    }

    @Test
    void testExistsByEmail_ExistingUser_ReturnsTrue() {
        // Arrange
        User user = new User();
        user.setUsername("emailexists");
        user.setEmail("emailexists@example.com");
        user.setPasswordHash("password");
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Act
        boolean exists = userRepository.existsByEmail("emailexists@example.com");

        // Assert
        assertTrue(exists);
    }

    @Test
    void testExistsByEmail_NonExistingUser_ReturnsFalse() {
        // Act
        boolean exists = userRepository.existsByEmail("doesnotexist@example.com");

        // Assert
        assertFalse(exists);
    }

    @Test
    void testSave_NewUser_ReturnsUserWithId() {
        // Arrange
        User user = new User();
        user.setUsername("newuser");
        user.setEmail("newuser@example.com");
        user.setPasswordHash("hashedPassword789");
        user.setCreatedAt(LocalDateTime.now());

        // Act
        User saved = userRepository.save(user);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("newuser", saved.getUsername());
        assertEquals("newuser@example.com", saved.getEmail());
    }

    @Test
    void testSave_UpdateExistingUser_UpdatesSuccessfully() {
        // Arrange - Create initial user
        User user = new User();
        user.setUsername("updatetest");
        user.setEmail("update@example.com");
        user.setPasswordHash("oldPassword");
        user.setCreatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);

        // Act - Update user
        saved.setEmail("newemail@example.com");
        User updated = userRepository.save(saved);

        // Assert
        assertEquals(saved.getId(), updated.getId());
        assertEquals("newemail@example.com", updated.getEmail());
        assertEquals("updatetest", updated.getUsername());
    }
}
