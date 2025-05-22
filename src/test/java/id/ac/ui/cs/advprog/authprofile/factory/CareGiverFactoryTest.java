package id.ac.ui.cs.advprog.authprofile.factory;

import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterCareGiverRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterPacillianRequest;
import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
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
class CareGiverFactoryTest {

    @Mock
    private RoleRepository roleRepository;

    private CareGiverFactory careGiverFactory;
    private RegisterCareGiverRequest careGiverRequest;
    private RegisterPacillianRequest pacillianRequest;
    private Role careGiverRole;

    @BeforeEach
    void setUp() {
        careGiverFactory = new CareGiverFactory(roleRepository);

        // Set up roles
        careGiverRole = new Role();
        careGiverRole.setId(2);
        careGiverRole.setName(Role.ERole.ROLE_CAREGIVER);

        // Set up pacillian request for testing invalid type
        pacillianRequest = new RegisterPacillianRequest();
        pacillianRequest.setEmail("pacillian@example.com");
        pacillianRequest.setPassword("password");
        pacillianRequest.setName("Test Pacillian");
        pacillianRequest.setNik("1234567890123456");
        pacillianRequest.setAddress("Test Address");
        pacillianRequest.setPhoneNumber("081234567890");
        pacillianRequest.setMedicalHistory("No significant history");



        // Set up caregiver request
        careGiverRequest = new RegisterCareGiverRequest();
        careGiverRequest.setEmail("caregiver@example.com");
        careGiverRequest.setPassword("password");
        careGiverRequest.setName("Dr. Test");
        careGiverRequest.setNik("6543210987654321");
        careGiverRequest.setAddress("Doctor Address");
        careGiverRequest.setPhoneNumber("089876543210");
        careGiverRequest.setSpeciality("General");
        careGiverRequest.setWorkAddress("Test Hospital");
    }

    @Test
    void createUser_WithValidRequest_ShouldCreateCareGiver() {
        // given
        when(roleRepository.findByName(Role.ERole.ROLE_CAREGIVER)).thenReturn(Optional.of(careGiverRole));
        String encodedPassword = "encoded_password";

        // when
        User user = careGiverFactory.createUser(careGiverRequest, encodedPassword);

        // then
        assertThat(user).isNotNull().isInstanceOf(CareGiver.class);
        CareGiver careGiver = (CareGiver) user;

        // Verify basic user properties
        assertThat(careGiver.getEmail()).isEqualTo(careGiverRequest.getEmail());
        assertThat(careGiver.getPassword()).isEqualTo(encodedPassword);
        assertThat(careGiver.getName()).isEqualTo(careGiverRequest.getName());
        assertThat(careGiver.getNik()).isEqualTo(careGiverRequest.getNik());
        assertThat(careGiver.getAddress()).isEqualTo(careGiverRequest.getAddress());
        assertThat(careGiver.getPhoneNumber()).isEqualTo(careGiverRequest.getPhoneNumber());

        // Verify CareGiver-specific properties
        assertThat(careGiver.getSpeciality()).isEqualTo(careGiverRequest.getSpeciality());
        assertThat(careGiver.getWorkAddress()).isEqualTo(careGiverRequest.getWorkAddress());

        // Verify role assignment
        assertThat(careGiver.getRoles()).hasSize(1);
        assertThat(careGiver.getRoles().iterator().next().getName()).isEqualTo(Role.ERole.ROLE_CAREGIVER);
    }



    @Test
    void createUser_WithInvalidRequestType_ShouldThrowException() {
        // when/then
        assertThatThrownBy(() -> careGiverFactory.createUser(pacillianRequest, "encoded_password"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid request type for CareGiverFactory");
    }

    @Test
    void createUser_WhenRoleNotFound_ShouldThrowException() {
        // given
        when(roleRepository.findByName(Role.ERole.ROLE_CAREGIVER)).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> careGiverFactory.createUser(careGiverRequest, "encoded_password"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("CareGiver role not found");
    }
}