package id.ac.ui.cs.advprog.authprofile.exception;

/**
 * Custom exception for rating service unavailability
 */
class RatingServiceUnavailableException extends RuntimeException {
    public RatingServiceUnavailableException(String message) {
        super(message);
    }

    public RatingServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}