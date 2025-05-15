package id.ac.ui.cs.advprog.authprofile.security.strategy;

import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.User;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(100)
public class CareGiverAuthorizationStrategy implements AuthorizationStrategy {

    @Override
    public boolean isAuthorized(User user, Long resourceId, String action) {
        // Must be a caregiver
        if (!(user instanceof CareGiver)) {
            return false;
        }

        // Check specific actions
        switch (action) {
            // Profile viewing permissions
            case "VIEW_OWN_PROFILE":
            case "VIEW_PROFILE":
                // CareGivers can view any profile, including their own
                return true;
            // Profile modification permissions
            case "UPDATE_PROFILE":
            case "DELETE_PROFILE":
                // CareGivers can only update/delete their own profile
                return resourceId == null || user.getId().equals(resourceId);
            // Medical data access permissions
            case "VIEW_PACILLIAN_MEDICAL_HISTORY":
                // CareGivers can view any Pacillian's medical history
                return true;
            // Caregiver viewing permissions
            case "VIEW_CAREGIVER":
                // All Pacillians can view caregiver details
                return true;
            // Default case - deny access for any other actions
            default:
                return false;
        }
    }

    @Override
    public boolean supportsUserType(User user) {
        return user instanceof CareGiver;
    }
}