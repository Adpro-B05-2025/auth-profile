package id.ac.ui.cs.advprog.authprofile.security.strategy;

import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
import id.ac.ui.cs.advprog.authprofile.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AuthorizationContextTest {

    @Mock
    private PacillianAuthorizationStrategy pacillianStrategy;

    @Mock
    private CareGiverAuthorizationStrategy careGiverStrategy;

    @Mock
    private DefaultAuthorizationStrategy defaultStrategy;

    private AuthorizationContext authorizationContext;
    private AuthorizationContext emptyAuthorizationContext;

    private Pacillian pacillian;
    private CareGiver careGiver;
    private User generalUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize test users first
        pacillian = new Pacillian();
        pacillian.setId(1L);

        careGiver = new CareGiver();
        careGiver.setId(2L);

        generalUser = new User();
        generalUser.setId(3L);

        // Configure the strategies with specific instances
        when(pacillianStrategy.supportsUserType(pacillian)).thenReturn(true);
        when(pacillianStrategy.supportsUserType(careGiver)).thenReturn(false);
        when(pacillianStrategy.supportsUserType(generalUser)).thenReturn(false);

        when(careGiverStrategy.supportsUserType(pacillian)).thenReturn(false);
        when(careGiverStrategy.supportsUserType(careGiver)).thenReturn(true);
        when(careGiverStrategy.supportsUserType(generalUser)).thenReturn(false);

        // DefaultStrategy supports all user types
        when(defaultStrategy.supportsUserType(any())).thenReturn(true);

        // Create context with strategies in priority order
        authorizationContext = new AuthorizationContext(
                Arrays.asList(pacillianStrategy, careGiverStrategy, defaultStrategy));

        emptyAuthorizationContext = new AuthorizationContext(Collections.emptyList());
    }

    @Test
    void whenUserIsPacillian_thenUsePacillianStrategy() {
        when(pacillianStrategy.isAuthorized(any(Pacillian.class), anyLong(), anyString())).thenReturn(true);

        boolean result = authorizationContext.isAuthorized(pacillian, 1L, "VIEW_PROFILE");

        assertTrue(result, "Should delegate to Pacillian strategy and return its result");
    }

    @Test
    void whenUserIsCareGiver_thenUseCareGiverStrategy() {
        when(careGiverStrategy.isAuthorized(any(CareGiver.class), anyLong(), anyString())).thenReturn(true);

        boolean result = authorizationContext.isAuthorized(careGiver, 1L, "VIEW_PROFILE");

        assertTrue(result, "Should delegate to CareGiver strategy and return its result");
    }

    @Test
    void whenUserIsNeitherPacillianNorCareGiver_thenUseDefaultStrategy() {
        when(defaultStrategy.isAuthorized(any(User.class), anyLong(), anyString())).thenReturn(true);

        boolean result = authorizationContext.isAuthorized(generalUser, 1L, "VIEW_PROFILE");

        assertTrue(result, "Should delegate to default strategy and return its result");
    }

    @Test
    void whenPacillianStrategyReturnsFalse_thenContextReturnsFalse() {
        when(pacillianStrategy.isAuthorized(any(Pacillian.class), anyLong(), anyString())).thenReturn(false);

        boolean result = authorizationContext.isAuthorized(pacillian, 1L, "VIEW_PROFILE");

        assertFalse(result, "Should return false when Pacillian strategy returns false");
    }

    @Test
    void whenCareGiverStrategyReturnsFalse_thenContextReturnsFalse() {
        when(careGiverStrategy.isAuthorized(any(CareGiver.class), anyLong(), anyString())).thenReturn(false);

        boolean result = authorizationContext.isAuthorized(careGiver, 1L, "VIEW_PROFILE");

        assertFalse(result, "Should return false when CareGiver strategy returns false");
    }

    @Test
    void whenDefaultStrategyReturnsFalse_thenContextReturnsFalse() {
        when(defaultStrategy.isAuthorized(any(User.class), anyLong(), anyString())).thenReturn(false);

        boolean result = authorizationContext.isAuthorized(generalUser, 1L, "VIEW_PROFILE");

        assertFalse(result, "Should return false when default strategy returns false");
    }

    @Test
    void whenNoStrategySupportsUserType_thenReturnFalse() {
        // Test with empty context that has no strategies
        boolean result = emptyAuthorizationContext.isAuthorized(generalUser, 1L, "VIEW_PROFILE");
        assertFalse(result, "Should return false when no strategy supports the user type");
    }

    @Test
    void whenNullUserProvided_thenReturnFalse() {
        // Setup all strategies to reject null user
        when(pacillianStrategy.supportsUserType(null)).thenReturn(false);
        when(careGiverStrategy.supportsUserType(null)).thenReturn(false);
        when(defaultStrategy.supportsUserType(null)).thenReturn(false);

        // Create context without default strategy
        AuthorizationContext contextWithoutDefaultSupport = new AuthorizationContext(
                Arrays.asList(pacillianStrategy, careGiverStrategy));

        boolean result = contextWithoutDefaultSupport.isAuthorized(null, 1L, "VIEW_PROFILE");
        assertFalse(result, "Should return false when user is null and no strategy supports it");
    }
}