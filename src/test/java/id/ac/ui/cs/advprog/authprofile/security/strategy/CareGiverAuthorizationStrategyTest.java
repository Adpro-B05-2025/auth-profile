package id.ac.ui.cs.advprog.authprofile.security.strategy;

import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.Role;
import id.ac.ui.cs.advprog.authprofile.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CareGiverAuthorizationStrategyTest {

    @InjectMocks
    private CareGiverAuthorizationStrategy strategy;

    private CareGiver careGiver;
    private User nonCareGiver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up a CareGiver user
        careGiver = new CareGiver();
        careGiver.setId(1L);
        careGiver.setEmail("doctor@example.com");

        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setName(Role.ERole.ROLE_CAREGIVER);
        roles.add(role);
        careGiver.setRoles(roles);

        // Set up a non-CareGiver user
        nonCareGiver = new User();
        nonCareGiver.setId(2L);
        nonCareGiver.setEmail("user@example.com");
    }

    @Test
    void whenUserIsNotCareGiver_thenReturnFalse() {
        boolean result = strategy.isAuthorized(nonCareGiver, 1L, "VIEW_PROFILE");
        assertFalse(result, "Non-CareGiver users should not be authorized");
    }

    @Test
    void whenCareGiverViewsOwnProfile_thenReturnTrue() {
        boolean result = strategy.isAuthorized(careGiver, null, "VIEW_OWN_PROFILE");
        assertTrue(result, "CareGiver should be able to view their own profile");
    }

    @Test
    void whenCareGiverViewsAnyProfile_thenReturnTrue() {
        boolean result = strategy.isAuthorized(careGiver, 2L, "VIEW_PROFILE");
        assertTrue(result, "CareGiver should be able to view any profile");
    }

    @Test
    void whenCareGiverViewsAnyProfileWithNullResourceId_thenReturnTrue() {
        boolean result = strategy.isAuthorized(careGiver, null, "VIEW_PROFILE");
        assertTrue(result, "CareGiver should be able to view any profile even with null resourceId");
    }

    @Test
    void whenCareGiverUpdatesOwnProfile_thenReturnTrue() {
        boolean result = strategy.isAuthorized(careGiver, 1L, "UPDATE_PROFILE");
        assertTrue(result, "CareGiver should be able to update their own profile");
    }

    @Test
    void whenCareGiverUpdatesOwnProfileWithNullResourceId_thenReturnTrue() {
        boolean result = strategy.isAuthorized(careGiver, null, "UPDATE_PROFILE");
        assertTrue(result, "CareGiver should be able to update their own profile when resourceId is null");
    }

    @Test
    void whenCareGiverUpdatesOtherProfile_thenReturnFalse() {
        boolean result = strategy.isAuthorized(careGiver, 2L, "UPDATE_PROFILE");
        assertFalse(result, "CareGiver should not be able to update other profiles");
    }

    @Test
    void whenCareGiverDeletesOwnProfile_thenReturnTrue() {
        boolean result = strategy.isAuthorized(careGiver, 1L, "DELETE_PROFILE");
        assertTrue(result, "CareGiver should be able to delete their own profile");
    }

    @Test
    void whenCareGiverDeletesOwnProfileWithNullResourceId_thenReturnTrue() {
        boolean result = strategy.isAuthorized(careGiver, null, "DELETE_PROFILE");
        assertTrue(result, "CareGiver should be able to delete their own profile when resourceId is null");
    }

    @Test
    void whenCareGiverDeletesOtherProfile_thenReturnFalse() {
        boolean result = strategy.isAuthorized(careGiver, 2L, "DELETE_PROFILE");
        assertFalse(result, "CareGiver should not be able to delete other profiles");
    }

    @Test
    void whenCareGiverViewsPacillianMedicalHistory_thenReturnTrue() {
        boolean result = strategy.isAuthorized(careGiver, 2L, "VIEW_PACILLIAN_MEDICAL_HISTORY");
        assertTrue(result, "CareGiver should be able to view any pacillian's medical history");
    }

    @Test
    void whenCareGiverViewsPacillianMedicalHistoryWithNullResourceId_thenReturnTrue() {
        boolean result = strategy.isAuthorized(careGiver, null, "VIEW_PACILLIAN_MEDICAL_HISTORY");
        assertTrue(result, "CareGiver should be able to view any pacillian's medical history even with null resourceId");
    }

    @Test
    void whenCareGiverPerformsUnknownAction_thenReturnFalse() {
        boolean result = strategy.isAuthorized(careGiver, 1L, "UNKNOWN_ACTION");
        assertFalse(result, "Unknown actions should not be authorized");
    }

    @Test
    void whenCareGiverPerformsUnknownActionWithNullResourceId_thenReturnFalse() {
        boolean result = strategy.isAuthorized(careGiver, null, "UNKNOWN_ACTION");
        assertFalse(result, "Unknown actions should not be authorized even with null resourceId");
    }

    @Test
    void supportsUserTypeReturnsTrueForCareGiver() {
        assertTrue(strategy.supportsUserType(careGiver), "Strategy should support CareGiver type");
    }

    @Test
    void supportsUserTypeReturnsFalseForNonCareGiver() {
        assertFalse(strategy.supportsUserType(nonCareGiver), "Strategy should not support non-CareGiver type");
    }

    @Test
    void supportsUserTypeReturnsFalseForNull() {
        assertFalse(strategy.supportsUserType(null), "Strategy should not support null user");
    }
}