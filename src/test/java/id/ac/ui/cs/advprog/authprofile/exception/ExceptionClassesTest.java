package id.ac.ui.cs.advprog.authprofile.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExceptionClassesTest {

    @Test
    void testResourceNotFoundException() {
        // Arrange
        String errorMessage = "Resource not found message";

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(errorMessage);

        // Assert
        assertEquals(errorMessage, exception.getMessage());

        // Verify correct annotation
        Annotation annotation = ResourceNotFoundException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(annotation);
        assertEquals(HttpStatus.NOT_FOUND, ((ResponseStatus) annotation).value());
    }

    @Test
    void testEmailAlreadyExistsException() {
        // Arrange
        String errorMessage = "Email already exists message";

        // Act
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException(errorMessage);

        // Assert
        assertEquals(errorMessage, exception.getMessage());

        // Verify correct annotation
        Annotation annotation = EmailAlreadyExistsException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(annotation);
        assertEquals(HttpStatus.CONFLICT, ((ResponseStatus) annotation).value());
    }

    @Test
    void testSerialVersionUIDExists() {
        // This test verifies that our exceptions have serialVersionUID defined

        try {
            ResourceNotFoundException.class.getDeclaredField("serialVersionUID");
            EmailAlreadyExistsException.class.getDeclaredField("serialVersionUID");
        } catch (NoSuchFieldException e) {
            throw new AssertionError("serialVersionUID field is missing in one of the exception classes", e);
        }
    }
}