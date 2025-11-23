package ie.tcd.scss.aichat.service;

import ie.tcd.scss.aichat.dto.AuthResponse;
import ie.tcd.scss.aichat.dto.LoginRequest;
import ie.tcd.scss.aichat.dto.RegisterRequest;
import ie.tcd.scss.aichat.model.User;
import ie.tcd.scss.aichat.repository.UserRepository;
import ie.tcd.scss.aichat.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 * Tests user registration, login, password hashing, and token generation
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(10);
    }

    @Test
    void testRegister_Success() {
        // Given
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");
        
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(jwtUtil.generateToken("testuser")).thenReturn("mock-jwt-token");
        
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        savedUser.setEmail("test@example.com");
        savedUser.setPasswordHash("hashed-password");
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        AuthResponse response = authService.register(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("mock-jwt-token", response.getToken());
        
        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtUtil, times(1)).generateToken("testuser");
    }

    @Test
    void testRegister_DuplicateUsername_ThrowsException() {
        // Given
        RegisterRequest request = new RegisterRequest("existinguser", "new@example.com", "password123");
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.register(request)
        );
        
        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, times(1)).existsByUsername("existinguser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_DuplicateEmail_ThrowsException() {
        // Given
        RegisterRequest request = new RegisterRequest("newuser", "existing@example.com", "password123");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.register(request)
        );
        
        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, times(1)).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_PasswordIsHashed() {
        // Given
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "plainPassword");
        
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(jwtUtil.generateToken(anyString())).thenReturn("token");
        
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        savedUser.setEmail("test@example.com");
        
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            savedUser.setPasswordHash(user.getPasswordHash());
            return savedUser;
        });

        // When
        authService.register(request);

        // Then
        verify(userRepository).save(argThat(user -> {
            // Verify password is hashed (BCrypt format starts with $2a$)
            assertTrue(user.getPasswordHash().startsWith("$2a$10$"));
            // Verify password is not stored in plain text
            assertNotEquals("plainPassword", user.getPasswordHash());
            return true;
        }));
    }

    @Test
    void testLogin_Success() {
        // Given
        String rawPassword = "password123";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        
        LoginRequest request = new LoginRequest("testuser", rawPassword);
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash(hashedPassword);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("testuser")).thenReturn("mock-jwt-token");

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("mock-jwt-token", response.getToken());
        
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(jwtUtil, times(1)).generateToken("testuser");
    }

    @Test
    void testLogin_UserNotFound_ThrowsException() {
        // Given
        LoginRequest request = new LoginRequest("nonexistent", "password123");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.login(request)
        );
        
        assertEquals("Invalid credentials", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("nonexistent");
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void testLogin_WrongPassword_ThrowsException() {
        // Given
        String correctPassword = "correctPassword";
        String hashedPassword = passwordEncoder.encode(correctPassword);
        
        LoginRequest request = new LoginRequest("testuser", "wrongPassword");
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPasswordHash(hashedPassword);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.login(request)
        );
        
        assertEquals("Invalid credentials", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void testGetUserFromToken_Success() {
        // Given
        String token = "valid-token";
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        
        when(jwtUtil.extractUsername(token)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        User result = authService.getUserFromToken(token);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(jwtUtil, times(1)).extractUsername(token);
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testGetUserFromToken_UserNotFound_ThrowsException() {
        // Given
        String token = "valid-token";
        when(jwtUtil.extractUsername(token)).thenReturn("nonexistent");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.getUserFromToken(token)
        );
        
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testValidateToken_ValidToken_ReturnsTrue() {
        // Given
        String token = "valid-token";
        when(jwtUtil.extractUsername(token)).thenReturn("testuser");
        when(jwtUtil.validateToken(token, "testuser")).thenReturn(true);

        // When
        boolean isValid = authService.validateToken(token);

        // Then
        assertTrue(isValid);
        verify(jwtUtil, times(1)).extractUsername(token);
        verify(jwtUtil, times(1)).validateToken(token, "testuser");
    }

    @Test
    void testValidateToken_InvalidToken_ReturnsFalse() {
        // Given
        String token = "invalid-token";
        when(jwtUtil.extractUsername(token)).thenThrow(new RuntimeException("Invalid token"));

        // When
        boolean isValid = authService.validateToken(token);

        // Then
        assertFalse(isValid);
        verify(jwtUtil, times(1)).extractUsername(token);
    }

    @Test
    void testValidateToken_ExpiredToken_ReturnsFalse() {
        // Given
        String token = "expired-token";
        when(jwtUtil.extractUsername(token)).thenReturn("testuser");
        when(jwtUtil.validateToken(token, "testuser")).thenReturn(false);

        // When
        boolean isValid = authService.validateToken(token);

        // Then
        assertFalse(isValid);
    }
}
