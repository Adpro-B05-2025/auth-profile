package id.ac.ui.cs.advprog.authprofile.security.aspect;

import id.ac.ui.cs.advprog.authprofile.exception.UnauthorizedException;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import id.ac.ui.cs.advprog.authprofile.security.annotation.RequiresAuthorization;
import id.ac.ui.cs.advprog.authprofile.security.strategy.AuthorizationContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthorizationAspectTest {

    @Mock
    private AuthorizationContext authorizationContext;

    @Mock
    private UserRepository userRepository;

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

    @InjectMocks
    private AuthorizationAspect authorizationAspect;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        // Set up user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Set up join point and method signature
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);

        // Mock method arguments for SpEL evaluation
        Object[] args = new Object[]{1L};
        when(joinPoint.getArgs()).thenReturn(args);
    }

    @Test
    void whenAuthenticationIsNull_thenThrowUnauthorizedException() {
        // Set up security context with null authentication
        when(securityContext.getAuthentication()).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> {
            authorizationAspect.checkAuthorization(joinPoint);
        }, "Should throw UnauthorizedException when authentication is null");
    }

    @Test
    void whenUserNotAuthenticated_thenThrowUnauthorizedException() {
        when(authentication.isAuthenticated()).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> {
            authorizationAspect.checkAuthorization(joinPoint);
        }, "Should throw UnauthorizedException when user is not authenticated");
    }

    @Test
    void whenPrincipalNotUserDetails_thenThrowUnauthorizedException() {
        when(authentication.getPrincipal()).thenReturn("not a UserDetails object");

        assertThrows(UnauthorizedException.class, () -> {
            authorizationAspect.checkAuthorization(joinPoint);
        }, "Should throw UnauthorizedException when principal is not UserDetails");
    }

    @Test
    void whenUserNotFound_thenThrowUnauthorizedException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Set up a mock annotation for the method
        RequiresAuthorization annotation = createMockAnnotation("TEST_ACTION", "#args[0]");
        when(method.getAnnotation(RequiresAuthorization.class)).thenReturn(annotation);

        assertThrows(UnauthorizedException.class, () -> {
            authorizationAspect.checkAuthorization(joinPoint);
        }, "Should throw UnauthorizedException when user is not found");
    }

    @Test
    void whenUserAuthorized_thenProceed() {
        // Set up a mock annotation for the method
        RequiresAuthorization annotation = createMockAnnotation("TEST_ACTION", "#args[0]");
        when(method.getAnnotation(RequiresAuthorization.class)).thenReturn(annotation);

        // Set authorization to succeed
        when(authorizationContext.isAuthorized(eq(testUser), anyLong(), eq("TEST_ACTION"))).thenReturn(true);

        // Should not throw any exception
        assertDoesNotThrow(() -> {
            authorizationAspect.checkAuthorization(joinPoint);
        }, "Should not throw when user is authorized");
    }

    @Test
    void whenResourceIdExpressionIsNull_thenUseNullResourceId() {
        // Set up annotation with "null" SpEL expression
        RequiresAuthorization annotation = createMockAnnotation("TEST_ACTION", "null");
        when(method.getAnnotation(RequiresAuthorization.class)).thenReturn(annotation);

        // Set authorization to succeed with null resource ID
        when(authorizationContext.isAuthorized(eq(testUser), isNull(), eq("TEST_ACTION"))).thenReturn(true);

        // Should not throw any exception
        assertDoesNotThrow(() -> {
            authorizationAspect.checkAuthorization(joinPoint);
        }, "Should not throw when resource ID expression is 'null'");
    }

    @Test
    void whenInvalidSpelExpression_thenUseNullResourceId() {
        // Set up annotation with an invalid SpEL expression
        RequiresAuthorization annotation = createMockAnnotation("TEST_ACTION", "invalidExpression");
        when(method.getAnnotation(RequiresAuthorization.class)).thenReturn(annotation);

        // Set authorization to succeed with null resource ID
        when(authorizationContext.isAuthorized(eq(testUser), isNull(), eq("TEST_ACTION"))).thenReturn(true);

        // Should not throw any exception due to graceful handling of the invalid expression
        assertDoesNotThrow(() -> {
            authorizationAspect.checkAuthorization(joinPoint);
        }, "Should not throw when SpEL expression is invalid");
    }

    @Test
    void whenNonLongResourceId_thenConvertToLong() {
        // Create a special mock for this test to handle the SpEL evaluation
        RequiresAuthorization annotation = createMockAnnotation("TEST_ACTION", "'1'");
        when(method.getAnnotation(RequiresAuthorization.class)).thenReturn(annotation);

        // For this test, we need to set up the aspect to evaluate the string expression to "1"
        // but we can't directly mock the SpEL evaluation, so we'll set up the authorization check
        // to accept any Long argument
        when(authorizationContext.isAuthorized(eq(testUser), any(Long.class), eq("TEST_ACTION"))).thenReturn(true);

        // Should not throw any exception
        assertDoesNotThrow(() -> {
            authorizationAspect.checkAuthorization(joinPoint);
        }, "Should not throw when resource ID is converted from String to Long");
    }

    @Test
    void whenUserNotAuthorized_thenThrowUnauthorizedException() {
        // Set up a mock annotation for the method
        RequiresAuthorization annotation = createMockAnnotation("TEST_ACTION", "#args[0]");
        when(method.getAnnotation(RequiresAuthorization.class)).thenReturn(annotation);

        // Set authorization to fail
        when(authorizationContext.isAuthorized(any(User.class), anyLong(), anyString())).thenReturn(false);

        // Should throw UnauthorizedException
        assertThrows(UnauthorizedException.class, () -> {
            authorizationAspect.checkAuthorization(joinPoint);
        }, "Should throw UnauthorizedException when user is not authorized");
    }

    @Test
    void whenSpelExpressionReturnsNull_thenUseNullResourceId() {
        // Set up annotation with an expression that will evaluate to null
        // The "#nonExistentVar" will evaluate to null when processed by SpEL
        RequiresAuthorization annotation = createMockAnnotation("TEST_ACTION", "#nonExistentVar");
        when(method.getAnnotation(RequiresAuthorization.class)).thenReturn(annotation);

        // Set authorization to succeed with null resource ID
        when(authorizationContext.isAuthorized(eq(testUser), isNull(), eq("TEST_ACTION"))).thenReturn(true);

        // Should not throw any exception
        assertDoesNotThrow(() -> {
            authorizationAspect.checkAuthorization(joinPoint);
        }, "Should not throw when SpEL expression returns null");
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