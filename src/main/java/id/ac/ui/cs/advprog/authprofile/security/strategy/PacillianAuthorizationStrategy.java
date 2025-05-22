package id.ac.ui.cs.advprog.authprofile.security.strategy;

import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(100)
public class PacillianAuthorizationStrategy extends BaseAuthorizationStrategy {

    private final UserRepository userRepository;

    @Autowired
    public PacillianAuthorizationStrategy(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean isAuthorized(User user, Long resourceId, String action) {
        if (!supportsUserType(user)) {
            return false;
        }

        return switch (action) {
            // Pacillians can always view their own profile info
            case VIEW_OWN_PROFILE -> true;

            // Pacillians can view any caregiver profile
            case VIEW_CAREGIVER -> true;

            // For general profile viewing, check if it's their own profile
            case VIEW_PROFILE -> canViewProfile(user, resourceId);

            // Handle modification actions using base class logic
            case UPDATE_PROFILE, DELETE_PROFILE -> handleModificationActions(user, resourceId, action);

            // Deny all other actions
            default -> false;
        };
    }

    @Override
    public boolean supportsUserType(User user) {
        return user instanceof Pacillian;
    }

    /**
     * Pacillians can view their own profile or any caregiver profile
     */
    private boolean canViewProfile(User user, Long resourceId) {
        if (user.getId().equals(resourceId)) {
            return true; // Own profile
        } else {
            return false;
        }
    }
}