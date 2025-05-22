package id.ac.ui.cs.advprog.authprofile.service;

import id.ac.ui.cs.advprog.authprofile.dto.request.UpdateProfileRequest;
import id.ac.ui.cs.advprog.authprofile.dto.response.ProfileResponse;
import id.ac.ui.cs.advprog.authprofile.exception.EmailAlreadyExistsException;
import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
import id.ac.ui.cs.advprog.authprofile.model.Role;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.repository.CareGiverRepository;
import id.ac.ui.cs.advprog.authprofile.repository.PacillianRepository;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import id.ac.ui.cs.advprog.authprofile.security.jwt.JwtUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private IAuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PacillianRepository pacillianRepository;

    @Mock
    private CareGiverRepository careGiverRepository;

    @InjectMocks
    private ProfileServiceImpl profileServiceImpl;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    private User user;
    private Pacillian pacillian;
    private CareGiver careGiver;
    private CareGiver careGiver2;
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


        // Create a second caregiver for testing multiple results
        careGiver2 = new CareGiver();
        careGiver2.setId(4L);
        careGiver2.setEmail("doctor2@example.com");
        careGiver2.setName("Dr. Smith");
        careGiver2.setNik("4567890123456789");
        careGiver2.setAddress("Another Hospital Address");
        careGiver2.setPhoneNumber("084567890123");
        careGiver2.setSpeciality("Neurology");
        careGiver2.setWorkAddress("City Hospital");
        careGiver2.setAverageRating(4.8);
        careGiver2.setRoles(doctorRoles);

        careGivers = new ArrayList<>();
        careGivers.add(careGiver);
        careGivers.add(careGiver2);

        updateRequest = new UpdateProfileRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setAddress("Updated Address");
        updateRequest.setPhoneNumber("089876543210");
        updateRequest.setMedicalHistory("Updated medical history");
        updateRequest.setSpeciality("Updated speciality");
        updateRequest.setWorkAddress("Updated work address");

        SecurityContextHolder.clearContext();

    }

    @AfterEach
    void tearDown() {
        // Always clean up the security context after each test
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext() {
        // Create a clean security context
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

        // Configure authentication with lenient stubbings
        // This avoids UnnecessaryStubbingException when not all of these are used in every test
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        lenient().when(userDetails.getUsername()).thenReturn(user.getId().toString());

        // Set the authentication in the context
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserProfile_ShouldReturnProfileResponse() {
        // Set up security context for this specific test
        mockSecurityContext();

        // given - now using findById instead of findByEmail
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        ProfileResponse response = profileServiceImpl.getCurrentUserProfile();

        // then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getName()).isEqualTo("Test User");

        verify(userRepository).findById(1L);
    }

    @Test
    void getCurrentUserProfile_WithNonExistentUser_ShouldThrowException() {
        // Set up security context for this specific test
        mockSecurityContext();

        // given - now using findById instead of findByEmail
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> profileServiceImpl.getCurrentUserProfile())
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(1L);
    }


    @Test
    void getUserProfile_ShouldReturnProfileResponse() {
        // given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        // when
        ProfileResponse response = profileServiceImpl.getUserProfile(1L);

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
        assertThatThrownBy(() -> profileServiceImpl.getUserProfile(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");

        verify(userRepository).findById(999L);
    }

    @Test
    void getAllCareGivers_ShouldReturnListOfProfileResponses() {
        // given
        when(careGiverRepository.findAll()).thenReturn(careGivers);

        // when
        List<ProfileResponse> responses = profileServiceImpl.getAllCareGivers();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getEmail()).isEqualTo("caregiver@example.com");
        assertThat(responses.get(0).getSpeciality()).isEqualTo("General");
        assertThat(responses.get(1).getEmail()).isEqualTo("doctor2@example.com");
        assertThat(responses.get(1).getSpeciality()).isEqualTo("Neurology");

        verify(careGiverRepository).findAll();
    }

    @Test
    void searchCareGivers_ByNameAndSpeciality_ShouldReturnListOfProfileResponses() {
        // given
        when(careGiverRepository.findByNameAndSpeciality("test", "general")).thenReturn(Arrays.asList(careGiver));

        // when
        List<ProfileResponse> responses = profileServiceImpl.searchCareGivers("test", "general");

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("Dr. Test");

        verify(careGiverRepository).findByNameAndSpeciality("test", "general");
    }

    @Test
    void searchCareGivers_ByNameOnly_ShouldReturnListOfProfileResponses() {
        // given
        when(careGiverRepository.findByNameContainingIgnoreCase("test")).thenReturn(Arrays.asList(careGiver));

        // when
        List<ProfileResponse> responses = profileServiceImpl.searchCareGivers("test", null);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("Dr. Test");

        verify(careGiverRepository).findByNameContainingIgnoreCase("test");
    }

    @Test
    void searchCareGivers_BySpecialityOnly_ShouldReturnListOfProfileResponses() {
        // given
        when(careGiverRepository.findBySpecialityContainingIgnoreCase("general")).thenReturn(Arrays.asList(careGiver));

        // when
        List<ProfileResponse> responses = profileServiceImpl.searchCareGivers(null, "general");

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
        List<ProfileResponse> responses = profileServiceImpl.searchCareGivers(null, null);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getEmail()).isEqualTo("caregiver@example.com");
        assertThat(responses.get(1).getEmail()).isEqualTo("doctor2@example.com");

        verify(careGiverRepository).findAll();
    }



    @Test
    void updateCurrentUserProfile_ForPacillian_ShouldReturnUpdatedProfile() {
        // Set up security context
        mockSecurityContext();

        // Set up pacillian
        pacillian.setId(1L);
        pacillian.setEmail("test@example.com");

        // Set up request
        updateRequest.setEmail("test@example.com");
        updateRequest.setName("Updated Name");
        updateRequest.setAddress("Updated Address");
        updateRequest.setPhoneNumber("089876543210");
        updateRequest.setMedicalHistory("Updated Medical History");

        // Set up mocks
        when(userRepository.findById(1L)).thenReturn(Optional.of(pacillian));
        when(pacillianRepository.save(any(Pacillian.class))).thenReturn(pacillian);

        // Execute method
        ProfileResponse response = profileServiceImpl.updateCurrentUserProfile(updateRequest);

        // Verify response
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Updated Name");
        assertThat(response.getAddress()).isEqualTo("Updated Address");
        assertThat(response.getPhoneNumber()).isEqualTo("089876543210");
        assertThat(response.getMedicalHistory()).isEqualTo("Updated Medical History");

        // Verify calls
        verify(userRepository, times(1)).findById(1L);
        verify(pacillianRepository).save(any(Pacillian.class));
        verify(pacillianRepository).flush();

        // Since we're not changing the email, this shouldn't be called
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void updateCurrentUserProfile_ForRegularUser_ShouldReturnUpdatedProfile() {
        // Set up security context for this specific test
        mockSecurityContext();

        // given - a regular User that is neither Pacillian nor CareGiver
        // Return the user for both calls to findById
        when(userRepository.findById(1L)).thenReturn(Optional.of(user))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail("new.email@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Setup the update request with a new email
        updateRequest.setEmail("new.email@example.com");

        // when
        ProfileResponse response = profileServiceImpl.updateCurrentUserProfile(updateRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Updated Name");
        assertThat(response.getAddress()).isEqualTo("Updated Address");
        assertThat(response.getPhoneNumber()).isEqualTo("089876543210");

        // Verify findById was called twice (since we're changing email)
        verify(userRepository, times(2)).findById(1L);
        verify(userRepository).existsByEmail("new.email@example.com");
        verify(userRepository).save(any(User.class));
        verify(pacillianRepository, never()).save(any(Pacillian.class));
        verify(careGiverRepository, never()).save(any(CareGiver.class));
    }

    @Test
    void deleteCurrentUserAccount_ShouldDeleteUser() {
        // Set up security context
        mockSecurityContext();

        // Set up repository mock for ID-based lookup
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Execute the method
        profileServiceImpl.deleteCurrentUserAccount();

        // Verify the repository calls - now using findById
        verify(userRepository).findById(1L);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteCurrentUserAccount_WithNonExistentUser_ShouldThrowException() {
        // Set up security context
        mockSecurityContext();

        // Set up repository mock for ID-based lookup
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Execute and verify exception
        assertThatThrownBy(() -> profileServiceImpl.deleteCurrentUserAccount())
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(1L);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void updateCurrentUserProfile_WithNonExistentUser_ShouldThrowException() {
        // Set up security context for this specific test
        mockSecurityContext();

        // given - now using findById instead of findByEmail
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> profileServiceImpl.updateCurrentUserProfile(updateRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(1L);
        // Verify that no save methods are called
        verify(userRepository, never()).save(any(User.class));
        verify(pacillianRepository, never()).save(any(Pacillian.class));
        verify(careGiverRepository, never()).save(any(CareGiver.class));
    }

    // New tests for the getAllCareGiversLite method
    @Test
    void getAllCareGiversLite_ShouldReturnLiteProfileResponses() {
        // given
        when(careGiverRepository.findAll()).thenReturn(careGivers);

        // when
        List<ProfileResponse> responses = profileServiceImpl.getAllCareGiversLite();

        // then
        assertThat(responses).hasSize(2);

        // Verify first caregiver response
        ProfileResponse firstResponse = responses.get(0);
        assertThat(firstResponse.getId()).isEqualTo(careGiver.getId());
        assertThat(firstResponse.getName()).isEqualTo(careGiver.getName());
        assertThat(firstResponse.getEmail()).isEqualTo(careGiver.getEmail());
        assertThat(firstResponse.getPhoneNumber()).isEqualTo(careGiver.getPhoneNumber());
        assertThat(firstResponse.getSpeciality()).isEqualTo(careGiver.getSpeciality());
        assertThat(firstResponse.getWorkAddress()).isEqualTo(careGiver.getWorkAddress());
        assertThat(firstResponse.getAverageRating()).isEqualTo(careGiver.getAverageRating());

        // Verify sensitive info is null
        assertThat(firstResponse.getNik()).isNull();
        assertThat(firstResponse.getAddress()).isNull();



        // Verify second caregiver response
        ProfileResponse secondResponse = responses.get(1);
        assertThat(secondResponse.getId()).isEqualTo(careGiver2.getId());
        assertThat(secondResponse.getName()).isEqualTo(careGiver2.getName());
        assertThat(secondResponse.getEmail()).isEqualTo(careGiver2.getEmail());


        verify(careGiverRepository).findAll();
    }

    // New tests for all searchCareGiversLite method cases
    @Test
    void searchCareGiversLite_ByNameAndSpeciality_ShouldReturnLiteProfiles() {
        // given
        when(careGiverRepository.findByNameAndSpeciality("test", "general")).thenReturn(Arrays.asList(careGiver));

        // when
        List<ProfileResponse> responses = profileServiceImpl.searchCareGiversLite("test", "general");

        // then
        assertThat(responses).hasSize(1);

        ProfileResponse response = responses.get(0);
        assertThat(response.getId()).isEqualTo(careGiver.getId());
        assertThat(response.getName()).isEqualTo(careGiver.getName());
        assertThat(response.getEmail()).isEqualTo(careGiver.getEmail());
        assertThat(response.getPhoneNumber()).isEqualTo(careGiver.getPhoneNumber());
        assertThat(response.getSpeciality()).isEqualTo(careGiver.getSpeciality());
        assertThat(response.getNik()).isNull();
        assertThat(response.getAddress()).isNull();

        verify(careGiverRepository).findByNameAndSpeciality("test", "general");
    }

    @Test
    void searchCareGiversLite_ByNameOnly_ShouldReturnLiteProfiles() {
        // given
        when(careGiverRepository.findByNameContainingIgnoreCase("test")).thenReturn(Arrays.asList(careGiver));

        // when
        List<ProfileResponse> responses = profileServiceImpl.searchCareGiversLite("test", null);

        // then
        assertThat(responses).hasSize(1);

        ProfileResponse response = responses.get(0);
        assertThat(response.getId()).isEqualTo(careGiver.getId());
        assertThat(response.getName()).isEqualTo(careGiver.getName());
        assertThat(response.getEmail()).isEqualTo(careGiver.getEmail());
        assertThat(response.getNik()).isNull();

        verify(careGiverRepository).findByNameContainingIgnoreCase("test");
    }

    @Test
    void searchCareGiversLite_BySpecialityOnly_ShouldReturnLiteProfiles() {
        // given
        when(careGiverRepository.findBySpecialityContainingIgnoreCase("general")).thenReturn(Arrays.asList(careGiver));

        // when
        List<ProfileResponse> responses = profileServiceImpl.searchCareGiversLite(null, "general");

        // then
        assertThat(responses).hasSize(1);

        ProfileResponse response = responses.get(0);
        assertThat(response.getId()).isEqualTo(careGiver.getId());
        assertThat(response.getSpeciality()).isEqualTo(careGiver.getSpeciality());
        assertThat(response.getEmail()).isEqualTo(careGiver.getEmail());
        assertThat(response.getNik()).isNull();

        verify(careGiverRepository).findBySpecialityContainingIgnoreCase("general");
    }

    @Test
    void searchCareGiversLite_WithNoFilters_ShouldReturnAllLiteProfiles() {
        // given
        when(careGiverRepository.findAll()).thenReturn(careGivers);

        // when
        List<ProfileResponse> responses = profileServiceImpl.searchCareGiversLite(null, null);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getEmail()).isEqualTo(careGiver.getEmail());
        assertThat(responses.get(1).getEmail()).isEqualTo(careGiver2.getEmail());

        // Verify all responses have sensitive info nullified
        for (ProfileResponse response : responses) {
            assertThat(response.getNik()).isNull();
            assertThat(response.getAddress()).isNull();
        }

        verify(careGiverRepository).findAll();
    }

    @Test
    void createLiteProfileResponse_WithNullWorkingSchedules_ShouldHandleGracefully() {
        // given
        CareGiver careGiverWithNullSchedules = new CareGiver();
        careGiverWithNullSchedules.setId(5L);
        careGiverWithNullSchedules.setEmail("nullschedules@example.com");
        careGiverWithNullSchedules.setName("Dr. Null");

        List<CareGiver> careGiversWithNull = Arrays.asList(careGiverWithNullSchedules);
        when(careGiverRepository.findAll()).thenReturn(careGiversWithNull);

        // when
        List<ProfileResponse> responses = profileServiceImpl.getAllCareGiversLite();

        // then
        assertThat(responses).hasSize(1);


        verify(careGiverRepository).findAll();
    }

    @Test
    void getCareGiverProfileLite_ShouldReturnLiteProfileResponse() {
        // given
        Long caregiverId = 3L;
        when(careGiverRepository.findById(caregiverId)).thenReturn(Optional.of(careGiver));

        // when
        ProfileResponse response = profileServiceImpl.getCareGiverProfileLite(caregiverId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(caregiverId);
        assertThat(response.getName()).isEqualTo(careGiver.getName());
        assertThat(response.getEmail()).isEqualTo(careGiver.getEmail());
        assertThat(response.getPhoneNumber()).isEqualTo(careGiver.getPhoneNumber());
        assertThat(response.getSpeciality()).isEqualTo(careGiver.getSpeciality());
        assertThat(response.getWorkAddress()).isEqualTo(careGiver.getWorkAddress());

        // Verify sensitive info is null
        assertThat(response.getNik()).isNull();
        assertThat(response.getAddress()).isNull();



        verify(careGiverRepository).findById(caregiverId);
    }

    @Test
    void getCareGiverProfileLite_WithNonExistentId_ShouldThrowException() {
        // given
        Long nonExistentId = 999L;
        when(careGiverRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> profileServiceImpl.getCareGiverProfileLite(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Caregiver not found with id: " + nonExistentId);

        verify(careGiverRepository).findById(nonExistentId);
    }

    @Test
    void getCareGiverProfileLite_WithRegularUserId_ShouldThrowException() {
        // given
        Long userId = 1L;

        // Mock that the repository returns empty for this ID (no caregiver found)
        when(careGiverRepository.findById(userId)).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> profileServiceImpl.getCareGiverProfileLite(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Caregiver not found with id: " + userId);

        verify(careGiverRepository).findById(userId);
    }











    @Test
    void updateCurrentUserProfile_NoEmailChange_ForPacillian() {
        // Set up security context
        mockSecurityContext();

        // Important: Set pacillian's ID to match the security context ID
        pacillian.setId(1L);
        pacillian.setEmail("test@example.com");

        // Create update request with same email as security context
        updateRequest.setEmail("test@example.com");
        updateRequest.setName("Updated Name");
        updateRequest.setAddress("Updated Address");
        updateRequest.setPhoneNumber("089876543210");
        updateRequest.setMedicalHistory("Updated Medical History");

        // Setup mocks - now using findById instead of findByEmail
        when(userRepository.findById(1L)).thenReturn(Optional.of(pacillian));
        when(pacillianRepository.save(any(Pacillian.class))).thenReturn(pacillian);

        // Execute
        ProfileResponse response = profileServiceImpl.updateCurrentUserProfile(updateRequest);

        // Verify
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Updated Name");
        assertThat(response.getMedicalHistory()).isEqualTo("Updated Medical History");

        verify(userRepository).findById(1L);
        verify(pacillianRepository).save(any(Pacillian.class));
        verify(pacillianRepository).flush();
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void updateCurrentUserProfile_NoEmailChange_ForCareGiver() {
        // Set up security context
        mockSecurityContext();

        // Important: Set careGiver's ID to match the security context ID
        careGiver.setId(1L);
        careGiver.setEmail("test@example.com");

        // Create update request with same email as in the security context
        updateRequest.setEmail("test@example.com");
        updateRequest.setName("Updated Name");
        updateRequest.setAddress("Updated Address");
        updateRequest.setPhoneNumber("089876543210");
        updateRequest.setSpeciality("Updated Speciality");
        updateRequest.setWorkAddress("Updated Work Address");

        // Setup mocks - now using findById instead of findByEmail
        when(userRepository.findById(1L)).thenReturn(Optional.of(careGiver));
        when(careGiverRepository.save(any(CareGiver.class))).thenReturn(careGiver);

        // Execute
        ProfileResponse response = profileServiceImpl.updateCurrentUserProfile(updateRequest);

        // Verify
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Updated Name");
        assertThat(response.getSpeciality()).isEqualTo("Updated Speciality");
        assertThat(response.getWorkAddress()).isEqualTo("Updated Work Address");

        verify(userRepository).findById(1L);
        verify(careGiverRepository).save(any(CareGiver.class));
        verify(careGiverRepository).flush();
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void updateCurrentUserProfile_NoEmailChange_ForRegularUser() {
        // Set up security context
        mockSecurityContext();

        // Create update request with same email
        updateRequest.setEmail(user.getEmail());
        updateRequest.setName("Updated Name");
        updateRequest.setAddress("Updated Address");
        updateRequest.setPhoneNumber("089876543210");

        // Setup mocks - now using findById instead of findByEmail
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Execute
        ProfileResponse response = profileServiceImpl.updateCurrentUserProfile(updateRequest);

        // Verify
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Updated Name");

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
        verify(userRepository).flush();
        verify(pacillianRepository, never()).save(any());
        verify(careGiverRepository, never()).save(any());
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void updateCurrentUserProfile_WithEmailChange_ForRegularUser_Success() {
        // Set up security context
        mockSecurityContext();

        // Create update request with new email
        String newEmail = "newemail@example.com";
        updateRequest.setEmail(newEmail);
        updateRequest.setName("Updated Name");
        updateRequest.setAddress("Updated Address");
        updateRequest.setPhoneNumber("089876543210");

        // Setup mocks - using findById instead of findByEmail
        // Return the user for both calls to findById
        when(userRepository.findById(1L)).thenReturn(Optional.of(user))
                .thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Mock the JWT token generation with user ID
        when(jwtUtils.generateJwtTokenFromUserId(user.getId().toString())).thenReturn("new-jwt-token");

        // Mock ServletRequestAttributes and HttpServletResponse
        ServletRequestAttributes requestAttributes = mock(ServletRequestAttributes.class);
        HttpServletResponse httpResponse = mock(HttpServletResponse.class);
        when(requestAttributes.getResponse()).thenReturn(httpResponse);
        RequestContextHolder.setRequestAttributes(requestAttributes);

        // Execute
        ProfileResponse response = profileServiceImpl.updateCurrentUserProfile(updateRequest);

        // Verify
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Updated Name");

        // Verify the repository calls with user ID - now expecting 2 calls to findById
        verify(userRepository, times(2)).findById(1L);
        verify(userRepository).existsByEmail(newEmail);
        verify(userRepository).save(any(User.class));
        verify(userRepository).flush();

        // Verify token generation using user ID
        verify(jwtUtils).generateJwtTokenFromUserId(user.getId().toString());

        // Instead of verifying the exact value, just verify that the headers are set
        verify(httpResponse).setHeader(eq("Authorization"), anyString());
        verify(httpResponse).setHeader("X-Email-Changed", "true");

        // Reset RequestContextHolder
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void updateCurrentUserProfile_WithEmailChange_EmailAlreadyExists() {
        // Set up security context
        mockSecurityContext();

        // Create update request with new email
        String newEmail = "existing@example.com";
        updateRequest.setEmail(newEmail);

        // Setup mocks - using findById instead of findByEmail
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(newEmail)).thenReturn(true);

        // Execute and verify exception
        assertThatThrownBy(() -> profileServiceImpl.updateCurrentUserProfile(updateRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("Email is already in use");

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail(newEmail);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateCurrentUserProfile_WithEmailChange_NullRequestAttributes() {
        // Set up security context
        mockSecurityContext();

        // Create update request with new email
        String newEmail = "newemail@example.com";
        updateRequest.setEmail(newEmail);

        // Setup mocks
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user))
                .thenReturn(Optional.of(user)); // Return user for both calls
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Set null request attributes
        RequestContextHolder.resetRequestAttributes();

        // Execute
        ProfileResponse response = profileServiceImpl.updateCurrentUserProfile(updateRequest);

        // Verify
        assertThat(response).isNotNull();

        // Verify findById was called twice (once for initial lookup, once after email change)
        verify(userRepository, times(2)).findById(user.getId());
    }

    @Test
    void updateCurrentUserProfile_WithEmailChange_NullHttpResponse() {
        // Set up security context
        mockSecurityContext();

        // Create update request with new email
        String newEmail = "newemail@example.com";
        updateRequest.setEmail(newEmail);

        // Set up user
        user.setId(1L);
        user.setEmail("test@example.com");

        // Setup mocks
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Mock ServletRequestAttributes but with null HttpResponse
        ServletRequestAttributes requestAttributes = mock(ServletRequestAttributes.class);
        when(requestAttributes.getResponse()).thenReturn(null);
        RequestContextHolder.setRequestAttributes(requestAttributes);

        // Execute
        ProfileResponse response = profileServiceImpl.updateCurrentUserProfile(updateRequest);

        // Verify
        assertThat(response).isNotNull();
        verify(requestAttributes).getResponse();

        // Verify repository calls
        verify(userRepository, times(2)).findById(1L); // Changed from times(1) to times(2)
        verify(userRepository).existsByEmail(newEmail);
        verify(userRepository).save(any(User.class));

        // Reset RequestContextHolder
        RequestContextHolder.resetRequestAttributes();
    }


    @Test
    void updateCurrentUserProfile_UserNotFoundAfterUpdate() {
        // Set up security context
        mockSecurityContext();

        // Create update request with new email
        String newEmail = "newemail@example.com";
        updateRequest.setEmail(newEmail);

        // Setup mocks - now using findById instead of findByEmail for both calls
        // First findById call returns user, second returns empty
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user))  // First call returns user
                .thenReturn(Optional.empty());  // Second call returns empty
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Execute and verify exception
        assertThatThrownBy(() -> profileServiceImpl.updateCurrentUserProfile(updateRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found after update");

        // Verify findById was called twice
        verify(userRepository, times(2)).findById(user.getId());
    }

    @Test
    void getUserName_ShouldReturnUserName() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        String userName = profileServiceImpl.getUserName(1L);

        // then
        assertThat(userName).isNotNull();
        assertThat(userName).isEqualTo("Test User");

        verify(userRepository).findById(1L);
    }

    @Test
    void getUserName_WithNonExistentUser_ShouldThrowException() {
        // given
        Long nonExistentUserId = 999L;
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> profileServiceImpl.getUserName(nonExistentUserId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with id: " + nonExistentUserId);

        verify(userRepository).findById(nonExistentUserId);
    }

    @Test
    void getUserName_WithPacillian_ShouldReturnPacillianName() {
        // given
        when(userRepository.findById(2L)).thenReturn(Optional.of(pacillian));

        // when
        String userName = profileServiceImpl.getUserName(2L);

        // then
        assertThat(userName).isNotNull();
        assertThat(userName).isEqualTo("Test Pacillian");

        verify(userRepository).findById(2L);
    }

    @Test
    void getUserName_WithCareGiver_ShouldReturnCareGiverName() {
        // given
        when(userRepository.findById(3L)).thenReturn(Optional.of(careGiver));

        // when
        String userName = profileServiceImpl.getUserName(3L);

        // then
        assertThat(userName).isNotNull();
        assertThat(userName).isEqualTo("Dr. Test");

        verify(userRepository).findById(3L);
    }


}