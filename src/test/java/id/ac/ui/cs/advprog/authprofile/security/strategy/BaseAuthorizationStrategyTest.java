package id.ac.ui.cs.advprog.authprofile.security.strategy;

import id.ac.ui.cs.advprog.authprofile.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class BaseAuthorizationStrategyTest {

    // Concrete implementation for testing the abstract class
    private static class TestAuthorizationStrategy extends BaseAuthorizationStrategy {
        @Override
        public boolean isAuthorized(User user, Long resourceId, String action) {
            return handleModificationActions(user, resourceId, action);
        }

        @Override
        public boolean supportsUserType(User user) {
            return true;
        }
    }

    private TestAuthorizationStrategy strategy;

    @Mock
    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        strategy = new TestAuthorizationStrategy();
        when(mockUser.getId()).thenReturn(1L);
    }

    @Nested
    @DisplayName("handleModificationActions Tests")
    class HandleModificationActionsTests {

        @Test
        @DisplayName("Should return false for non-modification actions - VIEW_USERNAME")
        void shouldReturnFalseForViewUsernameAction() {
            // Arrange
            String action = AuthorizationStrategy.VIEW_USERNAME;
            Long resourceId = 1L;

            // Act
            boolean result = strategy.handleModificationActions(mockUser, resourceId, action);

            // Assert
            assertFalse(result, "handleModificationActions should return false for VIEW_USERNAME action");
        }

        @Test
        @DisplayName("Should return false for non-modification actions - VIEW_OWN_PROFILE")
        void shouldReturnFalseForViewOwnProfileAction() {
            // Arrange
            String action = AuthorizationStrategy.VIEW_OWN_PROFILE;
            Long resourceId = 1L;

            // Act
            boolean result = strategy.handleModificationActions(mockUser, resourceId, action);

            // Assert
            assertFalse(result, "handleModificationActions should return false for VIEW_OWN_PROFILE action");
        }

        @Test
        @DisplayName("Should return false for non-modification actions - VIEW_PROFILE")
        void shouldReturnFalseForViewProfileAction() {
            // Arrange
            String action = AuthorizationStrategy.VIEW_PROFILE;
            Long resourceId = 1L;

            // Act
            boolean result = strategy.handleModificationActions(mockUser, resourceId, action);

            // Assert
            assertFalse(result, "handleModificationActions should return false for VIEW_PROFILE action");
        }

        @Test
        @DisplayName("Should return false for non-modification actions - VIEW_CAREGIVER")
        void shouldReturnFalseForViewCaregiverAction() {
            // Arrange
            String action = AuthorizationStrategy.VIEW_CAREGIVER;
            Long resourceId = 1L;

            // Act
            boolean result = strategy.handleModificationActions(mockUser, resourceId, action);

            // Assert
            assertFalse(result, "handleModificationActions should return false for VIEW_CAREGIVER action");
        }

        @Test
        @DisplayName("Should return false for non-modification actions - VIEW_PACILLIAN_MEDICAL_HISTORY")
        void shouldReturnFalseForViewMedicalHistoryAction() {
            // Arrange
            String action = AuthorizationStrategy.VIEW_PACILLIAN_MEDICAL_HISTORY;
            Long resourceId = 1L;

            // Act
            boolean result = strategy.handleModificationActions(mockUser, resourceId, action);

            // Assert
            assertFalse(result, "handleModificationActions should return false for VIEW_PACILLIAN_MEDICAL_HISTORY action");
        }

        @Test
        @DisplayName("Should return false for unknown actions")
        void shouldReturnFalseForUnknownActions() {
            // Arrange
            String action = "UNKNOWN_ACTION";
            Long resourceId = 1L;

            // Act
            boolean result = strategy.handleModificationActions(mockUser, resourceId, action);

            // Assert
            assertFalse(result, "handleModificationActions should return false for unknown actions");
        }



        @Test
        @DisplayName("Should return false for empty string actions")
        void shouldReturnFalseForEmptyStringActions() {
            // Arrange
            String action = "";
            Long resourceId = 1L;

            // Act
            boolean result = strategy.handleModificationActions(mockUser, resourceId, action);

            // Assert
            assertFalse(result, "handleModificationActions should return false for empty string actions");
        }

        @Test
        @DisplayName("Should return true for UPDATE_PROFILE when user owns resource")
        void shouldReturnTrueForUpdateProfileWhenUserOwnsResource() {
            // Arrange
            String action = AuthorizationStrategy.UPDATE_PROFILE;
            Long resourceId = 1L; // Same as user ID

            // Act
            boolean result = strategy.handleModificationActions(mockUser, resourceId, action);

            // Assert
            assertTrue(result, "handleModificationActions should return true for UPDATE_PROFILE when user owns resource");
        }

        @Test
        @DisplayName("Should return false for UPDATE_PROFILE when user doesn't own resource")
        void shouldReturnFalseForUpdateProfileWhenUserDoesntOwnResource() {
            // Arrange
            String action = AuthorizationStrategy.UPDATE_PROFILE;
            Long resourceId = 2L; // Different from user ID

            // Act
            boolean result = strategy.handleModificationActions(mockUser, resourceId, action);

            // Assert
            assertFalse(result, "handleModificationActions should return false for UPDATE_PROFILE when user doesn't own resource");
        }

        @Test
        @DisplayName("Should return true for DELETE_PROFILE when user owns resource")
        void shouldReturnTrueForDeleteProfileWhenUserOwnsResource() {
            // Arrange
            String action = AuthorizationStrategy.DELETE_PROFILE;
            Long resourceId = 1L; // Same as user ID

            // Act
            boolean result = strategy.handleModificationActions(mockUser, resourceId, action);

            // Assert
            assertTrue(result, "handleModificationActions should return true for DELETE_PROFILE when user owns resource");
        }

        @Test
        @DisplayName("Should return false for DELETE_PROFILE when user doesn't own resource")
        void shouldReturnFalseForDeleteProfileWhenUserDoesntOwnResource() {
            // Arrange
            String action = AuthorizationStrategy.DELETE_PROFILE;
            Long resourceId = 2L; // Different from user ID

            // Act
            boolean result = strategy.handleModificationActions(mockUser, resourceId, action);

            // Assert
            assertFalse(result, "handleModificationActions should return false for DELETE_PROFILE when user doesn't own resource");
        }

        @Test
        @DisplayName("Should return true for modification actions when resourceId is null")
        void shouldReturnTrueForModificationActionsWhenResourceIdIsNull() {
            // Arrange
            String action = AuthorizationStrategy.UPDATE_PROFILE;
            Long resourceId = null;

            // Act
            boolean result = strategy.handleModificationActions(mockUser, resourceId, action);

            // Assert
            assertTrue(result, "handleModificationActions should return true for modification actions when resourceId is null");
        }
    }

    @Nested
    @DisplayName("Helper Methods Tests")
    class HelperMethodsTests {

        @Test
        @DisplayName("isModificationAction should return true for UPDATE_PROFILE")
        void isModificationActionShouldReturnTrueForUpdateProfile() {
            assertTrue(strategy.isModificationAction(AuthorizationStrategy.UPDATE_PROFILE));
        }

        @Test
        @DisplayName("isModificationAction should return true for DELETE_PROFILE")
        void isModificationActionShouldReturnTrueForDeleteProfile() {
            assertTrue(strategy.isModificationAction(AuthorizationStrategy.DELETE_PROFILE));
        }

        @Test
        @DisplayName("isModificationAction should return false for viewing actions")
        void isModificationActionShouldReturnFalseForViewingActions() {
            assertFalse(strategy.isModificationAction(AuthorizationStrategy.VIEW_PROFILE));
            assertFalse(strategy.isModificationAction(AuthorizationStrategy.VIEW_OWN_PROFILE));
            assertFalse(strategy.isModificationAction(AuthorizationStrategy.VIEW_USERNAME));
            assertFalse(strategy.isModificationAction(AuthorizationStrategy.VIEW_CAREGIVER));
        }

        @Test
        @DisplayName("canModifyOwnResource should return true when resourceId matches user ID")
        void canModifyOwnResourceShouldReturnTrueWhenResourceIdMatchesUserId() {
            assertTrue(strategy.canModifyOwnResource(mockUser, 1L));
        }

        @Test
        @DisplayName("canModifyOwnResource should return false when resourceId doesn't match user ID")
        void canModifyOwnResourceShouldReturnFalseWhenResourceIdDoesntMatchUserId() {
            assertFalse(strategy.canModifyOwnResource(mockUser, 2L));
        }

        @Test
        @DisplayName("canModifyOwnResource should return true when resourceId is null")
        void canModifyOwnResourceShouldReturnTrueWhenResourceIdIsNull() {
            assertTrue(strategy.canModifyOwnResource(mockUser, null));
        }
    }
}