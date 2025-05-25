package id.ac.ui.cs.advprog.authprofile.security.aspect;

import id.ac.ui.cs.advprog.authprofile.config.MonitoringConfig;
import id.ac.ui.cs.advprog.authprofile.exception.UnauthorizedException;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import id.ac.ui.cs.advprog.authprofile.security.annotation.RequiresAuthorization;
import id.ac.ui.cs.advprog.authprofile.security.strategy.AuthorizationContext;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthorizationAspectTest {

    @Mock
    private AuthorizationContext authorizationContext;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MonitoringConfig monitoringConfig;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private Method method;

    private AuthorizationAspect authorizationAspect;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create the aspect with mocked dependencies
        authorizationAspect = new AuthorizationAspect(authorizationContext, userRepository, monitoringConfig);

        // Set up monitoring config mocks - set the public field directly
        monitoringConfig.meterRegistry = meterRegistry;

        // Mock counter methods - these will be used by successful/failed authorization
        when(meterRegistry.counter(anyString(), any(Tags.class))).thenReturn(counter);

        // Set up user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        // Set up join point and method signature
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getName()).thenReturn("testMethod");

        // Mock method arguments for SpEL evaluation
        Object[] args = new Object[]{1L};
        when(joinPoint.getArgs()).thenReturn(args);
    }

    @Test
    void whenAuthenticationIsNull_thenThrowUnauthorizedException() {
        // Set up security context with null authentication
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authorizationAspect.checkAuthorization(joinPoint);
        }, "Should throw UnauthorizedException when authentication is null");

        assertEquals("User not authenticated", exception.getMessage());
    }

    @Test
    void whenUserNotAuthenticated_thenThrowUnauthorizedException() {
        // Set up security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authorizationAspect.checkAuthorization(joinPoint);
        }, "Should throw UnauthorizedException when user is not authenticated");

        assertEquals("User not authenticated", exception.getMessage());
    }

    @Test
    void whenPrincipalNotUserDetails_thenThrowUnauthorizedException() {
        // Set up security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("not a UserDetails object");

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authorizationAspect.checkAuthorization(joinPoint);
        }, "Should throw UnauthorizedException when principal is not UserDetails");

        assertEquals("User not authenticated", exception.getMessage());
    }

    @Test
    void whenUserIdIsInvalidFormat_thenThrowUnauthorizedException() {
        // Set up security context and authentication
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // Set up with a non-numeric username
        when(userDetails.getUsername()).thenReturn("not-a-number");

        // Set up a mock annotation
        RequiresAuthorization annotation = createMockAnnotation("TEST_ACTION", "#args[0]");
        when(method.getAnnotation(RequiresAuthorization.class)).thenReturn(annotation);
        when(methodSignature.getMethod()).thenReturn(method);

        // Should throw UnauthorizedException when user ID can't be parsed
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authorizationAspect.checkAuthorization(joinPoint);
        }, "Should throw UnauthorizedException when user ID format is invalid");

        // Verify the exception message contains information about the invalid format
        assertTrue(exception.getMessage().contains("Invalid user ID format"));
    }

    @Test
    void whenUserNotFound_thenThrowUnauthorizedException() {
        // Set up security context and authentication
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // Using non-existent user ID
        when(userDetails.getUsername()).thenReturn("999");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Set up a mock annotation for the method
        RequiresAuthorization annotation = createMockAnnotation("TEST_ACTION", "#args[0]");
        when(method.getAnnotation(RequiresAuthorization.class)).thenReturn(annotation);
        when(methodSignature.getMethod()).thenReturn(method);

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authorizationAspect.checkAuthorization(joinPoint);
        }, "Should throw UnauthorizedException when user is not found");

        assertEquals("User not found", exception.getMessage());

        // Verify user lookup was attempted
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void whenUserAuthorized_thenProceed() {
        // Set up security context and authentication
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("1");

        // Set up user repository
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Set up a mock annotation for the method
        RequiresAuthorization annotation = createMockAnnotation("TEST_ACTION", "#args[0]");
        when(method.getAnnotation(RequiresAuthorization.class)).thenReturn(annotation);
        when(methodSignature.getMethod()).thenReturn(method);

        // Set authorization to succeed
        when(authorizationContext.isAuthorized(eq(testUser), eq(1L), eq("TEST_ACTION"))).thenReturn(true);

        // Should not throw any exception
        assertDoesNotThrow(() -> {
            authorizationAspect.checkAuthorization(joinPoint);
        }, "Should not throw when user is authorized");

        // Verify authorization was checked
        verify(authorizationContext, times(1)).isAuthorized(eq(testUser), eq(1L), eq("TEST_ACTION"));

        // Verify successful authorization metric was recorded
        verify(meterRegistry, times(1)).counter(eq("auth_authorization_successful"), any(Tags.class));
        verify(counter, times(1)).increment();
    }

    @Test
    void whenUserNotAuthorized_thenThrowUnauthorizedException() {
        // Set up security context and authentication
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("1");

        // Set up user repository
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Set up a mock annotation for the method
        RequiresAuthorization annotation = createMockAnnotation("TEST_ACTION", "#args[0]");
        when(method.getAnnotation(RequiresAuthorization.class)).thenReturn(annotation);
        when(methodSignature.getMethod()).thenReturn(method);

        // Set authorization to fail
        when(authorizationContext.isAuthorized(eq(testUser), eq(1L), eq("TEST_ACTION"))).thenReturn(false);

        // Should throw UnauthorizedException
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authorizationAspect.checkAuthorization(joinPoint);
        }, "Should throw UnauthorizedException when user is not authorized");

        assertEquals("User not authorized to perform this action", exception.getMessage());

        // Verify authorization was checked
        verify(authorizationContext, times(1)).isAuthorized(eq(testUser), eq(1L), eq("TEST_ACTION"));

        // Verify denied authorization metric was recorded
        verify(meterRegistry, times(1)).counter(eq("auth_authorization_denied"), any(Tags.class));
        verify(counter, times(1)).increment();
    }

    @Test
    void whenResourceIdExpressionIsNull_thenUseNullResourceId() {
        // Set up security context and authentication
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("1");

        // Set up user repository
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Set up annotation with "null" SpEL expression
        RequiresAuthorization annotation = createMockAnnotation("TEST_ACTION", "null");
        when(method.getAnnotation(RequiresAuthorization.class)).thenReturn(annotation);
        when(methodSignature.getMethod()).thenReturn(method);

        // Set authorization to succeed with null resource ID
        when(authorizationContext.isAuthorized(eq(testUser), isNull(), eq("TEST_ACTION"))).thenReturn(true);

        // Should not throw any exception
        assertDoesNotThrow(() -> {
            authorizationAspect.checkAuthorization(joinPoint);
        }, "Should not throw when resource ID expression is 'null'");

        // Verify authorization was checked with null resource ID
        verify(authorizationContext, times(1)).isAuthorized(eq(testUser), isNull(), eq("TEST_ACTION"));
    }

    @Test
    void whenInvalidSpelExpression_thenUseNullResourceId() {
        // Set up security context and authentication
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("1");

        // Set up user repository
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Set up annotation with an invalid SpEL expression
        RequiresAuthorization annotation = createMockAnnotation("TEST_ACTION", "invalidExpression");
        when(method.getAnnotation(RequiresAuthorization.class)).thenReturn(annotation);
        when(methodSignature.getMethod()).thenReturn(method);

        // Set authorization to succeed with null resource ID (fallback when SpEL fails)
        when(authorizationContext.isAuthorized(eq(testUser), isNull(), eq("TEST_ACTION"))).thenReturn(true);

        // Should not throw any exception due to graceful handling of the invalid expression
        assertDoesNotThrow(() -> {
            authorizationAspect.checkAuthorization(joinPoint);
        }, "Should not throw when SpEL expression is invalid");

        // Verify authorization was called with null resource ID due to SpEL failure
        verify(authorizationContext, times(1)).isAuthorized(eq(testUser), isNull(), eq("TEST_ACTION"));
    }

    @Test
    void whenSpelExpressionReturnsNull_thenUseNullResourceId() {
        // Set up security context and authentication
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("1");

        // Set up user repository
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Set up annotation with an expression that will evaluate to null
        // The "#nonExistentVar" will evaluate to null when processed by SpEL
        RequiresAuthorization annotation = createMockAnnotation("TEST_ACTION", "#nonExistentVar");
        when(method.getAnnotation(RequiresAuthorization.class)).thenReturn(annotation);
        when(methodSignature.getMethod()).thenReturn(method);

        // Set authorization to succeed with null resource ID
        when(authorizationContext.isAuthorized(eq(testUser), isNull(), eq("TEST_ACTION"))).thenReturn(true);

        // Should not throw any exception
        assertDoesNotThrow(() -> {
            authorizationAspect.checkAuthorization(joinPoint);
        }, "Should not throw when SpEL expression returns null");

        // Verify authorization was checked with null resource ID
        verify(authorizationContext, times(1)).isAuthorized(eq(testUser), isNull(), eq("TEST_ACTION"));
    }

    @Test
    void whenSpelExpressionReturnsStringNumber_thenConvertToLong() {
        // Set up security context and authentication
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("1");

        // Set up user repository
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Set up method arguments with a string that can be converted to Long
        Object[] args = new Object[]{"123"};
        when(joinPoint.getArgs()).thenReturn(args);

        // Set up annotation with SpEL expression that returns the first argument
        RequiresAuthorization annotation = createMockAnnotation("TEST_ACTION", "#args[0]");
        when(method.getAnnotation(RequiresAuthorization.class)).thenReturn(annotation);
        when(methodSignature.getMethod()).thenReturn(method);

        // Set authorization to succeed - the SpEL should convert "123" to 123L
        when(authorizationContext.isAuthorized(eq(testUser), eq(123L), eq("TEST_ACTION"))).thenReturn(true);

        // Should not throw any exception
        assertDoesNotThrow(() -> {
            authorizationAspect.checkAuthorization(joinPoint);
        }, "Should not throw when resource ID is converted from String to Long");

        // Verify authorization was checked with converted Long value
        verify(authorizationContext, times(1)).isAuthorized(eq(testUser), eq(123L), eq("TEST_ACTION"));
    }

    @Test
    void whenSpelExpressionFailsConversion_thenUseNullResourceId() {
        // Set up security context and authentication
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("1");

        // Set up user repository
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Set up method arguments with a string that cannot be converted to Long
        Object[] args = new Object[]{"not-a-number"};
        when(joinPoint.getArgs()).thenReturn(args);

        // Set up annotation with SpEL expression that returns the first argument
        RequiresAuthorization annotation = createMockAnnotation("TEST_ACTION", "#args[0]");
        when(method.getAnnotation(RequiresAuthorization.class)).thenReturn(annotation);
        when(methodSignature.getMethod()).thenReturn(method);

        // Set authorization to succeed with null resource ID (fallback when conversion fails)
        when(authorizationContext.isAuthorized(eq(testUser), isNull(), eq("TEST_ACTION"))).thenReturn(true);

        // Should not throw any exception due to graceful handling of conversion failure
        assertDoesNotThrow(() -> {
            authorizationAspect.checkAuthorization(joinPoint);
        }, "Should not throw when SpEL result cannot be converted to Long");

        // Verify authorization was called with null resource ID due to conversion failure
        verify(authorizationContext, times(1)).isAuthorized(eq(testUser), isNull(), eq("TEST_ACTION"));
    }

    @Test
    void whenMultipleArgumentsInSpel_thenEvaluateCorrectly() {
        // Set up security context and authentication
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("1");

        // Set up user repository
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Set up method arguments with multiple values
        Object[] args = new Object[]{1L, "test", 42};
        when(joinPoint.getArgs()).thenReturn(args);

        // Set up annotation with SpEL expression that returns the third argument (index 2)
        RequiresAuthorization annotation = createMockAnnotation("TEST_ACTION", "#args[2]");
        when(method.getAnnotation(RequiresAuthorization.class)).thenReturn(annotation);
        when(methodSignature.getMethod()).thenReturn(method);

        // Set authorization to succeed with the third argument value
        when(authorizationContext.isAuthorized(eq(testUser), eq(42L), eq("TEST_ACTION"))).thenReturn(true);

        // Should not throw any exception
        assertDoesNotThrow(() -> {
            authorizationAspect.checkAuthorization(joinPoint);
        }, "Should not throw when using correct SpEL expression with multiple arguments");

        // Verify authorization was checked with the correct argument value
        verify(authorizationContext, times(1)).isAuthorized(eq(testUser), eq(42L), eq("TEST_ACTION"));
    }

    // Helper method to create a mock annotation
    private RequiresAuthorization createMockAnnotation(String action, String resourceIdExpression) {
        return new RequiresAuthorization() {
            @Override
            public String action() {
                return action;
            }

            @Override
            public String resourceIdExpression() {
                return resourceIdExpression;
            }

            @Override
            public Class<RequiresAuthorization> annotationType() {
                return RequiresAuthorization.class;
            }
        };
    }
}