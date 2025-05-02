package id.ac.ui.cs.advprog.authprofile.security.strategy;

import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
import id.ac.ui.cs.advprog.authprofile.model.Role;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PacillianAuthorizationStrategyTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PacillianAuthorizationStrategy strategy;

    private Pacillian pacillian;
    private User nonPacillian;
    private CareGiver careGiver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up a Pacillian user
        pacillian = new Pacillian();
        pacillian.setId(1L);
        pacillian.setEmail("patient@example.com");

        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setName(Role.ERole.ROLE_PACILLIAN);
        roles.add(role);
        pacillian.setRoles(roles);

        // Set up a non-Pacillian user
        nonPacillian = new User();
        nonPacillian.setId(2L);
        nonPacillian.setEmail("user@example.com");

        // Set up a CareGiver user
        careGiver = new CareGiver();
        careGiver.setId(3L);
        careGiver.setEmail("caregiver@example.com");

        Set<Role> careGiverRoles = new HashSet<>();
        Role careGiverRole = new Role();
        careGiverRole.setName(Role.ERole.ROLE_CAREGIVER);
        careGiverRoles.add(careGiverRole);
        careGiver.setRoles(careGiverRoles);

        // Mock the repository behavior
        when(userRepository.findById(1L)).thenReturn(Optional.of(pacillian));
        when(userRepository.findById(2L)).thenReturn(Optional.of(nonPacillian));
        when(userRepository.findById(3L)).thenReturn(Optional.of(careGiver));
    }

    @Test
    void whenUserIsNotPacillian_thenReturnFalse() {
        boolean result = strategy.isAuthorized(nonPacillian, 1L, "VIEW_PROFILE");
        assertFalse(result, "Non-Pacillian users should not be authorized");
    }

    @Test
    void whenPacillianViewsOwnProfile_thenReturnTrue() {
        boolean result = strategy.isAuthorized(pacillian, 1L, "VIEW_PROFILE");
        assertTrue(result, "Pacillian should be able to view their own profile");
    }

    @Test
    void whenPacillianViewsViewsOwnProfileWithNullResourceId_thenReturnTrue() {
        boolean result = strategy.isAuthorized(pacillian, null, "VIEW_OWN_PROFILE");
        assertTrue(result, "Pacillian should be able to view their own profile when resourceId is null");
    }

    @Test
    void whenPacillianViewsOtherProfileThatIsNotCareGiver_thenReturnFalse() {
        boolean result = strategy.isAuthorized(pacillian, 2L, "VIEW_PROFILE");
        assertFalse(result, "Pacillian should not be able to view non-caregiver profiles");
    }

    @Test
    void whenPacillianViewsCareGiverProfile_thenReturnTrue() {
        boolean result = strategy.isAuthorized(pacillian, 3L, "VIEW_PROFILE");
        assertTrue(result, "Pacillian should be able to view caregiver profiles");
    }

    @Test
    void whenPacillianViewsCareGiver_thenReturnTrue() {
        boolean result = strategy.isAuthorized(pacillian, 3L, "VIEW_CAREGIVER");
        assertTrue(result, "Pacillian should be able to view caregiver profiles");
    }

    @Test
    void whenPacillianUpdatesOwnProfile_thenReturnTrue() {
        boolean result = strategy.isAuthorized(pacillian, 1L, "UPDATE_PROFILE");
        assertTrue(result, "Pacillian should be able to update their own profile");
    }

    @Test
    void whenPacillianUpdatesOwnProfileWithNullResourceId_thenReturnTrue() {
        boolean result = strategy.isAuthorized(pacillian, null, "UPDATE_PROFILE");
        assertTrue(result, "Pacillian should be able to update their own profile when resourceId is null");
    }

    @Test
    void whenPacillianUpdatesOtherProfile_thenReturnFalse() {
        boolean result = strategy.isAuthorized(pacillian, 2L, "UPDATE_PROFILE");
        assertFalse(result, "Pacillian should not be able to update other profiles");
    }

    @Test
    void whenPacillianDeletesOwnProfile_thenReturnTrue() {
        boolean result = strategy.isAuthorized(pacillian, 1L, "DELETE_PROFILE");
        assertTrue(result, "Pacillian should be able to delete their own profile");
    }

    @Test
    void whenPacillianDeletesOwnProfileWithNullResourceId_thenReturnTrue() {
        boolean result = strategy.isAuthorized(pacillian, null, "DELETE_PROFILE");
        assertTrue(result, "Pacillian should be able to delete their own profile when resourceId is null");
    }

    @Test
    void whenPacillianDeletesOtherProfile_thenReturnFalse() {
        boolean result = strategy.isAuthorized(pacillian, 2L, "DELETE_PROFILE");
        assertFalse(result, "Pacillian should not be able to delete other profiles");
    }

    @Test
    void whenPacillianPerformsUnknownAction_thenReturnFalse() {
        boolean result = strategy.isAuthorized(pacillian, 1L, "UNKNOWN_ACTION");
        assertFalse(result, "Unknown actions should not be authorized");
    }
}