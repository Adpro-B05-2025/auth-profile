package id.ac.ui.cs.advprog.authprofile.security.strategy;

import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
import id.ac.ui.cs.advprog.authprofile.model.User;
import org.springframework.stereotype.Component;

@Component
public class PacillianAuthorizationStrategy implements AuthorizationStrategy {

    @Override
    public boolean isAuthorized(User user, Long resourceId, String action) {
        // Pacillians can only access their own data
        if (!(user instanceof Pacillian)) {
            return false;
        }

        // Check specific actions
        switch (action) {
            case "VIEW_PROFILE":
                // Pacillians can view their own profile or any caregiver profile
                return user.getId().equals(resourceId);
            case "UPDATE_PROFILE":
            case "DELETE_PROFILE":
                // Pacillians can only update/delete their own profile
                return user.getId().equals(resourceId);
            case "VIEW_CAREGIVER":
                // All Pacillians can view caregiver details
                return true;
            default:
                return false;
        }
    }
}