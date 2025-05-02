package id.ac.ui.cs.advprog.authprofile.security.strategy;

import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
import id.ac.ui.cs.advprog.authprofile.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationContext {

    private final PacillianAuthorizationStrategy pacillianStrategy;
    private final CareGiverAuthorizationStrategy careGiverStrategy;

    @Autowired
    public AuthorizationContext(
            PacillianAuthorizationStrategy pacillianStrategy,
            CareGiverAuthorizationStrategy careGiverStrategy) {
        this.pacillianStrategy = pacillianStrategy;
        this.careGiverStrategy = careGiverStrategy;
    }

    /**
     * Determines if a user is authorized to perform an action
     * @param user the user attempting the action
     * @param resourceId the ID of the resource being accessed
     * @param action the action being performed
     * @return true if authorized, false otherwise
     */
    public boolean isAuthorized(User user, Long resourceId, String action) {
        if (user instanceof Pacillian) {
            return pacillianStrategy.isAuthorized(user, resourceId, action);
        } else if (user instanceof CareGiver) {
            return careGiverStrategy.isAuthorized(user, resourceId, action);
        }

        // Default to unauthorized for unknown user types
        return false;
    }
}