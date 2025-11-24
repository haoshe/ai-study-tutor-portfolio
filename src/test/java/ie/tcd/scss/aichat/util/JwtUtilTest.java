package ie.tcd.scss.aichat.util;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtil
 * Tests JWT token generation, validation, and extraction
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void testGenerateToken_Success() {
        // Given
        String username = "testuser";

        // When
        String token = jwtUtil.generateToken(username);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        // JWT tokens have 3 parts separated by dots
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void testExtractUsername_Success() {
        // Given
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        // When
        String extractedUsername = jwtUtil.extractUsername(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    void testExtractExpiration_Success() {
        // Given
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        // When
        Date expiration = jwtUtil.extractExpiration(token);

        // Then
        assertNotNull(expiration);
        // Token should expire in the future (24 hours from now)
        assertTrue(expiration.after(new Date()));
        // Token should expire within 25 hours from now (24h + 1h buffer)
        long oneHourFromNow = System.currentTimeMillis() + (25 * 60 * 60 * 1000);
        assertTrue(expiration.before(new Date(oneHourFromNow)));
    }

    @Test
    void testValidateToken_ValidToken_ReturnsTrue() {
        // Given
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        // When
        Boolean isValid = jwtUtil.validateToken(token, username);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_WrongUsername_ReturnsFalse() {
        // Given
        String username = "testuser";
        String token = jwtUtil.generateToken(username);
        String differentUsername = "differentuser";

        // When
        Boolean isValid = jwtUtil.validateToken(token, differentUsername);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_InvalidToken_ThrowsException() {
        // Given
        String invalidToken = "invalid.token.here";
        String username = "testuser";

        // When & Then
        assertThrows(Exception.class, () -> {
            jwtUtil.validateToken(invalidToken, username);
        });
    }

    @Test
    void testExtractUsername_InvalidToken_ThrowsException() {
        // Given
        String invalidToken = "not-a-valid-token";

        // When & Then
        assertThrows(Exception.class, () -> {
            jwtUtil.extractUsername(invalidToken);
        });
    }

    @Test
    void testGenerateToken_DifferentUsers_GenerateDifferentTokens() {
        // Given
        String user1 = "user1";
        String user2 = "user2";

        // When
        String token1 = jwtUtil.generateToken(user1);
        String token2 = jwtUtil.generateToken(user2);

        // Then
        assertNotEquals(token1, token2);
        assertEquals(user1, jwtUtil.extractUsername(token1));
        assertEquals(user2, jwtUtil.extractUsername(token2));
    }

    @Test
    void testGenerateToken_SameUser_GeneratesDifferentTokens() throws InterruptedException {
        // Given
        String username = "testuser";

        // When
        // Add delay to ensure different timestamps (JWT uses seconds, not milliseconds)
        String token1 = jwtUtil.generateToken(username);
        Thread.sleep(1001); // 1 second delay to ensure different timestamps
        String token2 = jwtUtil.generateToken(username);

        // Then
        // Tokens should be different due to different issuedAt timestamps
        assertNotEquals(token1, token2);
        // But both should be valid for the same username
        assertTrue(jwtUtil.validateToken(token1, username));
        assertTrue(jwtUtil.validateToken(token2, username));
    }

    @Test
    void testTokenFormat_HasCorrectStructure() {
        // Given
        String username = "testuser";

        // When
        String token = jwtUtil.generateToken(username);

        // Then
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT should have 3 parts: header, payload, signature");
        
        // Each part should be non-empty
        assertTrue(parts[0].length() > 0, "Header should not be empty");
        assertTrue(parts[1].length() > 0, "Payload should not be empty");
        assertTrue(parts[2].length() > 0, "Signature should not be empty");
    }

    @Test
    void testExtractExpiration_TokenNotExpired() {
        // Given
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        // When
        Date expiration = jwtUtil.extractExpiration(token);
        Date now = new Date();

        // Then
        assertTrue(expiration.after(now), "Token should not be expired yet");
    }

    @Test
    void testValidateToken_WithEmptyUsername_ReturnsFalse() {
        // Given
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        // When
        Boolean isValid = jwtUtil.validateToken(token, "");

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_WithNullUsername_ReturnsFalse() {
        // Given
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        // When
        Boolean isValid = jwtUtil.validateToken(token, null);

        // Then
        // Should return false for null username, not throw exception
        assertFalse(isValid);
    }
}
