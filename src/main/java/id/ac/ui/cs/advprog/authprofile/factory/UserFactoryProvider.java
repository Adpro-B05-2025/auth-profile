package id.ac.ui.cs.advprog.authprofile.factory;

import id.ac.ui.cs.advprog.authprofile.dto.request.BaseRegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provider class that returns the appropriate factory based on the request type.
 * This implementation follows the Open/Closed Principle by using a registry of factories.
 */
@Component
public class UserFactoryProvider {

    private final Map<Class<? extends BaseRegisterRequest>, UserFactory> factoryRegistry = new HashMap<>();

    /**
     * Constructs a new UserFactoryProvider with a list of factories.
     * Each factory registers itself for the request types it can handle.
     *
     * @param factories list of all UserFactory implementations
     */
    @Autowired
    public UserFactoryProvider(List<UserFactory> factories) {
        // Each factory registers the request types it can handle
        for (UserFactory factory : factories) {
            for (Class<? extends BaseRegisterRequest> requestType : factory.getSupportedRequestTypes()) {
                factoryRegistry.put(requestType, factory);
            }
        }
    }

    /**
     * Returns the appropriate user factory based on the request type
     * @param registerRequest the registration request
     * @return the user factory
     * @throws IllegalArgumentException if no factory is registered for the request type
     */
    public UserFactory getFactory(BaseRegisterRequest registerRequest) {
        if (registerRequest == null) {
            throw new IllegalArgumentException("Register request cannot be null");
        }

        UserFactory factory = factoryRegistry.get(registerRequest.getClass());
        if (factory == null) {
            throw new IllegalArgumentException("Unsupported registration request type: " +
                    registerRequest.getClass().getName());
        }

        return factory;
    }
}