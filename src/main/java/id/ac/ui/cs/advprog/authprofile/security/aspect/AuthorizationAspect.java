package id.ac.ui.cs.advprog.authprofile.security.aspect;

import id.ac.ui.cs.advprog.authprofile.exception.UnauthorizedException;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import id.ac.ui.cs.advprog.authprofile.security.annotation.RequiresAuthorization;
import id.ac.ui.cs.advprog.authprofile.security.strategy.AuthorizationContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
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

    private final AuthorizationContext authorizationContext;
    private final UserRepository userRepository;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    @Autowired
    public AuthorizationAspect(AuthorizationContext authorizationContext, UserRepository userRepository) {
        this.authorizationContext = authorizationContext;
        this.userRepository = userRepository;
    }

    @Before("@annotation(id.ac.ui.cs.advprog.authprofile.security.annotation.RequiresAuthorization)")
    public void checkAuthorization(JoinPoint joinPoint) {
        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new UnauthorizedException("User not authenticated");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Parse user ID from userDetails
        Long userId;
        try {
            userId = Long.parseLong(userDetails.getUsername());
        } catch (NumberFormatException e) {
            // Convert NumberFormatException to UnauthorizedException
            throw new UnauthorizedException("Invalid user ID format: " + userDetails.getUsername());
        }

        // Find user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        // Get the annotation details
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresAuthorization annotation = method.getAnnotation(RequiresAuthorization.class);

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
                // If we can't evaluate the expression or convert to Long, use null (which means no specific resource)
                resourceId = null;
            }
        }

        // Check if the user is authorized
        if (!authorizationContext.isAuthorized(user, resourceId, annotation.action())) {
            throw new UnauthorizedException("User not authorized to perform this action");
        }
    }
}