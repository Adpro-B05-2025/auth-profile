package id.ac.ui.cs.advprog.authprofile.factory;

import id.ac.ui.cs.advprog.authprofile.dto.request.BaseRegisterRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterCareGiverRequest;
import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.Role;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.model.WorkingSchedule;
import id.ac.ui.cs.advprog.authprofile.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Factory implementation for creating CareGiver users
 */
@Component
public class CareGiverFactory implements UserFactory {

    private final RoleRepository roleRepository;

    @Autowired
    public CareGiverFactory(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public User createUser(BaseRegisterRequest registerRequest, String encodedPassword) {
        if (!(registerRequest instanceof RegisterCareGiverRequest careGiverRequest)) {
            throw new IllegalArgumentException("Invalid request type for CareGiverFactory");
        }

        // Create CareGiver user
        CareGiver careGiver = new CareGiver(
                registerRequest.getEmail(),
                encodedPassword,
                registerRequest.getName(),
                registerRequest.getNik(),
                registerRequest.getAddress(),
                registerRequest.getPhoneNumber(),
                careGiverRequest.getSpeciality(),
                careGiverRequest.getWorkAddress()
        );

        // Add working schedules if provided
        if (careGiverRequest.getWorkingSchedules() != null) {
            for (RegisterCareGiverRequest.WorkingScheduleRequest scheduleRequest : careGiverRequest.getWorkingSchedules()) {
                WorkingSchedule schedule = new WorkingSchedule(
                        scheduleRequest.getDayOfWeek(),
                        scheduleRequest.getStartTime(),
                        scheduleRequest.getEndTime(),
                        careGiver
                );
                careGiver.addWorkingSchedule(schedule);
            }
        }

        // Set CareGiver role
        Set<Role> roles = new HashSet<>();
        Role careGiverRole = roleRepository.findByName(Role.ERole.ROLE_CAREGIVER)
                .orElseThrow(() -> new RuntimeException("Error: CareGiver role not found"));
        roles.add(careGiverRole);
        careGiver.setRoles(roles);

        return careGiver;
    }
}