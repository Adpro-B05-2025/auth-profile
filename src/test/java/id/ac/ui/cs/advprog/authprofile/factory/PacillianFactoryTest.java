package id.ac.ui.cs.advprog.authprofile.factory;

import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterCareGiverRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterPacillianRequest;
import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
import id.ac.ui.cs.advprog.authprofile.model.Role;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PacillianFactoryTest {

    @Mock
    private RoleRepository roleRepository;

    private PacillianFactory pacillianFactory;
    private RegisterPacillianRequest pacillianRequest;
    private RegisterCareGiverRequest careGiverRequest;
    private Role pacillianRole;

    @BeforeEach
    void setUp() {
        pacillianFactory = new PacillianFactory(roleRepository);

        // Set up roles
        pacillianRole = new Role();
        pacillianRole.setId(1);
        pacillianRole.setName(Role.ERole.ROLE_PACILLIAN);

        // Set up pacillian request
        pacillianRequest = new RegisterPacillianRequest();
        pacillianRequest.setEmail("pacillian@example.com");
        pacillianRequest.setPassword("password");
        pacillianRequest.setName("Test Pacillian");
        pacillianRequest.setNik("1234567890123456");
        pacillianRequest.setAddress("Test Address");
        pacillianRequest.setPhoneNumber("081234567890");
        pacillianRequest.setMedicalHistory("No significant history");

        // Set up caregiver request for testing invalid type
        List<RegisterCareGiverRequest.WorkingScheduleRequest> schedules = new ArrayList<>();
        RegisterCareGiverRequest.WorkingScheduleRequest schedule = new RegisterCareGiverRequest.WorkingScheduleRequest();
        schedule.setDayOfWeek(DayOfWeek.MONDAY);
        schedule.setStartTime(LocalTime.of(8, 0));
        schedule.setEndTime(LocalTime.of(16, 0));
        schedules.add(schedule);

        careGiverRequest = new RegisterCareGiverRequest();
        careGiverRequest.setEmail("caregiver@example.com");
        careGiverRequest.setPassword("password");
        careGiverRequest.setName("Dr. Test");
        careGiverRequest.setNik("6543210987654321");
        careGiverRequest.setAddress("Doctor Address");
        careGiverRequest.setPhoneNumber("089876543210");
        careGiverRequest.setSpeciality("General");
        careGiverRequest.setWorkAddress("Test Hospital");
        careGiverRequest.setWorkingSchedules(schedules);
    }

    @Test
    void createUser_WithValidRequest_ShouldCreatePacillian() {
        // given
        when(roleRepository.findByName(Role.ERole.ROLE_PACILLIAN)).thenReturn(Optional.of(pacillianRole));
        String encodedPassword = "encoded_password";

        // when
        User user = pacillianFactory.createUser(pacillianRequest, encodedPassword);

        // then
        assertThat(user).isNotNull().isInstanceOf(Pacillian.class);
        Pacillian pacillian = (Pacillian) user;

        // Verify basic user properties
        assertThat(pacillian.getEmail()).isEqualTo(pacillianRequest.getEmail());
        assertThat(pacillian.getPassword()).isEqualTo(encodedPassword);
        assertThat(pacillian.getName()).isEqualTo(pacillianRequest.getName());
        assertThat(pacillian.getNik()).isEqualTo(pacillianRequest.getNik());
        assertThat(pacillian.getAddress()).isEqualTo(pacillianRequest.getAddress());
        assertThat(pacillian.getPhoneNumber()).isEqualTo(pacillianRequest.getPhoneNumber());

        // Verify Pacillian-specific properties
        assertThat(pacillian.getMedicalHistory()).isEqualTo(pacillianRequest.getMedicalHistory());

        // Verify role assignment
        assertThat(pacillian.getRoles()).hasSize(1);
        assertThat(pacillian.getRoles().iterator().next().getName()).isEqualTo(Role.ERole.ROLE_PACILLIAN);
    }

    @Test
    void createUser_WithInvalidRequestType_ShouldThrowException() {
        // when/then
        assertThatThrownBy(() -> pacillianFactory.createUser(careGiverRequest, "encoded_password"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid request type for PacillianFactory");
    }

    @Test
    void createUser_WhenRoleNotFound_ShouldThrowException() {
        // given
        when(roleRepository.findByName(Role.ERole.ROLE_PACILLIAN)).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> pacillianFactory.createUser(pacillianRequest, "encoded_password"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Pacillian role not found");
    }
}