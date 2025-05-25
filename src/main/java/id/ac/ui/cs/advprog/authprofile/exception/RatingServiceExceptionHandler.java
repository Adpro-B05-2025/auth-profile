package id.ac.ui.cs.advprog.authprofile.exception;

import id.ac.ui.cs.advprog.authprofile.dto.response.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

/**
 * Additional exception handlers for rating service integration
 * Add these to your existing GlobalExceptionHandler class
 */
@RestControllerAdvice
public class RatingServiceExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RatingServiceExceptionHandler.class);

    @ExceptionHandler(RatingServiceUnavailableException.class)
    public ResponseEntity<MessageResponse> handleRatingServiceUnavailable(RatingServiceUnavailableException ex) {
        logger.warn("Rating service unavailable: {}", ex.getMessage());
        MessageResponse response = new MessageResponse(
                "Rating service is temporarily unavailable. Please try again later.",
                false
        );
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<MessageResponse> handleResourceAccessException(ResourceAccessException ex) {
        logger.error("Network error accessing rating service: {}", ex.getMessage());
        MessageResponse response = new MessageResponse(
                "Unable to connect to rating service. Please try again later.",
                false
        );
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<MessageResponse> handleRestClientException(RestClientException ex) {
        logger.error("Rating service client error: {}", ex.getMessage());
        MessageResponse response = new MessageResponse(
                "Error communicating with rating service.",
                false
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_GATEWAY);
    }
}

