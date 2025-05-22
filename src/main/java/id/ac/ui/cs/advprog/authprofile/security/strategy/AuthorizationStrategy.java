package id.ac.ui.cs.advprog.authprofile.security.strategy;

import id.ac.ui.cs.advprog.authprofile.model.User;

public interface AuthorizationStrategy {

    // Action constants for better maintainability
    String VIEW_USERNAME = "VIEW_USERNAME";
    String VIEW_OWN_PROFILE = "VIEW_OWN_PROFILE";
    String VIEW_PROFILE = "VIEW_PROFILE";
    String UPDATE_PROFILE = "UPDATE_PROFILE";
    String DELETE_PROFILE = "DELETE_PROFILE";
    String VIEW_PACILLIAN_MEDICAL_HISTORY = "VIEW_PACILLIAN_MEDICAL_HISTORY";
    String VIEW_CAREGIVER = "VIEW_CAREGIVER";

    /**
     * Checks if a user is authorized to perform a specific action
     * @param user the user attempting the action
     * @param resourceId the ID of the resource being accessed (optional)
     * @param action the action being performed
     * @return true if authorized, false otherwise
     */
    boolean isAuthorized(User user, Long resourceId, String action);

    /**
     * Determines if this strategy can handle the given user type
     * @param user the user to check
     * @return true if this strategy can handle the user type
     */
    boolean supportsUserType(User user);
}