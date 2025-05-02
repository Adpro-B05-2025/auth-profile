package id.ac.ui.cs.advprog.authprofile.factory;

import id.ac.ui.cs.advprog.authprofile.dto.request.BaseRegisterRequest;
import id.ac.ui.cs.advprog.authprofile.model.User;

import java.util.Set;

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

    /**
     * Returns the set of request types this factory can handle
     * @return a set of request class types
     */
    Set<Class<? extends BaseRegisterRequest>> getSupportedRequestTypes();
}