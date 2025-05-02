package id.ac.ui.cs.advprog.authprofile.exception;

import id.ac.ui.cs.advprog.authprofile.dto.response.MessageResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleResourceNotFoundException() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found");

        // Act
        ResponseEntity<MessageResponse> response = exceptionHandler.handleResourceNotFoundException(exception);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Resource not found", response.getBody().getMessage());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void handleEntityNotFoundException() {
        // Arrange
        EntityNotFoundException exception = new EntityNotFoundException("Entity not found");

        // Act
        ResponseEntity<MessageResponse> response = exceptionHandler.handleEntityNotFoundException(exception);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Entity not found", response.getBody().getMessage());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void handleEmailAlreadyExistsException() {
        // Arrange
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException("Email already exists");

        // Act
        ResponseEntity<MessageResponse> response = exceptionHandler.handleEmailAlreadyExistsException(exception);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email already exists", response.getBody().getMessage());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void handleBadCredentialsException() {
        // Arrange
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        // Act
        ResponseEntity<MessageResponse> response = exceptionHandler.handleBadCredentialsException(exception);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password", response.getBody().getMessage());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void handleUsernameNotFoundException() {
        // Arrange
        UsernameNotFoundException exception = new UsernameNotFoundException("User not found");

        // Act
        ResponseEntity<MessageResponse> response = exceptionHandler.handleUsernameNotFoundException(exception);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody().getMessage());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void handleMethodArgumentNotValidException() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "email", "Email is required");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(java.util.Collections.singletonList(fieldError));

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationExceptions(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email is required", response.getBody().get("email"));
    }

    @Test
    void handleConstraintViolationException() {
        // Arrange
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Email format is invalid");
        violations.add(violation);

        ConstraintViolationException exception = new ConstraintViolationException("Validation failed", violations);

        // Act
        ResponseEntity<MessageResponse> response = exceptionHandler.handleConstraintViolationException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Validation error"));
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void handleGlobalException() {
        // Arrange
        Exception exception = new RuntimeException("Unexpected error");

        // Act
        ResponseEntity<MessageResponse> response = exceptionHandler.handleGlobalException(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("An unexpected error occurred"));
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void handleUnauthorizedException() {
        String errorMessage = "User not authorized";
        UnauthorizedException exception = new UnauthorizedException(errorMessage);

        ResponseEntity<MessageResponse> response = exceptionHandler.handleUnauthorizedException(exception);

        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "Status code should be FORBIDDEN");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(errorMessage, response.getBody().getMessage(), "Error message should match");
        assertFalse(response.getBody().isSuccess(), "Success flag should be false");
    }
}