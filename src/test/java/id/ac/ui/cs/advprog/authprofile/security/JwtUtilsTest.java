package id.ac.ui.cs.advprog.authprofile.security;

import id.ac.ui.cs.advprog.authprofile.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    private final String jwtSecret = "pandaCareTestSecretKey123456789012345678901234567890";
    private final int jwtExpirationMs = 3600000; // 1 hour

    @BeforeEach
    public void setup() {
        // Set the required fields using ReflectionTestUtils
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", jwtExpirationMs);
    }

    @Test
    public void testGenerateJwtToken() {
        // Arrange
        String email = "test@example.com";
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(email);

        // Act
        String token = jwtUtils.generateJwtToken(authentication);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    public void testGetUserNameFromJwtToken() {
        // Arrange
        String email = "test@example.com";
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(email);

        String token = jwtUtils.generateJwtToken(authentication);

        // Act
        String extractedEmail = jwtUtils.getUserNameFromJwtToken(token);

        // Assert
        assertEquals(email, extractedEmail);
    }

    @Test
    public void testValidateJwtToken_ValidToken() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        String token = jwtUtils.generateJwtToken(authentication);

        // Act
        boolean isValid = jwtUtils.validateJwtToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    public void testValidateJwtToken_MalformedToken() {
        // Act - No mock setup needed as this doesn't use the mocks
        boolean isValid = jwtUtils.validateJwtToken("malformed.jwt.token");

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testValidateJwtToken_ExpiredToken() throws Exception {
        // Arrange - Create a JwtUtils with a very short expiration time
        JwtUtils shortExpirationJwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(shortExpirationJwtUtils, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(shortExpirationJwtUtils, "jwtExpirationMs", 1); // 1 millisecond expiration

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        String token = shortExpirationJwtUtils.generateJwtToken(authentication);

        // Wait for token to expire
        Thread.sleep(10);

        // Act
        boolean isValid = shortExpirationJwtUtils.validateJwtToken(token);

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testValidateJwtToken_EmptyToken() {
        // Act - No mock setup needed as this doesn't use the mocks
        boolean isValid = jwtUtils.validateJwtToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testValidateJwtToken_NullToken() {
        // Act - No mock setup needed as this doesn't use the mocks
        boolean isValid = jwtUtils.validateJwtToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testValidateJwtToken_UnsupportedToken() {
        // Based on the UnsupportedJwtException definition, this exception is thrown when
        // the JWT format doesn't match what's expected by the application

        // For example, if the app expects a signed JWT but receives an unsigned/plaintext JWT,
        // or if it uses an algorithm that the parser doesn't support

        // Let's create a token that uses an unsupported algorithm (RS256) when the application
        // is configured for HS256
        String unsupportedToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9" + // header with RS256 algorithm
                ".eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ" + // payload
                ".RSASHA256Signature"; // invalid signature for demo

        // Act
        boolean isValid = jwtUtils.validateJwtToken(unsupportedToken);

        // Assert
        assertFalse(isValid);

        // Additional verification: the method should return false for other invalid tokens too
        assertFalse(jwtUtils.validateJwtToken("completely.invalid.token"));
    }
}