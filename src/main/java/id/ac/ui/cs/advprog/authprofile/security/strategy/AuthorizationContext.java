package id.ac.ui.cs.advprog.authprofile.security.strategy;

import id.ac.ui.cs.advprog.authprofile.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthorizationContext {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationContext.class);

    private final List<AuthorizationStrategy> strategies;

    @Autowired
    public AuthorizationContext(List<AuthorizationStrategy> strategies) {
        this.strategies = strategies;
        logger.info("Loaded {} authorization strategies", strategies.size());
        strategies.forEach(strategy ->
                logger.info("Registered strategy: {}", strategy.getClass().getSimpleName()));
    }

    /**
     * Determines if a user is authorized to perform an action by finding the first
     * strategy that supports the user type and delegating to it
     *
     * @param user the user attempting the action
     * @param resourceId the ID of the resource being accessed
     * @param action the action being performed
     * @return true if authorized, false otherwise
     */
    public boolean isAuthorized(User user, Long resourceId, String action) {
        // Find the first strategy that supports this user type
        for (AuthorizationStrategy strategy : strategies) {
            if (strategy.supportsUserType(user)) {
                logger.debug("Using {} for authorization check",
                        strategy.getClass().getSimpleName());
                return strategy.isAuthorized(user, resourceId, action);
            }
        }

        // If no strategy supports this user type, default to unauthorized
        logger.warn("No authorization strategy found for user type: {}",
                user != null ? user.getClass().getSimpleName() : "null");
        return false;
    }
}