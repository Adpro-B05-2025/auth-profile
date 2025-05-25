package id.ac.ui.cs.advprog.authprofile.exception;

import id.ac.ui.cs.advprog.authprofile.dto.response.MessageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceExceptionHandlerTest {

    @InjectMocks
    private RatingServiceExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        // Setup is handled by @InjectMocks
    }

    @Test
    void constructor_ShouldInitializeCorrectly() {
        // When creating a new instance
        RatingServiceExceptionHandler handler = new RatingServiceExceptionHandler();

        // Then it should be initialized without throwing exceptions
        assertNotNull(handler);
    }

    @Test
    void handleRatingServiceUnavailable_WithValidException_ShouldReturnServiceUnavailable() {
        // Given
        String errorMessage = "Rating service is down for maintenance";
        RatingServiceUnavailableException exception = new RatingServiceUnavailableException(errorMessage);

        // When
        ResponseEntity<MessageResponse> response = exceptionHandler.handleRatingServiceUnavailable(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());

        MessageResponse messageResponse = response.getBody();
        assertEquals("Rating service is temporarily unavailable. Please try again later.",
                messageResponse.getMessage());
        assertFalse(messageResponse.isSuccess());
    }

    @Test
    void handleRatingServiceUnavailable_WithNullMessage_ShouldReturnServiceUnavailable() {
        // Given
        RatingServiceUnavailableException exception = new RatingServiceUnavailableException(null);

        // When
        ResponseEntity<MessageResponse> response = exceptionHandler.handleRatingServiceUnavailable(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());

        MessageResponse messageResponse = response.getBody();
        assertEquals("Rating service is temporarily unavailable. Please try again later.",
                messageResponse.getMessage());
        assertFalse(messageResponse.isSuccess());
    }

    @Test
    void handleRatingServiceUnavailable_WithEmptyMessage_ShouldReturnServiceUnavailable() {
        // Given
        RatingServiceUnavailableException exception = new RatingServiceUnavailableException("");

        // When
        ResponseEntity<MessageResponse> response = exceptionHandler.handleRatingServiceUnavailable(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());

        MessageResponse messageResponse = response.getBody();
        assertEquals("Rating service is temporarily unavailable. Please try again later.",
                messageResponse.getMessage());
        assertFalse(messageResponse.isSuccess());
    }

    @Test
    void handleResourceAccessException_WithValidException_ShouldReturnServiceUnavailable() {
        // Given
        String errorMessage = "Connection timeout to rating service";
        ResourceAccessException exception = new ResourceAccessException(errorMessage);

        // When
        ResponseEntity<MessageResponse> response = exceptionHandler.handleResourceAccessException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());

        MessageResponse messageResponse = response.getBody();
        assertEquals("Unable to connect to rating service. Please try again later.",
                messageResponse.getMessage());
        assertFalse(messageResponse.isSuccess());
    }

    @Test
    void handleResourceAccessException_WithNetworkError_ShouldReturnServiceUnavailable() {
        // Given
        String errorMessage = "Network is unreachable";
        ResourceAccessException exception = new ResourceAccessException(errorMessage,
                new java.net.ConnectException("Connection refused"));

        // When
        ResponseEntity<MessageResponse> response = exceptionHandler.handleResourceAccessException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());

        MessageResponse messageResponse = response.getBody();
        assertEquals("Unable to connect to rating service. Please try again later.",
                messageResponse.getMessage());
        assertFalse(messageResponse.isSuccess());
    }

    @Test
    void handleResourceAccessException_WithNullMessage_ShouldReturnServiceUnavailable() {
        // Given
        ResourceAccessException exception = new ResourceAccessException(null);

        // When
        ResponseEntity<MessageResponse> response = exceptionHandler.handleResourceAccessException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());

        MessageResponse messageResponse = response.getBody();
        assertEquals("Unable to connect to rating service. Please try again later.",
                messageResponse.getMessage());
        assertFalse(messageResponse.isSuccess());
    }

    @Test
    void handleRestClientException_WithValidException_ShouldReturnBadGateway() {
        // Given
        String errorMessage = "HTTP 500 Internal Server Error";
        RestClientException exception = new RestClientException(errorMessage);

        // When
        ResponseEntity<MessageResponse> response = exceptionHandler.handleRestClientException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());

        MessageResponse messageResponse = response.getBody();
        assertEquals("Error communicating with rating service.",
                messageResponse.getMessage());
        assertFalse(messageResponse.isSuccess());
    }

    @Test
    void handleRestClientException_WithHttpClientError_ShouldReturnBadGateway() {
        // Given
        String errorMessage = "400 Bad Request from rating service";
        RestClientException exception = new RestClientException(errorMessage);

        // When
        ResponseEntity<MessageResponse> response = exceptionHandler.handleRestClientException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());

        MessageResponse messageResponse = response.getBody();
        assertEquals("Error communicating with rating service.",
                messageResponse.getMessage());
        assertFalse(messageResponse.isSuccess());
    }

    @Test
    void handleRestClientException_WithNullMessage_ShouldReturnBadGateway() {
        // Given
        RestClientException exception = new RestClientException(null);

        // When
        ResponseEntity<MessageResponse> response = exceptionHandler.handleRestClientException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());

        MessageResponse messageResponse = response.getBody();
        assertEquals("Error communicating with rating service.",
                messageResponse.getMessage());
        assertFalse(messageResponse.isSuccess());
    }

    @Test
    void handleRestClientException_WithEmptyMessage_ShouldReturnBadGateway() {
        // Given
        RestClientException exception = new RestClientException("");

        // When
        ResponseEntity<MessageResponse> response = exceptionHandler.handleRestClientException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());

        MessageResponse messageResponse = response.getBody();
        assertEquals("Error communicating with rating service.",
                messageResponse.getMessage());
        assertFalse(messageResponse.isSuccess());
    }

    @Test
    void allHandlers_ShouldReturnConsistentMessageResponseStructure() {
        // Given
        RatingServiceUnavailableException ratingException =
                new RatingServiceUnavailableException("Service down");
        ResourceAccessException resourceException =
                new ResourceAccessException("Connection failed");
        RestClientException clientException =
                new RestClientException("Client error");

        // When
        ResponseEntity<MessageResponse> ratingResponse =
                exceptionHandler.handleRatingServiceUnavailable(ratingException);
        ResponseEntity<MessageResponse> resourceResponse =
                exceptionHandler.handleResourceAccessException(resourceException);
        ResponseEntity<MessageResponse> clientResponse =
                exceptionHandler.handleRestClientException(clientException);

        // Then - All responses should have consistent structure
        assertNotNull(ratingResponse.getBody());
        assertNotNull(resourceResponse.getBody());
        assertNotNull(clientResponse.getBody());

        // All should have success = false
        assertFalse(ratingResponse.getBody().isSuccess());
        assertFalse(resourceResponse.getBody().isSuccess());
        assertFalse(clientResponse.getBody().isSuccess());

        // All should have non-null, non-empty messages
        assertNotNull(ratingResponse.getBody().getMessage());
        assertNotNull(resourceResponse.getBody().getMessage());
        assertNotNull(clientResponse.getBody().getMessage());

        assertFalse(ratingResponse.getBody().getMessage().isEmpty());
        assertFalse(resourceResponse.getBody().getMessage().isEmpty());
        assertFalse(clientResponse.getBody().getMessage().isEmpty());
    }

    @Test
    void exceptionHandlers_ShouldReturnDifferentHttpStatusCodes() {
        // Given
        RatingServiceUnavailableException ratingException =
                new RatingServiceUnavailableException("Service down");
        ResourceAccessException resourceException =
                new ResourceAccessException("Connection failed");
        RestClientException clientException =
                new RestClientException("Client error");

        // When
        ResponseEntity<MessageResponse> ratingResponse =
                exceptionHandler.handleRatingServiceUnavailable(ratingException);
        ResponseEntity<MessageResponse> resourceResponse =
                exceptionHandler.handleResourceAccessException(resourceException);
        ResponseEntity<MessageResponse> clientResponse =
                exceptionHandler.handleRestClientException(clientException);

        // Then - Each should return appropriate HTTP status
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ratingResponse.getStatusCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, resourceResponse.getStatusCode());
        assertEquals(HttpStatus.BAD_GATEWAY, clientResponse.getStatusCode());
    }

    @Test
    void exceptionHandlers_ShouldReturnUserFriendlyMessages() {
        // Given
        RatingServiceUnavailableException ratingException =
                new RatingServiceUnavailableException("Internal service error with stack trace...");
        ResourceAccessException resourceException =
                new ResourceAccessException("java.net.ConnectException: Connection refused (Connection refused)");
        RestClientException clientException =
                new RestClientException("org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 Internal Server Error");

        // When
        ResponseEntity<MessageResponse> ratingResponse =
                exceptionHandler.handleRatingServiceUnavailable(ratingException);
        ResponseEntity<MessageResponse> resourceResponse =
                exceptionHandler.handleResourceAccessException(resourceException);
        ResponseEntity<MessageResponse> clientResponse =
                exceptionHandler.handleRestClientException(clientException);

        // Then - All should return user-friendly messages, not technical details
        String[] userFriendlyMessages = {
                ratingResponse.getBody().getMessage(),
                resourceResponse.getBody().getMessage(),
                clientResponse.getBody().getMessage()
        };

        for (String message : userFriendlyMessages) {
            assertFalse(message.contains("java."), "Message should not contain Java class names");
            assertFalse(message.contains("Exception"), "Message should not contain 'Exception'");
            assertFalse(message.contains("stack trace"), "Message should not contain technical terms");
            assertTrue(message.length() > 10, "Message should be descriptive");
        }
    }

    @Test
    void messageResponses_ShouldHaveConsistentFailureFlag() {
        // Given
        RatingServiceUnavailableException ratingException =
                new RatingServiceUnavailableException("Service down");
        ResourceAccessException resourceException =
                new ResourceAccessException("Connection failed");
        RestClientException clientException =
                new RestClientException("Client error");

        // When
        MessageResponse ratingMessage =
                exceptionHandler.handleRatingServiceUnavailable(ratingException).getBody();
        MessageResponse resourceMessage =
                exceptionHandler.handleResourceAccessException(resourceException).getBody();
        MessageResponse clientMessage =
                exceptionHandler.handleRestClientException(clientException).getBody();

        // Then - All should consistently indicate failure
        assertNotNull(ratingMessage);
        assertNotNull(resourceMessage);
        assertNotNull(clientMessage);

        assertFalse(ratingMessage.isSuccess());
        assertFalse(resourceMessage.isSuccess());
        assertFalse(clientMessage.isSuccess());
    }

    @Test
    void exceptionHandlers_ShouldHandleNestedExceptions() {
        // Given
        java.io.IOException rootCause = new java.net.SocketTimeoutException("Read timed out");
        ResourceAccessException resourceException =
                new ResourceAccessException("I/O error on GET request", rootCause);

        RestClientException clientException =
                new RestClientException("Request processing failed",
                        new RuntimeException("Underlying service error"));

        // When
        ResponseEntity<MessageResponse> resourceResponse =
                exceptionHandler.handleResourceAccessException(resourceException);
        ResponseEntity<MessageResponse> clientResponse =
                exceptionHandler.handleRestClientException(clientException);

        // Then - Should handle gracefully without exposing nested exception details
        assertNotNull(resourceResponse);
        assertNotNull(clientResponse);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, resourceResponse.getStatusCode());
        assertEquals(HttpStatus.BAD_GATEWAY, clientResponse.getStatusCode());

        // Messages should still be user-friendly
        assertFalse(resourceResponse.getBody().getMessage().contains("SocketTimeoutException"));
        assertFalse(clientResponse.getBody().getMessage().contains("RuntimeException"));
    }
}