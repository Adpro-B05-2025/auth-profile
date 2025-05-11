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
            case "VIEW_OWN_PROFILE":
                // CareGivers can always view their own profile
                return true;
            case "VIEW_PROFILE":
                // CareGivers can view any profile
                return true;
            case "UPDATE_PROFILE":
            case "DELETE_PROFILE":
                // CareGivers can only update/delete their own profile
                return resourceId == null || user.getId().equals(resourceId);
            case "VIEW_PACILLIAN_MEDICAL_HISTORY":
                // CareGivers can view any Pacillian's medical history
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean supportsUserType(User user) {
        return user instanceof CareGiver;
    }
}