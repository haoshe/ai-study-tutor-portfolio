package ie.tcd.scss.aichat.controller;

import ie.tcd.scss.aichat.dto.AuthResponse;
import ie.tcd.scss.aichat.dto.LoginRequest;
import ie.tcd.scss.aichat.dto.RegisterRequest;
import ie.tcd.scss.aichat.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthController
 * Tests HTTP endpoints for user registration and login
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void testRegister_Success() {
        // Given
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");
        AuthResponse expectedResponse = new AuthResponse(1L, "testuser", "test@example.com", "mock-jwt-token");
        
        when(authService.register(any(RegisterRequest.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<?> response = authController.register(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof AuthResponse);
        
        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals(1L, authResponse.getId());
        assertEquals("testuser", authResponse.getUsername());
        assertEquals("test@example.com", authResponse.getEmail());
        assertEquals("mock-jwt-token", authResponse.getToken());
        
        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void testRegister_DuplicateUsername_Returns400() {
        // Given
        RegisterRequest request = new RegisterRequest("existinguser", "new@example.com", "password123");
        when(authService.register(any(RegisterRequest.class)))
            .thenThrow(new IllegalArgumentException("Username already exists"));

        // When
        ResponseEntity<?> response = authController.register(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> errorResponse = (Map<String, Object>) response.getBody();
        assertEquals(400, errorResponse.get("status"));
        assertEquals("Username already exists", errorResponse.get("message"));
        
        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void testRegister_DuplicateEmail_Returns400() {
        // Given
        RegisterRequest request = new RegisterRequest("newuser", "existing@example.com", "password123");
        when(authService.register(any(RegisterRequest.class)))
            .thenThrow(new IllegalArgumentException("Email already exists"));

        // When
        ResponseEntity<?> response = authController.register(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> errorResponse = (Map<String, Object>) response.getBody();
        assertEquals(400, errorResponse.get("status"));
        assertEquals("Email already exists", errorResponse.get("message"));
    }

    @Test
    void testRegister_InternalError_Returns500() {
        // Given
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");
        when(authService.register(any(RegisterRequest.class)))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When
        ResponseEntity<?> response = authController.register(request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> errorResponse = (Map<String, Object>) response.getBody();
        assertEquals(500, errorResponse.get("status"));
        assertTrue(errorResponse.get("message").toString().contains("Registration failed"));
    }

    @Test
    void testLogin_Success() {
        // Given
        LoginRequest request = new LoginRequest("testuser", "password123");
        AuthResponse expectedResponse = new AuthResponse(1L, "testuser", "test@example.com", "mock-jwt-token");
        
        when(authService.login(any(LoginRequest.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<?> response = authController.login(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof AuthResponse);
        
        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals(1L, authResponse.getId());
        assertEquals("testuser", authResponse.getUsername());
        assertEquals("mock-jwt-token", authResponse.getToken());
        
        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void testLogin_InvalidCredentials_Returns401() {
        // Given
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new IllegalArgumentException("Invalid credentials"));

        // When
        ResponseEntity<?> response = authController.login(request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> errorResponse = (Map<String, Object>) response.getBody();
        assertEquals(401, errorResponse.get("status"));
        assertEquals("Invalid credentials", errorResponse.get("message"));
        
        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void testLogin_UserNotFound_Returns401() {
        // Given
        LoginRequest request = new LoginRequest("nonexistent", "password123");
        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new IllegalArgumentException("Invalid credentials"));

        // When
        ResponseEntity<?> response = authController.login(request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> errorResponse = (Map<String, Object>) response.getBody();
        assertEquals(401, errorResponse.get("status"));
        assertEquals("Invalid credentials", errorResponse.get("message"));
    }

    @Test
    void testLogin_InternalError_Returns500() {
        // Given
        LoginRequest request = new LoginRequest("testuser", "password123");
        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<?> response = authController.login(request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> errorResponse = (Map<String, Object>) response.getBody();
        assertEquals(500, errorResponse.get("status"));
        assertTrue(errorResponse.get("message").toString().contains("Login failed"));
    }

    @Test
    void testRegister_WithValidData_CallsService() {
        // Given
        RegisterRequest request = new RegisterRequest("newuser", "new@example.com", "SecurePass123!");
        AuthResponse expectedResponse = new AuthResponse(2L, "newuser", "new@example.com", "token");
        when(authService.register(request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<?> response = authController.register(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService, times(1)).register(request);
    }

    @Test
    void testLogin_WithValidData_CallsService() {
        // Given
        LoginRequest request = new LoginRequest("existinguser", "password");
        AuthResponse expectedResponse = new AuthResponse(1L, "existinguser", "user@example.com", "token");
        when(authService.login(request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<?> response = authController.login(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService, times(1)).login(request);
    }
}
