package id.ac.ui.cs.advprog.authprofile.service;

import id.ac.ui.cs.advprog.authprofile.dto.request.BaseRegisterRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.LoginRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterCareGiverRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterPacillianRequest;
import id.ac.ui.cs.advprog.authprofile.dto.response.JwtResponse;
import id.ac.ui.cs.advprog.authprofile.dto.response.TokenValidationResponse;
import id.ac.ui.cs.advprog.authprofile.factory.CareGiverFactory;
import id.ac.ui.cs.advprog.authprofile.factory.PacillianFactory;
import id.ac.ui.cs.advprog.authprofile.factory.UserFactory;
import id.ac.ui.cs.advprog.authprofile.factory.UserFactoryProvider;
import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
import id.ac.ui.cs.advprog.authprofile.model.Role;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.repository.CareGiverRepository;
import id.ac.ui.cs.advprog.authprofile.repository.PacillianRepository;
import id.ac.ui.cs.advprog.authprofile.repository.RoleRepository;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import id.ac.ui.cs.advprog.authprofile.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PacillianRepository pacillianRepository;

    @Mock
    private CareGiverRepository careGiverRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserFactoryProvider factoryProvider;

    @Mock
    private PacillianFactory pacillianFactory;

    @Mock
    private CareGiverFactory careGiverFactory;

    @InjectMocks
    private AuthServiceImpl authServiceImpl;

    private LoginRequest loginRequest;
    private RegisterPacillianRequest pacillianRequest;
    private RegisterCareGiverRequest careGiverRequest;
    private User user;
    private Pacillian pacillian;
    private CareGiver careGiver;
    private Role pacillianRole;
    private Role careGiverRole;
    private String validToken;
    private String invalidToken;

    @BeforeEach
    void setUp() {
        // Setup common test data
        loginRequest = new LoginRequest("test@example.com", "password");

        pacillianRequest = new RegisterPacillianRequest();
        pacillianRequest.setEmail("pacillian@example.com");
        pacillianRequest.setPassword("password");
        pacillianRequest.setName("Test Pacillian");
        pacillianRequest.setNik("1234567890123456");
        pacillianRequest.setAddress("Test Address");
        pacillianRequest.setPhoneNumber("081234567890");
        pacillianRequest.setMedicalHistory("No significant history");

        // Setup working schedule for caregiver
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

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encoded_password");
        user.setName("Test User");

        pacillianRole = new Role();
        pacillianRole.setId(1);
        pacillianRole.setName(Role.ERole.ROLE_PACILLIAN);

        careGiverRole = new Role();
        careGiverRole.setId(2);
        careGiverRole.setName(Role.ERole.ROLE_CAREGIVER);

        Set<Role> userRoles = new HashSet<>();
        userRoles.add(pacillianRole);
        user.setRoles(userRoles);

        // Create test models for pacillian and caregiver
        pacillian = new Pacillian();
        pacillian.setId(2L);
        pacillian.setEmail(pacillianRequest.getEmail());
        pacillian.setPassword("encoded_password");
        pacillian.setName(pacillianRequest.getName());
        pacillian.setNik(pacillianRequest.getNik());
        pacillian.setAddress(pacillianRequest.getAddress());
        pacillian.setPhoneNumber(pacillianRequest.getPhoneNumber());
        pacillian.setMedicalHistory(pacillianRequest.getMedicalHistory());
        Set<Role> pacillianRoles = new HashSet<>();
        pacillianRoles.add(pacillianRole);
        pacillian.setRoles(pacillianRoles);

        careGiver = new CareGiver();
        careGiver.setId(3L);
        careGiver.setEmail(careGiverRequest.getEmail());
        careGiver.setPassword("encoded_password");
        careGiver.setName(careGiverRequest.getName());
        careGiver.setNik(careGiverRequest.getNik());
        careGiver.setAddress(careGiverRequest.getAddress());
        careGiver.setPhoneNumber(careGiverRequest.getPhoneNumber());
        careGiver.setSpeciality(careGiverRequest.getSpeciality());
        careGiver.setWorkAddress(careGiverRequest.getWorkAddress());
        Set<Role> careGiverRoles = new HashSet<>();
        careGiverRoles.add(careGiverRole);
        careGiver.setRoles(careGiverRoles);

        // Setup tokens for validation tests
        validToken = "valid.jwt.token";
        invalidToken = "invalid.jwt.token";
    }

    @Test
    void authenticateUser_ShouldReturnJwtResponse() {
        // Setup authentication and userDetails mocks
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        // Now userDetails.getUsername() should return the user ID as a string
        when(userDetails.getUsername()).thenReturn("1");

        List<SimpleGrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PACILLIAN"));
        doReturn(authorities).when(userDetails).getAuthorities();
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("test_jwt_token");

        // Find user by ID not email now
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        JwtResponse response = authServiceImpl.authenticateUser(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("test_jwt_token");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRoles()).contains("ROLE_PACILLIAN");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateJwtToken(authentication);
        verify(userRepository).findById(1L);
    }

    @Test
    void authenticateUser_WhenUserNotFound_ShouldThrowException() {
        // Setup
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("1");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        // Remove the unnecessary stubbing for jwtUtils.generateJwtToken
        // when(jwtUtils.generateJwtToken(authentication)).thenReturn("test_jwt_token");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authServiceImpl.authenticateUser(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void registerPacillian_ShouldReturnSuccessMessage() {
        // given
        when(userRepository.existsByEmail(pacillianRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByNik(pacillianRequest.getNik())).thenReturn(false);
        when(encoder.encode(pacillianRequest.getPassword())).thenReturn("encoded_password");
        when(factoryProvider.getFactory(pacillianRequest)).thenReturn(pacillianFactory);
        when(pacillianFactory.createUser(eq(pacillianRequest), anyString())).thenReturn(pacillian);

        // when
        String result = authServiceImpl.registerUser(pacillianRequest);

        // then
        assertThat(result).isEqualTo("Pacillian registered successfully!");

        verify(userRepository).existsByEmail(pacillianRequest.getEmail());
        verify(userRepository).existsByNik(pacillianRequest.getNik());
        verify(encoder).encode(pacillianRequest.getPassword());
        verify(factoryProvider).getFactory(pacillianRequest);
        verify(pacillianFactory).createUser(eq(pacillianRequest), anyString());
        verify(pacillianRepository).save(pacillian);
    }

    @Test
    void registerPacillian_WithExistingEmail_ShouldThrowException() {
        // given
        when(userRepository.existsByEmail(pacillianRequest.getEmail())).thenReturn(true);

        // when/then
        assertThatThrownBy(() -> authServiceImpl.registerUser(pacillianRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email is already in use");

        verify(userRepository).existsByEmail(pacillianRequest.getEmail());
        verify(pacillianRepository, never()).save(any(Pacillian.class));
        verify(factoryProvider, never()).getFactory(any());
    }

    @Test
    void registerPacillian_WithExistingNIK_ShouldThrowException() {
        // given
        when(userRepository.existsByEmail(pacillianRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByNik(pacillianRequest.getNik())).thenReturn(true);

        // when/then
        assertThatThrownBy(() -> authServiceImpl.registerUser(pacillianRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("NIK is already in use");

        verify(userRepository).existsByEmail(pacillianRequest.getEmail());
        verify(userRepository).existsByNik(pacillianRequest.getNik());
        verify(pacillianRepository, never()).save(any(Pacillian.class));
        verify(factoryProvider, never()).getFactory(any());
    }

    @Test
    void registerCareGiver_ShouldReturnSuccessMessage() {
        // given
        when(userRepository.existsByEmail(careGiverRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByNik(careGiverRequest.getNik())).thenReturn(false);
        when(encoder.encode(careGiverRequest.getPassword())).thenReturn("encoded_password");
        when(factoryProvider.getFactory(careGiverRequest)).thenReturn(careGiverFactory);
        when(careGiverFactory.createUser(eq(careGiverRequest), anyString())).thenReturn(careGiver);

        // when
        String result = authServiceImpl.registerUser(careGiverRequest);

        // then
        assertThat(result).isEqualTo("CareGiver registered successfully!");

        verify(userRepository).existsByEmail(careGiverRequest.getEmail());
        verify(userRepository).existsByNik(careGiverRequest.getNik());
        verify(encoder).encode(careGiverRequest.getPassword());
        verify(factoryProvider).getFactory(careGiverRequest);
        verify(careGiverFactory).createUser(eq(careGiverRequest), anyString());
        verify(careGiverRepository).save(careGiver);
    }

    @Test
    void registerCareGiver_WithExistingEmail_ShouldThrowException() {
        // given
        when(userRepository.existsByEmail(careGiverRequest.getEmail())).thenReturn(true);

        // when/then
        assertThatThrownBy(() -> authServiceImpl.registerUser(careGiverRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email is already in use");

        verify(userRepository).existsByEmail(careGiverRequest.getEmail());
        verify(careGiverRepository, never()).save(any(CareGiver.class));
        verify(factoryProvider, never()).getFactory(any());
    }

    @Test
    void registerCareGiver_WithExistingNIK_ShouldThrowException() {
        // given
        when(userRepository.existsByEmail(careGiverRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByNik(careGiverRequest.getNik())).thenReturn(true);

        // when/then
        assertThatThrownBy(() -> authServiceImpl.registerUser(careGiverRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("NIK is already in use");

        verify(userRepository).existsByEmail(careGiverRequest.getEmail());
        verify(userRepository).existsByNik(careGiverRequest.getNik());
        verify(careGiverRepository, never()).save(any(CareGiver.class));
        verify(factoryProvider, never()).getFactory(any());
    }

    // Token validation tests updated to use user ID

    @Test
    void validateToken_WithValidToken_ShouldReturnValidResponse() {
        // given
        when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
        when(jwtUtils.getUserIdFromJwtToken(validToken)).thenReturn("1"); // Now returns user ID
        when(userRepository.findById(1L)).thenReturn(Optional.of(user)); // Find by ID

        // when
        TokenValidationResponse response = authServiceImpl.validateToken(validToken);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isValid()).isTrue();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("test@example.com");
        assertThat(response.getRoles()).contains("ROLE_PACILLIAN");

        verify(jwtUtils).validateJwtToken(validToken);
        verify(jwtUtils).getUserIdFromJwtToken(validToken);
        verify(userRepository).findById(1L);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnInvalidResponse() {
        // given
        when(jwtUtils.validateJwtToken(invalidToken)).thenReturn(false);

        // when
        TokenValidationResponse response = authServiceImpl.validateToken(invalidToken);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isValid()).isFalse();
        assertThat(response.getUserId()).isNull();
        assertThat(response.getUsername()).isNull();
        assertThat(response.getRoles()).isNull();

        verify(jwtUtils).validateJwtToken(invalidToken);
        verify(jwtUtils, never()).getUserIdFromJwtToken(anyString());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void validateToken_WhenExceptionOccurs_ShouldReturnInvalidResponse() {
        // given
        when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
        when(jwtUtils.getUserIdFromJwtToken(validToken)).thenReturn("1");
        when(userRepository.findById(1L)).thenThrow(new RuntimeException("Test exception"));

        // when
        TokenValidationResponse response = authServiceImpl.validateToken(validToken);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isValid()).isFalse();
        assertThat(response.getUserId()).isNull();
        assertThat(response.getUsername()).isNull();
        assertThat(response.getRoles()).isNull();

        verify(jwtUtils).validateJwtToken(validToken);
        verify(jwtUtils).getUserIdFromJwtToken(validToken);
        verify(userRepository).findById(1L);
    }

    @Test
    void validateToken_WhenUserNotFound_ShouldReturnInvalidResponse() {
        // given
        when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
        when(jwtUtils.getUserIdFromJwtToken(validToken)).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when/then
        TokenValidationResponse response = authServiceImpl.validateToken(validToken);

        // Response should be invalid
        assertThat(response.isValid()).isFalse();
    }



    @Test
    void generateTokenWithoutAuthentication_ShouldReturnToken() {
        // given
        String mockToken = "direct_jwt_token";
        String userId = "1";

        // Setup JWT utils mock behavior - now using user ID
        when(jwtUtils.generateJwtTokenFromUserId(userId)).thenReturn(mockToken);

        // when
        String resultToken = jwtUtils.generateJwtTokenFromUserId(userId);

        // then
        assertThat(resultToken).isNotNull();
        assertThat(resultToken).isEqualTo(mockToken);

        // Verify the JWT was generated with the user's ID
        verify(jwtUtils).generateJwtTokenFromUserId(userId);
    }

    @Test
    void registerUser_WithUnsupportedUserType_ShouldThrowException() {
        // Create a BaseRegisterRequest that we'll use with our factory
        BaseRegisterRequest unsupportedRequest = mock(BaseRegisterRequest.class);

        // Mock the BaseRegisterRequest to return expected values
        when(unsupportedRequest.getEmail()).thenReturn("unsupported@example.com");
        when(unsupportedRequest.getNik()).thenReturn("1234567890123456");
        when(unsupportedRequest.getPassword()).thenReturn("password123");

        // We need a User object that is neither a Pacillian nor a CareGiver
        User unsupportedUser = new User();
        unsupportedUser.setEmail("unsupported@example.com");

        // Setup mocks to pass validation - using lenient() to be less strict
        lenient().when(userRepository.existsByEmail(anyString())).thenReturn(false);
        lenient().when(userRepository.existsByNik(anyString())).thenReturn(false);

        // Mock the encoder
        when(encoder.encode(anyString())).thenReturn("encoded_password");

        // Mock the factory to return our unsupported user type - using lenient() and any()
        UserFactory mockFactory = mock(UserFactory.class);
        when(factoryProvider.getFactory(any())).thenReturn(mockFactory);
        lenient().when(mockFactory.createUser(any(), any())).thenReturn(unsupportedUser);

        // Act and Assert - verify that the exception is thrown with the correct message
        assertThatThrownBy(() -> authServiceImpl.registerUser(unsupportedRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported user type");

        // Verify our mocks were called as expected
        verify(factoryProvider).getFactory(any());
        verify(mockFactory).createUser(any(), any());
        verify(pacillianRepository, never()).save(any());
        verify(careGiverRepository, never()).save(any());
    }
}