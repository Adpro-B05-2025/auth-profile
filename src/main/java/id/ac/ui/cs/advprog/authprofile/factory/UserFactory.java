package id.ac.ui.cs.advprog.authprofile.factory;

import id.ac.ui.cs.advprog.authprofile.dto.request.BaseRegisterRequest;
import id.ac.ui.cs.advprog.authprofile.model.User;

/**
 * Factory interface for creating different types of users
 */
public interface UserFactory {
    /**
     * Creates a user with proper roles
     * @param registerRequest the registration request containing user data
     * @param encodedPassword the encrypted password
     * @return a configured user entity
     */
    User createUser(BaseRegisterRequest registerRequest, String encodedPassword);
}