package id.ac.ui.cs.advprog.authprofile.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

class UnauthorizedExceptionTest {

    @Test
    void testExceptionMessage() {
        String message = "Test error message";
        UnauthorizedException exception = new UnauthorizedException(message);

        assertEquals(message, exception.getMessage(), "Exception message should match");
    }

    @Test
    void testResponseStatusAnnotation() {
        ResponseStatus annotation = UnauthorizedException.class.getAnnotation(ResponseStatus.class);

        assertNotNull(annotation, "Class should have ResponseStatus annotation");
        assertEquals(HttpStatus.FORBIDDEN, annotation.value(), "ResponseStatus value should be FORBIDDEN");
    }
}