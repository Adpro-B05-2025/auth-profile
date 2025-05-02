package id.ac.ui.cs.advprog.authprofile.factory;

import id.ac.ui.cs.advprog.authprofile.dto.request.BaseRegisterRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterPacillianRequest;
import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
import id.ac.ui.cs.advprog.authprofile.model.Role;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Factory implementation for creating Pacillian users
 */
@Component
public class PacillianFactory implements UserFactory {

    private final RoleRepository roleRepository;

    @Autowired
    public PacillianFactory(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public User createUser(BaseRegisterRequest registerRequest, String encodedPassword) {
        if (!(registerRequest instanceof RegisterPacillianRequest pacillianRequest)) {
            throw new IllegalArgumentException("Invalid request type for PacillianFactory");
        }

        // Create Pacillian user
        Pacillian pacillian = new Pacillian(
                registerRequest.getEmail(),
                encodedPassword,
                registerRequest.getName(),
                registerRequest.getNik(),
                registerRequest.getAddress(),
                registerRequest.getPhoneNumber(),
                pacillianRequest.getMedicalHistory()
        );

        // Set Pacillian role
        Set<Role> roles = new HashSet<>();
        Role pacillianRole = roleRepository.findByName(Role.ERole.ROLE_PACILLIAN)
                .orElseThrow(() -> new RuntimeException("Error: Pacillian role not found"));
        roles.add(pacillianRole);
        pacillian.setRoles(roles);

        return pacillian;
    }
}