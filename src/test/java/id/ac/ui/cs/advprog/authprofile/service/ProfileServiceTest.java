package id.ac.ui.cs.advprog.authprofile.service;

import id.ac.ui.cs.advprog.authprofile.dto.request.UpdateProfileRequest;
import id.ac.ui.cs.advprog.authprofile.dto.response.ProfileResponse;
import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
import id.ac.ui.cs.advprog.authprofile.model.Role;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.repository.CareGiverRepository;
import id.ac.ui.cs.advprog.authprofile.repository.PacillianRepository;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PacillianRepository pacillianRepository;

    @Mock
    private CareGiverRepository careGiverRepository;

    @InjectMocks
    private ProfileService profileService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    private User user;
    private Pacillian pacillian;
    private CareGiver careGiver;
    private UpdateProfileRequest updateRequest;
    private List<CareGiver> careGivers;

    @BeforeEach
    void setUp() {
        // Setup test data
        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setName(Role.ERole.ROLE_PACILLIAN);
        roles.add(role);

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setNik("1234567890123456");
        user.setAddress("Test Address");
        user.setPhoneNumber("081234567890");
        user.setRoles(roles);

        pacillian = new Pacillian();
        pacillian.setId(2L);
        pacillian.setEmail("pacillian@example.com");
        pacillian.setName("Test Pacillian");
        pacillian.setNik("2345678901234567");
        pacillian.setAddress("Pacillian Address");
        pacillian.setPhoneNumber("082345678901");
        pacillian.setMedicalHistory("Test medical history");
        pacillian.setRoles(roles);

        careGiver = new CareGiver();
        careGiver.setId(3L);
        careGiver.setEmail("caregiver@example.com");
        careGiver.setName("Dr. Test");
        careGiver.setNik("3456789012345678");
        careGiver.setAddress("Doctor Address");
        careGiver.setPhoneNumber("083456789012");
        careGiver.setSpeciality("General");
        careGiver.setWorkAddress("Test Hospital");
        careGiver.setAverageRating(4.5);

        Set<Role> doctorRoles = new HashSet<>();
        Role doctorRole = new Role();
        doctorRole.setName(Role.ERole.ROLE_CAREGIVER);
        doctorRoles.add(doctorRole);
        careGiver.setRoles(doctorRoles);

        careGivers = new ArrayList<>();
        careGivers.add(careGiver);

        updateRequest = new UpdateProfileRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setAddress("Updated Address");
        updateRequest.setPhoneNumber("089876543210");
        updateRequest.setMedicalHistory("Updated medical history");
        updateRequest.setSpeciality("Updated speciality");
        updateRequest.setWorkAddress("Updated work address");
    }

    // Helper method to set up security context
    private void mockSecurityContext() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
    }

    @Test
    void getCurrentUserProfile_ShouldReturnProfileResponse() {
        // Set up security context for this specific test
        mockSecurityContext();

        // given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // when
        ProfileResponse response = profileService.getCurrentUserProfile();

        // then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getName()).isEqualTo("Test User");

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getCurrentUserProfile_WithNonExistentUser_ShouldThrowException() {
        // Set up security context for this specific test
        mockSecurityContext();

        // given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> profileService.getCurrentUserProfile())
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getCurrentUserEmail_WithStringPrincipal_ShouldReturnEmail() {
        // Set up SecurityContext with a string principal instead of UserDetails
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Call a method that uses getCurrentUserEmail internally
        ProfileResponse response = profileService.getCurrentUserProfile();

        // Verify the response is correct
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void getUserProfile_ShouldReturnProfileResponse() {
        // given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        // when
        ProfileResponse response = profileService.getUserProfile(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");

        verify(userRepository).findById(1L);
    }

    @Test
    void getUserProfile_WithNonExistentUser_ShouldThrowException() {
        // given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> profileService.getUserProfile(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");

        verify(userRepository).findById(999L);
    }

    @Test
    void getAllCareGivers_ShouldReturnListOfProfileResponses() {
        // given
        when(careGiverRepository.findAll()).thenReturn(careGivers);

        // when
        List<ProfileResponse> responses = profileService.getAllCareGivers();

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getEmail()).isEqualTo("caregiver@example.com");
        assertThat(responses.get(0).getSpeciality()).isEqualTo("General");

        verify(careGiverRepository).findAll();
    }

    @Test
    void searchCareGivers_ByNameAndSpeciality_ShouldReturnListOfProfileResponses() {
        // given
        when(careGiverRepository.findByNameAndSpeciality("test", "general")).thenReturn(careGivers);

        // when
        List<ProfileResponse> responses = profileService.searchCareGivers("test", "general");

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("Dr. Test");

        verify(careGiverRepository).findByNameAndSpeciality("test", "general");
    }

    @Test
    void searchCareGivers_ByNameOnly_ShouldReturnListOfProfileResponses() {
        // given
        when(careGiverRepository.findByNameContainingIgnoreCase("test")).thenReturn(careGivers);

        // when
        List<ProfileResponse> responses = profileService.searchCareGivers("test", null);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("Dr. Test");

        verify(careGiverRepository).findByNameContainingIgnoreCase("test");
    }

    @Test
    void searchCareGivers_BySpecialityOnly_ShouldReturnListOfProfileResponses() {
        // given
        when(careGiverRepository.findBySpecialityContainingIgnoreCase("general")).thenReturn(careGivers);

        // when
        List<ProfileResponse> responses = profileService.searchCareGivers(null, "general");

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getSpeciality()).isEqualTo("General");

        verify(careGiverRepository).findBySpecialityContainingIgnoreCase("general");
    }

    @Test
    void searchCareGivers_WithNoFilters_ShouldReturnAllCareGivers() {
        // given
        when(careGiverRepository.findAll()).thenReturn(careGivers);

        // when
        List<ProfileResponse> responses = profileService.searchCareGivers(null, null);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getEmail()).isEqualTo("caregiver@example.com");

        verify(careGiverRepository).findAll();
    }

    @Test
    void updateCurrentUserProfile_ForPacillian_ShouldReturnUpdatedProfile() {
        // Set up security context for this specific test
        mockSecurityContext();

        // given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(pacillian));
        when(pacillianRepository.save(any(Pacillian.class))).thenReturn(pacillian);

        // when
        ProfileResponse response = profileService.updateCurrentUserProfile(updateRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Updated Name");
        assertThat(response.getAddress()).isEqualTo("Updated Address");
        assertThat(response.getPhoneNumber()).isEqualTo("089876543210");
        assertThat(response.getMedicalHistory()).isEqualTo("Updated medical history");

        verify(userRepository).findByEmail("test@example.com");
        verify(pacillianRepository).save(any(Pacillian.class));
    }

    @Test
    void updateCurrentUserProfile_ForCareGiver_ShouldReturnUpdatedProfile() {
        // Set up security context for this specific test
        mockSecurityContext();

        // given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(careGiver));
        when(careGiverRepository.save(any(CareGiver.class))).thenReturn(careGiver);

        // when
        ProfileResponse response = profileService.updateCurrentUserProfile(updateRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Updated Name");
        assertThat(response.getAddress()).isEqualTo("Updated Address");
        assertThat(response.getPhoneNumber()).isEqualTo("089876543210");
        assertThat(response.getSpeciality()).isEqualTo("Updated speciality");
        assertThat(response.getWorkAddress()).isEqualTo("Updated work address");

        verify(userRepository).findByEmail("test@example.com");
        verify(careGiverRepository).save(any(CareGiver.class));
    }

    @Test
    void updateCurrentUserProfile_ForRegularUser_ShouldReturnUpdatedProfile() {
        // Set up security context for this specific test
        mockSecurityContext();

        // given - a regular User that is neither Pacillian nor CareGiver
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        ProfileResponse response = profileService.updateCurrentUserProfile(updateRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Updated Name");
        assertThat(response.getAddress()).isEqualTo("Updated Address");
        assertThat(response.getPhoneNumber()).isEqualTo("089876543210");

        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
        verify(pacillianRepository, never()).save(any(Pacillian.class));
        verify(careGiverRepository, never()).save(any(CareGiver.class));
    }

    @Test
    void deleteCurrentUserAccount_ShouldDeleteUser() {
        // Set up security context for this specific test
        mockSecurityContext();

        // given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // when
        profileService.deleteCurrentUserAccount();

        // then
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).delete(user);
    }

    @Test
    void deleteCurrentUserAccount_WithNonExistentUser_ShouldThrowException() {
        // Set up security context for this specific test
        mockSecurityContext();

        // given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> profileService.deleteCurrentUserAccount())
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void updateCurrentUserProfile_WithNonExistentUser_ShouldThrowException() {
        // Set up security context for this specific test
        mockSecurityContext();

        // given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> profileService.updateCurrentUserProfile(updateRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("test@example.com");
        // Verify that no save methods are called
        verify(userRepository, never()).save(any(User.class));
        verify(pacillianRepository, never()).save(any(Pacillian.class));
        verify(careGiverRepository, never()).save(any(CareGiver.class));
    }
}

