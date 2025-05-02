package id.ac.ui.cs.advprog.authprofile.security.strategy;

import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PacillianAuthorizationStrategy implements AuthorizationStrategy {

    private final UserRepository userRepository;

    @Autowired
    public PacillianAuthorizationStrategy(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean isAuthorized(User user, Long resourceId, String action) {
        // Pacillians can only access their own data
        if (!(user instanceof Pacillian)) {
            return false;
        }

        // Check specific actions
        switch (action) {
            case "VIEW_OWN_PROFILE":
                // Pacillians can always view their own profile
                return true;
            case "VIEW_PROFILE":
                // Pacillians can view their own profile or any caregiver profile
                return userRepository.findById(resourceId)
                        .map(targetUser -> targetUser instanceof CareGiver || user.getId().equals(resourceId))
                        .orElse(false);
            case "UPDATE_PROFILE":
            case "DELETE_PROFILE":
                // Pacillians can only update/delete their own profile
                return resourceId == null || user.getId().equals(resourceId);
            case "VIEW_CAREGIVER":
                // All Pacillians can view caregiver details
                return true;
            default:
                return false;
        }
    }
}