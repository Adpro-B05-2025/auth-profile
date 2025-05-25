package id.ac.ui.cs.advprog.authprofile.dto;

import id.ac.ui.cs.advprog.authprofile.dto.response.ProfileResponse;
import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
import id.ac.ui.cs.advprog.authprofile.model.Role;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileResponseTest {

    @Test
    void fromUser_WithPacillian_ShouldMapAllFields() {
        // given
        Role role = new Role();
        role.setName(Role.ERole.ROLE_PACILLIAN);

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        Pacillian pacillian = new Pacillian();
        pacillian.setId(1L);
        pacillian.setEmail("pacillian@example.com");
        pacillian.setPassword("password");
        pacillian.setName("Test Pacillian");
        pacillian.setNik("1234567890123456");
        pacillian.setAddress("Test Address");
        pacillian.setPhoneNumber("081234567890");
        pacillian.setMedicalHistory("Test medical history");
        pacillian.setRoles(roles);

        // when
        ProfileResponse response = ProfileResponse.fromUser(pacillian);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("pacillian@example.com");
        assertThat(response.getName()).isEqualTo("Test Pacillian");
        assertThat(response.getNik()).isEqualTo("1234567890123456");
        assertThat(response.getAddress()).isEqualTo("Test Address");
        assertThat(response.getPhoneNumber()).isEqualTo("081234567890");
        assertThat(response.getMedicalHistory()).isEqualTo("Test medical history");
        assertThat(response.getUserType()).isEqualTo("PACILLIAN");
        assertThat(response.getRoles()).containsExactly("ROLE_PACILLIAN");

        // These should be null for a Pacillian
        assertThat(response.getSpeciality()).isNull();
        assertThat(response.getWorkAddress()).isNull();
        assertThat(response.getAverageRating()).isNull();
    }

    @Test
    void fromUser_WithCareGiver_ShouldMapAllFields() {
        // given
        Role role = new Role();
        role.setName(Role.ERole.ROLE_CAREGIVER);

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        CareGiver careGiver = new CareGiver();
        careGiver.setId(2L);
        careGiver.setEmail("caregiver@example.com");
        careGiver.setPassword("password");
        careGiver.setName("Dr. Test");
        careGiver.setNik("2345678901234567");
        careGiver.setAddress("Doctor Address");
        careGiver.setPhoneNumber("082345678901");
        careGiver.setSpeciality("Cardiology");
        careGiver.setWorkAddress("Heart Hospital");
        careGiver.setAverageRating(4.5);
        careGiver.setRoles(roles);


        // when
        ProfileResponse response = ProfileResponse.fromUser(careGiver);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getEmail()).isEqualTo("caregiver@example.com");
        assertThat(response.getName()).isEqualTo("Dr. Test");
        assertThat(response.getNik()).isEqualTo("2345678901234567");
        assertThat(response.getAddress()).isEqualTo("Doctor Address");
        assertThat(response.getPhoneNumber()).isEqualTo("082345678901");
        assertThat(response.getUserType()).isEqualTo("CAREGIVER");
        assertThat(response.getRoles()).containsExactly("ROLE_CAREGIVER");

        // CareGiver specific fields
        assertThat(response.getSpeciality()).isEqualTo("Cardiology");
        assertThat(response.getWorkAddress()).isEqualTo("Heart Hospital");
        assertThat(response.getAverageRating()).isEqualTo(4.5);

        // This should be null for a CareGiver
        assertThat(response.getMedicalHistory()).isNull();
    }
}