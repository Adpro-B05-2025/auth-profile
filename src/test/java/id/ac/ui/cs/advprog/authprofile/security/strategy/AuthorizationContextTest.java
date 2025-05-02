package id.ac.ui.cs.advprog.authprofile.security.strategy;

import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
import id.ac.ui.cs.advprog.authprofile.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

    private AuthorizationContext authorizationContext;

    private Pacillian pacillian;
    private CareGiver careGiver;
    private User generalUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        authorizationContext = new AuthorizationContext(pacillianStrategy, careGiverStrategy);

        pacillian = new Pacillian();
        pacillian.setId(1L);

        careGiver = new CareGiver();
        careGiver.setId(2L);

        generalUser = new User();
        generalUser.setId(3L);
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
    void whenUserIsNeither_thenReturnFalse() {
        boolean result = authorizationContext.isAuthorized(generalUser, 1L, "VIEW_PROFILE");

        assertFalse(result, "Should return false for unknown user types");
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
}