package id.ac.ui.cs.advprog.authprofile.security.aspect;

import id.ac.ui.cs.advprog.authprofile.config.MonitoringConfig;
import id.ac.ui.cs.advprog.authprofile.exception.UnauthorizedException;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import id.ac.ui.cs.advprog.authprofile.security.annotation.RequiresAuthorization;
import id.ac.ui.cs.advprog.authprofile.security.strategy.AuthorizationContext;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Tags;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class AuthorizationAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationAspect.class);

    private final AuthorizationContext authorizationContext;
    private final UserRepository userRepository;
    private final MonitoringConfig monitoringConfig;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    @Autowired
    public AuthorizationAspect(AuthorizationContext authorizationContext,
                               UserRepository userRepository,
                               MonitoringConfig monitoringConfig) {
        this.authorizationContext = authorizationContext;
        this.userRepository = userRepository;
        this.monitoringConfig = monitoringConfig;
    }

    @Before("@annotation(id.ac.ui.cs.advprog.authprofile.security.annotation.RequiresAuthorization)")
    @Timed(value = "auth_authorization_check_duration", description = "Time taken to check authorization")
    public void checkAuthorization(JoinPoint joinPoint) {
        logger.debug("Checking authorization for method: {}", joinPoint.getSignature().getName());

        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof UserDetails)) {
            logger.warn("Authorization check failed - user not authenticated");
            throw new UnauthorizedException("User not authenticated");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Parse user ID from userDetails
        Long userId;
        try {
            userId = Long.parseLong(userDetails.getUsername());
        } catch (NumberFormatException e) {
            logger.error("Invalid user ID format: {}", userDetails.getUsername());
            throw new UnauthorizedException("Invalid user ID format: " + userDetails.getUsername());
        }

        // Find user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", userId);
                    return new UnauthorizedException("User not found");
                });

        // Get the annotation details
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresAuthorization annotation = method.getAnnotation(RequiresAuthorization.class);

        String action = annotation.action();

        // Extract the resource ID using SpEL if provided
        Long resourceId = null;
        if (!"null".equals(annotation.resourceIdExpression())) {
            try {
                StandardEvaluationContext context = new StandardEvaluationContext();
                // Make arguments available via #args array
                context.setVariable("args", joinPoint.getArgs());

                Expression expression = expressionParser.parseExpression(annotation.resourceIdExpression());
                Object result = expression.getValue(context);

                if (result != null) {
                    resourceId = result instanceof Long ? (Long) result : Long.valueOf(result.toString());
                }
            } catch (EvaluationException | NumberFormatException ex) {
                logger.debug("Could not evaluate resource ID expression: {}", ex.getMessage());
                resourceId = null;
            }
        }

        // Check if the user is authorized
        boolean authorized = authorizationContext.isAuthorized(user, resourceId, action);

        if (!authorized) {
            // Increment authorization denied counter with tags - CORRECTED VERSION
            monitoringConfig.meterRegistry.counter("auth_authorization_denied",
                    Tags.of(
                            "action", action,
                            "userType", user.getClass().getSimpleName(),
                            "method", joinPoint.getSignature().getName()
                    )).increment();

            logger.warn("Authorization denied for user {} (ID: {}) attempting action: {} on resource: {}",
                    user.getEmail(), userId, action, resourceId);

            throw new UnauthorizedException("User not authorized to perform this action");
        }

        logger.debug("Authorization successful for user {} (ID: {}) action: {} on resource: {}",
                user.getEmail(), userId, action, resourceId);

        // Increment successful authorization counter with tags - CORRECTED VERSION
        monitoringConfig.meterRegistry.counter("auth_authorization_successful",
                Tags.of(
                        "action", action,
                        "userType", user.getClass().getSimpleName(),
                        "method", joinPoint.getSignature().getName()
                )).increment();
    }
}