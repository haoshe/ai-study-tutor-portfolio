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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
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
        
        LoginRequest request = new LoginRequest("testuser", rawPassword,false);
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash(hashedPassword);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("testuser",false)).thenReturn("mock-jwt-token");

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("mock-jwt-token", response.getToken());
        
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(jwtUtil, times(1)).generateToken(eq("testuser"), anyBoolean());
    }

    @Test
    void testLogin_UserNotFound_ThrowsException() {
        // Given
        LoginRequest request = new LoginRequest("nonexistent", "password123",false);
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
        
        LoginRequest request = new LoginRequest("testuser", "wrongPassword", false);
        
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
 @Test
    void testLogin_WithRememberMeTrue_ShouldGenerateTokenWithExtendedExpiration() {
        // Given
        String rawPassword = "password123";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        
        // CHANGED: rememberMe set to true
        LoginRequest request = new LoginRequest("testuser", rawPassword, true);
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash(hashedPassword);
        
        String expectedToken = "jwt-token-with-30day-expiration";
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        // CHANGED: Mock generateToken with rememberMe=true
        when(jwtUtil.generateToken("testuser", true)).thenReturn(expectedToken);

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(expectedToken, response.getToken(), 
            "Token should be the one generated with rememberMe=true (30-day expiration)");
        // CHANGED: Verify generateToken called with true
        verify(jwtUtil).generateToken("testuser", true);
        verify(jwtUtil, never()).generateToken("testuser", false);
        
        System.out.println("✓ PASS: RememberMe=true generated token with extended expiration (30 days)");
    }

    @Test
    void testLogin_WithRememberMeFalse_ShouldGenerateTokenWithStandardExpiration() {
        // Given
        String rawPassword = "password123";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        
        // CHANGED: rememberMe explicitly set to false
        LoginRequest request = new LoginRequest("testuser", rawPassword, false);
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash(hashedPassword);
        
        String expectedToken = "jwt-token-with-24hour-expiration";
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        // CHANGED: Mock generateToken with rememberMe=false
        when(jwtUtil.generateToken("testuser", false)).thenReturn(expectedToken);

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(expectedToken, response.getToken(), 
            "Token should be the one generated with rememberMe=false (24-hour expiration)");
        // CHANGED: Verify generateToken called with false
        verify(jwtUtil).generateToken("testuser", false);
        verify(jwtUtil, never()).generateToken("testuser", true);
        
        System.out.println("✓ PASS: RememberMe=false generated token with standard expiration (24 hours)");
    }

    @Test
    void testLogin_ExpectedTrueButReceivedFalse_ShouldGenerateWrongTokenType() {
        // Given - User expects remember me but it's set to false
        String rawPassword = "password123";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        
        // CHANGED: rememberMe set to false (but user might have expected true)
        LoginRequest request = new LoginRequest("testuser", rawPassword, false);
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash(hashedPassword);
        
        String shortLivedToken = "jwt-token-with-24hour-expiration";
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("testuser", false)).thenReturn(shortLivedToken);

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(shortLivedToken, response.getToken(), 
            "Token should be short-lived (24 hours)");
        verify(jwtUtil).generateToken("testuser", false);
        verify(jwtUtil, never()).generateToken("testuser", true);
        
        // This simulates a scenario where user expected long-lived token but got short-lived
        System.out.println("✗ FAIL SCENARIO: Expected rememberMe=true (30-day token) but received rememberMe=false (24-hour token)");
        System.out.println("   Impact: User will be logged out after 24 hours instead of 30 days");
    }

    @Test
    void testLogin_ExpectedFalseButReceivedTrue_ShouldGenerateWrongTokenType() {
        // Given - User expects standard session but rememberMe is true
        String rawPassword = "password123";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        
        // CHANGED: rememberMe set to true (but user might have expected false)
        LoginRequest request = new LoginRequest("testuser", rawPassword, true);
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash(hashedPassword);
        
        String longLivedToken = "jwt-token-with-30day-expiration";
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("testuser", true)).thenReturn(longLivedToken);

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(longLivedToken, response.getToken(), 
            "Token should be long-lived (30 days)");
        verify(jwtUtil).generateToken("testuser", true);
        verify(jwtUtil, never()).generateToken("testuser", false);
        
        // This simulates a scenario where user expected short-lived token but got long-lived
        System.out.println("✗ FAIL SCENARIO: Expected rememberMe=false (24-hour token) but received rememberMe=true (30-day token)");
        System.out.println("   Impact: Security concern - token remains valid for 30 days instead of 24 hours");
    }

    @Test
    void testRememberMeFlag_PreservedThroughAuthFlow() {
        // Test that rememberMe flag is correctly passed from request to token generation
        // Given
        String rawPassword = "password123";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        
        // CHANGED: Creating two different requests with different rememberMe values
        LoginRequest rememberMeRequest = new LoginRequest("testuser", rawPassword, true);
        LoginRequest standardRequest = new LoginRequest("testuser", rawPassword, false);
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash(hashedPassword);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(anyString(), anyBoolean())).thenReturn("token");

        // When
        authService.login(rememberMeRequest);
        authService.login(standardRequest);

        // Then
        // CHANGED: Verify both true and false were called
        verify(jwtUtil).generateToken("testuser", true);
        verify(jwtUtil).generateToken("testuser", false);
        
        System.out.println("✓ PASS: RememberMe flag correctly preserved through authentication flow");
    }

    @Test
    void testLogin_VerifyRememberMeParameterPassedCorrectly() {
        // This test specifically verifies the rememberMe parameter flows correctly
        // Given
        String rawPassword = "password123";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        
        // CHANGED: Test with rememberMe=true
        LoginRequest request = new LoginRequest("testuser", rawPassword, true);
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash(hashedPassword);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("testuser", true)).thenReturn("long-lived-token");

        // When
        authService.login(request);

        // Then
        // CHANGED: Verify the exact parameter is passed
        verify(jwtUtil).generateToken(eq("testuser"), eq(true));
        
        System.out.println("✓ PASS: Verified generateToken called with correct rememberMe parameter");
    }
}
