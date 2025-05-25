package id.ac.ui.cs.advprog.authprofile.scheduler;

import id.ac.ui.cs.advprog.authprofile.service.IRatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RatingCacheSchedulerTest {

    @Mock
    private IRatingService ratingService;

    private RatingCacheScheduler scheduler;

    @BeforeEach
    void setUp() {
        // We don't use @InjectMocks because we need to test different constructor scenarios
    }

    @Test
    void constructor_WithSchedulerEnabled_ShouldInitializeCorrectly() {
        // When
        scheduler = new RatingCacheScheduler(ratingService, true);

        // Then
        assertNotNull(scheduler);
        // Constructor should log that scheduler is enabled (verified implicitly)
    }

    @Test
    void constructor_WithSchedulerDisabled_ShouldInitializeCorrectly() {
        // When
        scheduler = new RatingCacheScheduler(ratingService, false);

        // Then
        assertNotNull(scheduler);
        // Constructor should log that scheduler is disabled (verified implicitly)
    }


    @Test
    void updateCaregiverRatingCaches_WhenEnabledAndHealthy_ShouldUpdateCaches() {
        // Given
        scheduler = new RatingCacheScheduler(ratingService, true);
        when(ratingService.isRatingServiceHealthy()).thenReturn(true);
        doNothing().when(ratingService).updateAllCaregiverRatingCaches();

        // When
        scheduler.updateCaregiverRatingCaches();

        // Then
        verify(ratingService).isRatingServiceHealthy();
        verify(ratingService).updateAllCaregiverRatingCaches();
    }

    @Test
    void updateCaregiverRatingCaches_WhenEnabledButUnhealthy_ShouldSkipUpdate() {
        // Given
        scheduler = new RatingCacheScheduler(ratingService, true);
        when(ratingService.isRatingServiceHealthy()).thenReturn(false);

        // When
        scheduler.updateCaregiverRatingCaches();

        // Then
        verify(ratingService).isRatingServiceHealthy();
        verify(ratingService, never()).updateAllCaregiverRatingCaches();
    }

    @Test
    void updateCaregiverRatingCaches_WhenDisabled_ShouldNotExecute() {
        // Given
        scheduler = new RatingCacheScheduler(ratingService, false);

        // When
        scheduler.updateCaregiverRatingCaches();

        // Then
        verifyNoInteractions(ratingService);
    }

    @Test
    void updateCaregiverRatingCaches_WhenHealthCheckThrowsException_ShouldHandleGracefully() {
        // Given
        scheduler = new RatingCacheScheduler(ratingService, true);
        when(ratingService.isRatingServiceHealthy()).thenThrow(new RuntimeException("Health check failed"));

        // When
        assertDoesNotThrow(() -> scheduler.updateCaregiverRatingCaches());

        // Then
        verify(ratingService).isRatingServiceHealthy();
        verify(ratingService, never()).updateAllCaregiverRatingCaches();
    }

    @Test
    void updateCaregiverRatingCaches_WhenUpdateThrowsException_ShouldHandleGracefully() {
        // Given
        scheduler = new RatingCacheScheduler(ratingService, true);
        when(ratingService.isRatingServiceHealthy()).thenReturn(true);
        doThrow(new RuntimeException("Update failed")).when(ratingService).updateAllCaregiverRatingCaches();

        // When
        assertDoesNotThrow(() -> scheduler.updateCaregiverRatingCaches());

        // Then
        verify(ratingService).isRatingServiceHealthy();
        verify(ratingService).updateAllCaregiverRatingCaches();
    }

    @Test
    void updateCaregiverRatingCaches_WhenHealthyAndUpdateSucceeds_ShouldCompleteNormally() {
        // Given
        scheduler = new RatingCacheScheduler(ratingService, true);
        when(ratingService.isRatingServiceHealthy()).thenReturn(true);
        doNothing().when(ratingService).updateAllCaregiverRatingCaches();

        // When
        assertDoesNotThrow(() -> scheduler.updateCaregiverRatingCaches());

        // Then
        verify(ratingService).isRatingServiceHealthy();
        verify(ratingService).updateAllCaregiverRatingCaches();
    }

    @Test
    void checkRatingServiceHealth_WhenEnabledAndHealthy_ShouldPerformHealthCheck() {
        // Given
        scheduler = new RatingCacheScheduler(ratingService, true);
        when(ratingService.isRatingServiceHealthy()).thenReturn(true);

        // When
        scheduler.checkRatingServiceHealth();

        // Then
        verify(ratingService).isRatingServiceHealthy();
    }

    @Test
    void checkRatingServiceHealth_WhenEnabledAndUnhealthy_ShouldLogWarning() {
        // Given
        scheduler = new RatingCacheScheduler(ratingService, true);
        when(ratingService.isRatingServiceHealthy()).thenReturn(false);

        // When
        assertDoesNotThrow(() -> scheduler.checkRatingServiceHealth());

        // Then
        verify(ratingService).isRatingServiceHealthy();
    }

    @Test
    void checkRatingServiceHealth_WhenDisabled_ShouldNotExecute() {
        // Given
        scheduler = new RatingCacheScheduler(ratingService, false);

        // When
        scheduler.checkRatingServiceHealth();

        // Then
        verifyNoInteractions(ratingService);
    }

    @Test
    void checkRatingServiceHealth_WhenThrowsException_ShouldHandleGracefully() {
        // Given
        scheduler = new RatingCacheScheduler(ratingService, true);
        when(ratingService.isRatingServiceHealthy()).thenThrow(new RuntimeException("Network error"));

        // When
        assertDoesNotThrow(() -> scheduler.checkRatingServiceHealth());

        // Then
        verify(ratingService).isRatingServiceHealthy();
    }

    @Test
    void scheduledMethods_WhenEnabled_ShouldExecuteBasedOnSchedulerFlag() {
        // Given
        scheduler = new RatingCacheScheduler(ratingService, true);
        when(ratingService.isRatingServiceHealthy()).thenReturn(true);
        doNothing().when(ratingService).updateAllCaregiverRatingCaches();

        // When - simulate scheduled execution
        scheduler.updateCaregiverRatingCaches();
        scheduler.checkRatingServiceHealth();

        // Then
        verify(ratingService, times(2)).isRatingServiceHealthy(); // Called in both methods
        verify(ratingService).updateAllCaregiverRatingCaches();
    }

    @Test
    void scheduledMethods_WhenDisabled_ShouldNotExecuteAnyServiceCalls() {
        // Given
        scheduler = new RatingCacheScheduler(ratingService, false);

        // When - simulate scheduled execution
        scheduler.updateCaregiverRatingCaches();
        scheduler.checkRatingServiceHealth();

        // Then
        verifyNoInteractions(ratingService);
    }

    @Test
    void updateCacheMethod_ShouldFollowCorrectExecutionFlow() {
        // Given
        scheduler = new RatingCacheScheduler(ratingService, true);
        when(ratingService.isRatingServiceHealthy()).thenReturn(true);
        doNothing().when(ratingService).updateAllCaregiverRatingCaches();

        // When
        scheduler.updateCaregiverRatingCaches();

        // Then - verify execution order
        var inOrder = inOrder(ratingService);
        inOrder.verify(ratingService).isRatingServiceHealthy();
        inOrder.verify(ratingService).updateAllCaregiverRatingCaches();
    }

    @Test
    void healthCheckMethod_ShouldOnlyCallHealthService() {
        // Given
        scheduler = new RatingCacheScheduler(ratingService, true);
        when(ratingService.isRatingServiceHealthy()).thenReturn(true);

        // When
        scheduler.checkRatingServiceHealth();

        // Then
        verify(ratingService).isRatingServiceHealthy();
        verify(ratingService, never()).updateAllCaregiverRatingCaches();
    }

    @Test
    void constructor_ShouldAcceptBothServiceAndEnabledFlag() {
        // Given
        IRatingService mockService = mock(IRatingService.class);

        // When - test both enabled and disabled scenarios
        RatingCacheScheduler enabledScheduler = new RatingCacheScheduler(mockService, true);
        RatingCacheScheduler disabledScheduler = new RatingCacheScheduler(mockService, false);

        // Then
        assertNotNull(enabledScheduler);
        assertNotNull(disabledScheduler);
    }

    @Test
    void schedulerBehavior_ShouldBeConsistentAcrossMethods() {
        // Test that both scheduled methods respect the enabled flag consistently

        // Given - enabled scheduler
        scheduler = new RatingCacheScheduler(ratingService, true);
        when(ratingService.isRatingServiceHealthy()).thenReturn(true);
        doNothing().when(ratingService).updateAllCaregiverRatingCaches();

        // When
        scheduler.updateCaregiverRatingCaches();
        scheduler.checkRatingServiceHealth();

        // Then
        verify(ratingService, times(2)).isRatingServiceHealthy();
        verify(ratingService).updateAllCaregiverRatingCaches();

        // Reset mocks
        reset(ratingService);

        // Given - disabled scheduler
        scheduler = new RatingCacheScheduler(ratingService, false);

        // When
        scheduler.updateCaregiverRatingCaches();
        scheduler.checkRatingServiceHealth();

        // Then
        verifyNoInteractions(ratingService);
    }

    @Test
    void updateMethod_WhenMultipleExceptionsOccur_ShouldHandleEachGracefully() {
        // Given
        scheduler = new RatingCacheScheduler(ratingService, true);

        // Test health check exception
        when(ratingService.isRatingServiceHealthy()).thenThrow(new RuntimeException("Health check error"));
        assertDoesNotThrow(() -> scheduler.updateCaregiverRatingCaches());

        // Reset and test update exception
        reset(ratingService);
        when(ratingService.isRatingServiceHealthy()).thenReturn(true);
        doThrow(new RuntimeException("Update error")).when(ratingService).updateAllCaregiverRatingCaches();
        assertDoesNotThrow(() -> scheduler.updateCaregiverRatingCaches());

        // Then - both scenarios should be handled gracefully
        verify(ratingService).isRatingServiceHealthy();
        verify(ratingService).updateAllCaregiverRatingCaches();
    }

    @Test
    void healthCheckMethod_WithDifferentHealthStates_ShouldHandleBothCorrectly() {
        // Given
        scheduler = new RatingCacheScheduler(ratingService, true);

        // When - healthy service
        when(ratingService.isRatingServiceHealthy()).thenReturn(true);
        assertDoesNotThrow(() -> scheduler.checkRatingServiceHealth());

        // When - unhealthy service
        when(ratingService.isRatingServiceHealthy()).thenReturn(false);
        assertDoesNotThrow(() -> scheduler.checkRatingServiceHealth());

        // Then
        verify(ratingService, times(2)).isRatingServiceHealthy();
    }
}