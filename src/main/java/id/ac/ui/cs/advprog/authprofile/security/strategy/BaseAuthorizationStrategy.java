package id.ac.ui.cs.advprog.authprofile.security.strategy;

import id.ac.ui.cs.advprog.authprofile.model.User;
import java.util.Set;

public abstract class BaseAuthorizationStrategy implements AuthorizationStrategy {

    /**
     * Checks if user can modify their own resource
     */
    protected boolean canModifyOwnResource(User user, Long resourceId) {
        return resourceId == null || user.getId().equals(resourceId);
    }


    /**
     * Checks if action is a modification action
     */
    protected boolean isModificationAction(String action) {
        return Set.of(UPDATE_PROFILE, DELETE_PROFILE).contains(action);
    }

    /**
     * Default handling for common modification actions
     */
    protected boolean handleModificationActions(User user, Long resourceId, String action) {
        if (isModificationAction(action)) {
            return canModifyOwnResource(user, resourceId);
        }
        return false;
    }
}