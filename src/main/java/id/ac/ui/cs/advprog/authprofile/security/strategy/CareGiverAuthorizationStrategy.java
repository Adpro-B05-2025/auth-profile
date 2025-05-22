package id.ac.ui.cs.advprog.authprofile.security.strategy;

import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.User;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(100)
public class CareGiverAuthorizationStrategy extends BaseAuthorizationStrategy {

    @Override
    public boolean isAuthorized(User user, Long resourceId, String action) {
        if (!supportsUserType(user)) {
            return false;
        }

        return switch (action) {
            // CareGivers have broad viewing permissions
            case VIEW_USERNAME, VIEW_OWN_PROFILE, VIEW_PROFILE, VIEW_CAREGIVER -> true;

            // CareGivers can view any Pacillian's medical history
            case VIEW_PACILLIAN_MEDICAL_HISTORY -> true;

            // Handle modification actions using base class logic
            case UPDATE_PROFILE, DELETE_PROFILE -> handleModificationActions(user, resourceId, action);

            // Deny all other actions
            default -> false;
        };
    }

    @Override
    public boolean supportsUserType(User user) {
        return user instanceof CareGiver;
    }
}