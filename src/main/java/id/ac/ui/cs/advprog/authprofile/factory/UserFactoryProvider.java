package id.ac.ui.cs.advprog.authprofile.factory;

import id.ac.ui.cs.advprog.authprofile.dto.request.BaseRegisterRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterCareGiverRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterPacillianRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provider class that returns the appropriate factory based on the request type
 */
@Component
public class UserFactoryProvider {

    private final PacillianFactory pacillianFactory;
    private final CareGiverFactory careGiverFactory;

    @Autowired
    public UserFactoryProvider(PacillianFactory pacillianFactory, CareGiverFactory careGiverFactory) {
        this.pacillianFactory = pacillianFactory;
        this.careGiverFactory = careGiverFactory;
    }

    /**
     * Returns the appropriate user factory based on the request type
     * @param registerRequest the registration request
     * @return the user factory
     */
    public UserFactory getFactory(BaseRegisterRequest registerRequest) {
        if (registerRequest instanceof RegisterPacillianRequest) {
            return pacillianFactory;
        } else if (registerRequest instanceof RegisterCareGiverRequest) {
            return careGiverFactory;
        } else {
            throw new IllegalArgumentException("Unsupported registration request type");
        }
    }
}