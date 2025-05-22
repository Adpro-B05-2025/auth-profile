package id.ac.ui.cs.advprog.authprofile.security.strategy;

import id.ac.ui.cs.advprog.authprofile.model.User;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Default authorization strategy for handling any user type not covered by other strategies.
 * This follows the "Chain of Responsibility" pattern by acting as a fallback.
 */
@Component
@Order(Integer.MAX_VALUE) // Lowest possible precedence
public class DefaultAuthorizationStrategy extends BaseAuthorizationStrategy {

    @Override
    public boolean isAuthorized(User user, Long resourceId, String action) {
        return switch (action) {
            // Basic permissions any user should have
            case VIEW_OWN_PROFILE -> true;

            // Handle modification actions using base class logic
            case UPDATE_PROFILE, DELETE_PROFILE -> handleModificationActions(user, resourceId, action);

            // Deny all other actions by default
            default -> false;
        };
    }

    @Override
    public boolean supportsUserType(User user) {
        // This is a fallback strategy that handles any user type
        return true;
    }
}