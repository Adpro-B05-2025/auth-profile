package id.ac.ui.cs.advprog.authprofile.security.strategy;

import id.ac.ui.cs.advprog.authprofile.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class DefaultAuthorizationStrategyTest {

    @InjectMocks
    private DefaultAuthorizationStrategy strategy;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up a generic user
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
    }

    @Test
    void whenUserViewsOwnProfile_thenReturnTrue() {
        boolean result = strategy.isAuthorized(user, null, "VIEW_OWN_PROFILE");
        assertTrue(result, "Any user should be able to view their own profile");
    }

    @Test
    void whenUserUpdatesOwnProfileWithMatchingId_thenReturnTrue() {
        boolean result = strategy.isAuthorized(user, 1L, "UPDATE_PROFILE");
        assertTrue(result, "User should be able to update their own profile");
    }

    @Test
    void whenUserUpdatesOwnProfileWithNullResourceId_thenReturnTrue() {
        boolean result = strategy.isAuthorized(user, null, "UPDATE_PROFILE");
        assertTrue(result, "User should be able to update their own profile when resourceId is null");
    }

    @Test
    void whenUserUpdatesOtherProfile_thenReturnFalse() {
        boolean result = strategy.isAuthorized(user, 2L, "UPDATE_PROFILE");
        assertFalse(result, "User should not be able to update other profiles");
    }

    @Test
    void whenUserDeletesOwnProfileWithMatchingId_thenReturnTrue() {
        boolean result = strategy.isAuthorized(user, 1L, "DELETE_PROFILE");
        assertTrue(result, "User should be able to delete their own profile");
    }

    @Test
    void whenUserDeletesOwnProfileWithNullResourceId_thenReturnTrue() {
        boolean result = strategy.isAuthorized(user, null, "DELETE_PROFILE");
        assertTrue(result, "User should be able to delete their own profile when resourceId is null");
    }

    @Test
    void whenUserDeletesOtherProfile_thenReturnFalse() {
        boolean result = strategy.isAuthorized(user, 2L, "DELETE_PROFILE");
        assertFalse(result, "User should not be able to delete other profiles");
    }

    @Test
    void whenUserPerformsUnknownAction_thenReturnFalse() {
        boolean result = strategy.isAuthorized(user, 1L, "UNKNOWN_ACTION");
        assertFalse(result, "Unknown actions should not be authorized");
    }

    @Test
    void supportsUserTypeAlwaysReturnsTrue() {
        assertTrue(strategy.supportsUserType(user), "Default strategy should support all user types");
        assertTrue(strategy.supportsUserType(null), "Default strategy should even support null users");
    }
}