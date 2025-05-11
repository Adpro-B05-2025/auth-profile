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
public class DefaultAuthorizationStrategy implements AuthorizationStrategy {

    @Override
    public boolean isAuthorized(User user, Long resourceId, String action) {
        // Basic permissions any user should have
        switch (action) {
            case "VIEW_OWN_PROFILE":
                return true;
            case "UPDATE_PROFILE":
            case "DELETE_PROFILE":
                return resourceId == null || user.getId().equals(resourceId);
            default:
                return false;
        }
    }

    @Override
    public boolean supportsUserType(User user) {
        // This is a fallback strategy that will handle any user type
        return true;
    }


}