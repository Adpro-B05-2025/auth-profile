package id.ac.ui.cs.advprog.authprofile.config;

import id.ac.ui.cs.advprog.authprofile.model.Role;
import id.ac.ui.cs.advprog.authprofile.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer {

    @Autowired
    private RoleRepository roleRepository;

    @PostConstruct
    @Transactional
    public void initialize() {
        initRoles();
    }

    private void initRoles() {
        if (roleRepository.count() == 0) {
            Role pacillianRole = new Role();
            pacillianRole.setName(Role.ERole.ROLE_PACILLIAN);
            roleRepository.save(pacillianRole);

            Role caregiverRole = new Role();
            caregiverRole.setName(Role.ERole.ROLE_CAREGIVER);
            roleRepository.save(caregiverRole);
        }
    }
}