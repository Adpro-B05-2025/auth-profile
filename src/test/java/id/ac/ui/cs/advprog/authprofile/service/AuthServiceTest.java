package id.ac.ui.cs.advprog.authprofile.service;

import id.ac.ui.cs.advprog.authprofile.dto.request.LoginRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterCareGiverRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterPacillianRequest;
import id.ac.ui.cs.advprog.authprofile.dto.response.JwtResponse;
import id.ac.ui.cs.advprog.authprofile.exception.EmailAlreadyExistsException;
import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
import id.ac.ui.cs.advprog.authprofile.model.Role;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.model.WorkingSchedule;
import id.ac.ui.cs.advprog.authprofile.repository.CareGiverRepository;
import id.ac.ui.cs.advprog.authprofile.repository.PacillianRepository;
import id.ac.ui.cs.advprog.authprofile.repository.RoleRepository;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import id.ac.ui.cs.advprog.authprofile.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

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

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest;
    private RegisterPacillianRequest pacillianRequest;
    private RegisterCareGiverRequest careGiverRequest;
    private User user;
    private Role pacillianRole;
    private Role careGiverRole;

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

        Set<Role> roles = new HashSet<>();
        roles.add(pacillianRole);
        user.setRoles(roles);
    }

    @Test
    void authenticateUser_ShouldReturnJwtResponse() {
        // Setup authentication and userDetails mocks
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        List<SimpleGrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PACILLIAN"));
        doReturn(authorities).when(userDetails).getAuthorities();
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("test_jwt_token");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        JwtResponse response = authService.authenticateUser(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("test_jwt_token");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRoles()).contains("ROLE_PACILLIAN");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateJwtToken(authentication);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void authenticateUser_WhenUserNotFound_ShouldThrowException() {
        // Setup
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("test_jwt_token");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.authenticateUser(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void registerPacillian_ShouldReturnSuccessMessage() {
        // given
        when(userRepository.existsByEmail(pacillianRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByNik(pacillianRequest.getNik())).thenReturn(false);
        when(encoder.encode(pacillianRequest.getPassword())).thenReturn("encoded_password");
        when(roleRepository.findByName(Role.ERole.ROLE_PACILLIAN)).thenReturn(Optional.of(pacillianRole));

        // when
        String result = authService.registerPacillian(pacillianRequest);

        // then
        assertThat(result).isEqualTo("Pacillian registered successfully!");

        verify(userRepository).existsByEmail(pacillianRequest.getEmail());
        verify(userRepository).existsByNik(pacillianRequest.getNik());
        verify(encoder).encode(pacillianRequest.getPassword());
        verify(roleRepository).findByName(Role.ERole.ROLE_PACILLIAN);
        verify(pacillianRepository).save(any(Pacillian.class));
    }

    @Test
    void registerPacillian_WithExistingEmail_ShouldThrowException() {
        // given
        when(userRepository.existsByEmail(pacillianRequest.getEmail())).thenReturn(true);

        // when/then
        assertThatThrownBy(() -> authService.registerPacillian(pacillianRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email is already in use");

        verify(userRepository).existsByEmail(pacillianRequest.getEmail());
        verify(pacillianRepository, never()).save(any(Pacillian.class));
    }

    @Test
    void registerPacillian_WithExistingNIK_ShouldThrowException() {
        // given
        when(userRepository.existsByEmail(pacillianRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByNik(pacillianRequest.getNik())).thenReturn(true);

        // when/then
        assertThatThrownBy(() -> authService.registerPacillian(pacillianRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("NIK is already in use");

        verify(userRepository).existsByEmail(pacillianRequest.getEmail());
        verify(userRepository).existsByNik(pacillianRequest.getNik());
        verify(pacillianRepository, never()).save(any(Pacillian.class));
    }

    @Test
    void registerPacillian_WhenRoleNotFound_ShouldThrowException() {
        // given
        when(userRepository.existsByEmail(pacillianRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByNik(pacillianRequest.getNik())).thenReturn(false);
        when(encoder.encode(pacillianRequest.getPassword())).thenReturn("encoded_password");
        when(roleRepository.findByName(Role.ERole.ROLE_PACILLIAN)).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> authService.registerPacillian(pacillianRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Role is not found");

        verify(roleRepository).findByName(Role.ERole.ROLE_PACILLIAN);
        verify(pacillianRepository, never()).save(any(Pacillian.class));
    }

    @Test
    void registerCareGiver_ShouldReturnSuccessMessage() {
        // given
        when(userRepository.existsByEmail(careGiverRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByNik(careGiverRequest.getNik())).thenReturn(false);
        when(encoder.encode(careGiverRequest.getPassword())).thenReturn("encoded_password");
        when(roleRepository.findByName(Role.ERole.ROLE_CAREGIVER)).thenReturn(Optional.of(careGiverRole));

        // when
        String result = authService.registerCareGiver(careGiverRequest);

        // then
        assertThat(result).isEqualTo("CareGiver registered successfully!");

        verify(userRepository).existsByEmail(careGiverRequest.getEmail());
        verify(userRepository).existsByNik(careGiverRequest.getNik());
        verify(encoder).encode(careGiverRequest.getPassword());
        verify(roleRepository).findByName(Role.ERole.ROLE_CAREGIVER);

        // Verify that a working schedule was added to the CareGiver
        ArgumentCaptor<CareGiver> careGiverCaptor = ArgumentCaptor.forClass(CareGiver.class);
        verify(careGiverRepository).save(careGiverCaptor.capture());
        CareGiver savedCareGiver = careGiverCaptor.getValue();
        assertThat(savedCareGiver.getWorkingSchedules()).isNotEmpty();
        assertThat(savedCareGiver.getWorkingSchedules().size()).isEqualTo(1);
    }

    @Test
    void registerCareGiver_WithoutWorkingSchedules_ShouldRegisterSuccessfully() {
        // given
        careGiverRequest.setWorkingSchedules(null);
        when(userRepository.existsByEmail(careGiverRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByNik(careGiverRequest.getNik())).thenReturn(false);
        when(encoder.encode(careGiverRequest.getPassword())).thenReturn("encoded_password");
        when(roleRepository.findByName(Role.ERole.ROLE_CAREGIVER)).thenReturn(Optional.of(careGiverRole));

        // when
        String result = authService.registerCareGiver(careGiverRequest);

        // then
        assertThat(result).isEqualTo("CareGiver registered successfully!");

        // Verify that no working schedules were added
        ArgumentCaptor<CareGiver> careGiverCaptor = ArgumentCaptor.forClass(CareGiver.class);
        verify(careGiverRepository).save(careGiverCaptor.capture());
        CareGiver savedCareGiver = careGiverCaptor.getValue();
        assertThat(savedCareGiver.getWorkingSchedules()).isEmpty();
    }

    @Test
    void registerCareGiver_WithExistingEmail_ShouldThrowException() {
        // given
        when(userRepository.existsByEmail(careGiverRequest.getEmail())).thenReturn(true);

        // when/then
        assertThatThrownBy(() -> authService.registerCareGiver(careGiverRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email is already in use");

        verify(userRepository).existsByEmail(careGiverRequest.getEmail());
        verify(careGiverRepository, never()).save(any(CareGiver.class));
    }

    @Test
    void registerCareGiver_WithExistingNIK_ShouldThrowException() {
        // given
        when(userRepository.existsByEmail(careGiverRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByNik(careGiverRequest.getNik())).thenReturn(true);

        // when/then
        assertThatThrownBy(() -> authService.registerCareGiver(careGiverRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("NIK is already in use");

        verify(userRepository).existsByEmail(careGiverRequest.getEmail());
        verify(userRepository).existsByNik(careGiverRequest.getNik());
        verify(careGiverRepository, never()).save(any(CareGiver.class));
    }

    @Test
    void registerCareGiver_WhenRoleNotFound_ShouldThrowException() {
        // given
        when(userRepository.existsByEmail(careGiverRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByNik(careGiverRequest.getNik())).thenReturn(false);
        when(encoder.encode(careGiverRequest.getPassword())).thenReturn("encoded_password");
        when(roleRepository.findByName(Role.ERole.ROLE_CAREGIVER)).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> authService.registerCareGiver(careGiverRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Role is not found");

        verify(roleRepository).findByName(Role.ERole.ROLE_CAREGIVER);
        verify(careGiverRepository, never()).save(any(CareGiver.class));
    }
}