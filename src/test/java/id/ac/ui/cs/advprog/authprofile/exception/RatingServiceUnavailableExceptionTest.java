package id.ac.ui.cs.advprog.authprofile.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RatingServiceUnavailableException Tests")
class RatingServiceUnavailableExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create exception with message only")
        void shouldCreateExceptionWithMessageOnly() {
            // Given
            String expectedMessage = "Rating service is currently unavailable";

            // When
            RatingServiceUnavailableException exception = new RatingServiceUnavailableException(expectedMessage);

            // Then
            assertNotNull(exception);
            assertEquals(expectedMessage, exception.getMessage());
            assertNull(exception.getCause());
            assertTrue(exception instanceof RuntimeException);
        }

        @Test
        @DisplayName("Should create exception with null message")
        void shouldCreateExceptionWithNullMessage() {
            // Given
            String expectedMessage = null;

            // When
            RatingServiceUnavailableException exception = new RatingServiceUnavailableException(expectedMessage);

            // Then
            assertNotNull(exception);
            assertNull(exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with empty message")
        void shouldCreateExceptionWithEmptyMessage() {
            // Given
            String expectedMessage = "";

            // When
            RatingServiceUnavailableException exception = new RatingServiceUnavailableException(expectedMessage);

            // Then
            assertNotNull(exception);
            assertEquals(expectedMessage, exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            // Given
            String expectedMessage = "Failed to connect to rating service";
            Throwable expectedCause = new RuntimeException("Connection timeout");

            // When
            RatingServiceUnavailableException exception = new RatingServiceUnavailableException(expectedMessage, expectedCause);

            // Then
            assertNotNull(exception);
            assertEquals(expectedMessage, exception.getMessage());
            assertEquals(expectedCause, exception.getCause());
            assertTrue(exception instanceof RuntimeException);
        }

        @Test
        @DisplayName("Should create exception with null message and valid cause")
        void shouldCreateExceptionWithNullMessageAndValidCause() {
            // Given
            String expectedMessage = null;
            Throwable expectedCause = new IllegalStateException("Service down");

            // When
            RatingServiceUnavailableException exception = new RatingServiceUnavailableException(expectedMessage, expectedCause);

            // Then
            assertNotNull(exception);
            assertNull(exception.getMessage());
            assertEquals(expectedCause, exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with valid message and null cause")
        void shouldCreateExceptionWithValidMessageAndNullCause() {
            // Given
            String expectedMessage = "Rating service unavailable";
            Throwable expectedCause = null;

            // When
            RatingServiceUnavailableException exception = new RatingServiceUnavailableException(expectedMessage, expectedCause);

            // Then
            assertNotNull(exception);
            assertEquals(expectedMessage, exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with both null message and cause")
        void shouldCreateExceptionWithBothNullMessageAndCause() {
            // Given
            String expectedMessage = null;
            Throwable expectedCause = null;

            // When
            RatingServiceUnavailableException exception = new RatingServiceUnavailableException(expectedMessage, expectedCause);

            // Then
            assertNotNull(exception);
            assertNull(exception.getMessage());
            assertNull(exception.getCause());
        }
    }

    @Nested
    @DisplayName("Exception Behavior Tests")
    class ExceptionBehaviorTests {

        @Test
        @DisplayName("Should be throwable")
        void shouldBeThrowable() {
            // Given
            String message = "Rating service is down";
            RatingServiceUnavailableException exception = new RatingServiceUnavailableException(message);

            // When & Then
            assertThrows(RatingServiceUnavailableException.class, () -> {
                throw exception;
            });
        }

        @Test
        @DisplayName("Should maintain stack trace")
        void shouldMaintainStackTrace() {
            // Given
            String message = "Rating service connection failed";

            // When
            RatingServiceUnavailableException exception = new RatingServiceUnavailableException(message);

            // Then
            assertNotNull(exception.getStackTrace());
            assertTrue(exception.getStackTrace().length > 0);
        }

        @Test
        @DisplayName("Should preserve cause stack trace")
        void shouldPreserveCauseStackTrace() {
            // Given
            String message = "Rating service unavailable";
            RuntimeException cause = new RuntimeException("Network error");

            // When
            RatingServiceUnavailableException exception = new RatingServiceUnavailableException(message, cause);

            // Then
            assertNotNull(exception.getCause());
            assertEquals(cause, exception.getCause());
            assertNotNull(exception.getCause().getStackTrace());
            assertTrue(exception.getCause().getStackTrace().length > 0);
        }

        @Test
        @DisplayName("Should be instance of RuntimeException")
        void shouldBeInstanceOfRuntimeException() {
            // Given
            RatingServiceUnavailableException exception = new RatingServiceUnavailableException("Test message");

            // Then
            assertInstanceOf(RuntimeException.class, exception);
            assertInstanceOf(Exception.class, exception);
            assertInstanceOf(Throwable.class, exception);
        }
    }

    @Nested
    @DisplayName("Real-world Usage Scenarios")
    class RealWorldUsageTests {

        @Test
        @DisplayName("Should handle connection timeout scenario")
        void shouldHandleConnectionTimeoutScenario() {
            // Given
            String message = "Rating service connection timed out after 5 seconds";
            Throwable cause = new java.net.SocketTimeoutException("Connection timeout");

            // When
            RatingServiceUnavailableException exception = new RatingServiceUnavailableException(message, cause);

            // Then
            assertEquals(message, exception.getMessage());
            assertInstanceOf(java.net.SocketTimeoutException.class, exception.getCause());
            assertEquals("Connection timeout", exception.getCause().getMessage());
        }

        @Test
        @DisplayName("Should handle service down scenario")
        void shouldHandleServiceDownScenario() {
            // Given
            String message = "Rating service is temporarily unavailable for maintenance";

            // When
            RatingServiceUnavailableException exception = new RatingServiceUnavailableException(message);

            // Then
            assertEquals(message, exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should handle HTTP error scenario")
        void shouldHandleHttpErrorScenario() {
            // Given
            String message = "Rating service returned HTTP 503 - Service Unavailable";
            RuntimeException cause = new RuntimeException("HTTP 503 Service Unavailable");

            // When
            RatingServiceUnavailableException exception = new RatingServiceUnavailableException(message, cause);

            // Then
            assertEquals(message, exception.getMessage());
            assertEquals(cause, exception.getCause());
            assertEquals("HTTP 503 Service Unavailable", exception.getCause().getMessage());
        }

        @Test
        @DisplayName("Should handle circuit breaker scenario")
        void shouldHandleCircuitBreakerScenario() {
            // Given
            String message = "Rating service circuit breaker is open - too many failures detected";
            Exception cause = new IllegalStateException("Circuit breaker open");

            // When
            RatingServiceUnavailableException exception = new RatingServiceUnavailableException(message, cause);

            // Then
            assertEquals(message, exception.getMessage());
            assertInstanceOf(IllegalStateException.class, exception.getCause());
            assertEquals("Circuit breaker open", exception.getCause().getMessage());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very long message")
        void shouldHandleVeryLongMessage() {
            // Given
            StringBuilder longMessage = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                longMessage.append("Rating service is unavailable. ");
            }
            String expectedMessage = longMessage.toString();

            // When
            RatingServiceUnavailableException exception = new RatingServiceUnavailableException(expectedMessage);

            // Then
            assertEquals(expectedMessage, exception.getMessage());
            assertTrue(exception.getMessage().length() > 30000);
        }

        @Test
        @DisplayName("Should handle special characters in message")
        void shouldHandleSpecialCharactersInMessage() {
            // Given
            String messageWithSpecialChars = "Rating service unavailable: áéíóú ñ çü 中文 日本語 한국어 🚨⚠️❌";

            // When
            RatingServiceUnavailableException exception = new RatingServiceUnavailableException(messageWithSpecialChars);

            // Then
            assertEquals(messageWithSpecialChars, exception.getMessage());
        }

        @Test
        @DisplayName("Should handle nested cause chain")
        void shouldHandleNestedCauseChain() {
            // Given
            RuntimeException rootCause = new RuntimeException("Root network error");
            IllegalStateException intermediateCause = new IllegalStateException("Service state error", rootCause);
            String message = "Rating service chain failure";

            // When
            RatingServiceUnavailableException exception = new RatingServiceUnavailableException(message, intermediateCause);

            // Then
            assertEquals(message, exception.getMessage());
            assertEquals(intermediateCause, exception.getCause());
            assertEquals(rootCause, exception.getCause().getCause());
            assertEquals("Root network error", exception.getCause().getCause().getMessage());
        }
    }

    @Nested
    @DisplayName("Equality and String Representation")
    class EqualityAndStringTests {

        @Test
        @DisplayName("Should have meaningful toString representation")
        void shouldHaveMeaningfulToStringRepresentation() {
            // Given
            String message = "Rating service is unavailable";
            RatingServiceUnavailableException exception = new RatingServiceUnavailableException(message);

            // When
            String toString = exception.toString();

            // Then
            assertNotNull(toString);
            assertTrue(toString.contains("RatingServiceUnavailableException"));
            assertTrue(toString.contains(message));
        }

        @Test
        @DisplayName("Should handle toString with cause")
        void shouldHandleToStringWithCause() {
            // Given
            String message = "Rating service failed";
            RuntimeException cause = new RuntimeException("Connection error");
            RatingServiceUnavailableException exception = new RatingServiceUnavailableException(message, cause);

            // When
            String toString = exception.toString();

            // Then
            assertNotNull(toString);
            assertTrue(toString.contains("RatingServiceUnavailableException"));
            assertTrue(toString.contains(message));
        }

        @Test
        @DisplayName("Should handle toString with null message")
        void shouldHandleToStringWithNullMessage() {
            // Given
            RatingServiceUnavailableException exception = new RatingServiceUnavailableException(null);

            // When
            String toString = exception.toString();

            // Then
            assertNotNull(toString);
            assertTrue(toString.contains("RatingServiceUnavailableException"));
        }
    }
}