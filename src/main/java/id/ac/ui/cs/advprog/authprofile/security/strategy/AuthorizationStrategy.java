package id.ac.ui.cs.advprog.authprofile.security.strategy;

import id.ac.ui.cs.advprog.authprofile.model.User;

public interface AuthorizationStrategy {
    /**
     * Checks if a user is authorized to perform a specific action
     * @param user the user attempting the action
     * @param resourceId the ID of the resource being accessed (optional)
     * @param action the action being performed
     * @return true if authorized, false otherwise
     */
    boolean isAuthorized(User user, Long resourceId, String action);
}