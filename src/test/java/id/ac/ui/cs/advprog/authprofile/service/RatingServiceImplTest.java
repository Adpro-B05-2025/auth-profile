package id.ac.ui.cs.advprog.authprofile.service;

import id.ac.ui.cs.advprog.authprofile.client.RatingClientService;
import id.ac.ui.cs.advprog.authprofile.config.MonitoringConfig;
import id.ac.ui.cs.advprog.authprofile.dto.response.RatingResponseDto;
import id.ac.ui.cs.advprog.authprofile.dto.response.RatingSummaryResponse;
import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
import id.ac.ui.cs.advprog.authprofile.repository.CareGiverRepository;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RatingServiceImplTest {

    @Mock
    private RatingClientService ratingClientService;

    @Mock
    private CareGiverRepository careGiverRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MonitoringConfig monitoringConfig;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    private RatingServiceImpl ratingService;

    @BeforeEach
    void setUp() {
        // Set up the monitoring config mock properly
        monitoringConfig.meterRegistry = meterRegistry;

        // Use lenient stubbing for monitoring methods that may not be called in all tests
        lenient().when(meterRegistry.counter(anyString(), any(Tags.class))).thenReturn(counter);
        lenient().when(meterRegistry.counter(anyString())).thenReturn(counter);
        lenient().doNothing().when(counter).increment();
        lenient().doNothing().when(counter).increment(anyDouble());

        ratingService = new RatingServiceImpl(
                ratingClientService,
                careGiverRepository,
                userRepository,
                monitoringConfig
        );
    }

    @Test
    void constructor_ShouldInitializeCorrectly() {
        // When creating a new instance
        RatingServiceImpl service = new RatingServiceImpl(
                ratingClientService,
                careGiverRepository,
                userRepository,
                monitoringConfig
        );

        // Then it should be initialized without throwing exceptions
        assertNotNull(service);
    }

    @Test
    void getRatingsByDoctorId_WithValidDoctorId_ShouldReturnRatings() {
        // Given
        Long doctorId = 1L;
        List<RatingResponseDto> expectedRatings = createMockRatings();

        when(ratingClientService.getRatingsByDoctorId(doctorId)).thenReturn(expectedRatings);

        // When
        List<RatingResponseDto> result = ratingService.getRatingsByDoctorId(doctorId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedRatings, result);

        // Verify monitoring calls
        verify(meterRegistry, times(2)).counter(anyString(), any(Tags.class));
        verify(counter, times(2)).increment();
        verify(ratingClientService).getRatingsByDoctorId(doctorId);
    }

    @Test
    void getRatingsByDoctorId_WithClientException_ShouldThrowAndRecordFailure() {
        // Given
        Long doctorId = 1L;
        RuntimeException expectedException = new RuntimeException("Client error");

        when(ratingClientService.getRatingsByDoctorId(doctorId)).thenThrow(expectedException);

        // When & Then
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            ratingService.getRatingsByDoctorId(doctorId);
        });

        assertEquals("Client error", thrown.getMessage());

        // Verify monitoring calls (request counter + failure counter)
        verify(meterRegistry, times(2)).counter(anyString(), any(Tags.class));
        verify(counter, times(2)).increment();
    }

    @Test
    void getRatingSummary_WithValidDoctorId_ShouldReturnSummary() {
        // Given
        Long doctorId = 1L;
        RatingClientService.RatingSummary clientSummary =
                new RatingClientService.RatingSummary(4.5, 10);

        when(ratingClientService.getRatingSummary(doctorId)).thenReturn(clientSummary);

        // When
        RatingSummaryResponse result = ratingService.getRatingSummary(doctorId);

        // Then
        assertNotNull(result);
        assertEquals(4.5, result.getAverageRating());
        assertEquals(10, result.getTotalRatings());

        // Verify monitoring calls
        verify(meterRegistry, times(2)).counter(anyString());
        verify(counter, times(2)).increment();
        verify(ratingClientService).getRatingSummary(doctorId);
    }

    @Test
    void getRatingSummary_WithClientException_ShouldReturnEmptySummary() {
        // Given
        Long doctorId = 1L;

        when(ratingClientService.getRatingSummary(doctorId))
                .thenThrow(new RuntimeException("Client error"));

        // When
        RatingSummaryResponse result = ratingService.getRatingSummary(doctorId);

        // Then
        assertNotNull(result);
        assertEquals(0.0, result.getAverageRating());
        assertEquals(0, result.getTotalRatings());

        // Verify monitoring calls (request counter + failure counter)
        verify(meterRegistry, times(2)).counter(anyString());
        verify(counter, times(2)).increment();
    }

    @Test
    void getCurrentUserRatingSummary_WithCaregiverUser_ShouldReturnSummary() {
        // Given
        Long userId = 1L;
        CareGiver caregiver = createMockCaregiver(userId);
        RatingClientService.RatingSummary clientSummary =
                new RatingClientService.RatingSummary(4.2, 5);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder =
                     mockStatic(SecurityContextHolder.class)) {

            // Mock security context
            mockSecurityContext(userId.toString());
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);

            when(userRepository.findById(userId)).thenReturn(Optional.of(caregiver));
            when(ratingClientService.getRatingSummary(userId)).thenReturn(clientSummary);

            // When
            RatingSummaryResponse result = ratingService.getCurrentUserRatingSummary();

            // Then
            assertNotNull(result);
            assertEquals(4.2, result.getAverageRating());
            assertEquals(5, result.getTotalRatings());

            verify(userRepository).findById(userId);
            verify(ratingClientService).getRatingSummary(userId);
        }
    }

    @Test
    void getCurrentUserRatingSummary_WithPacillianUser_ShouldReturnEmptySummary() {
        // Given
        Long userId = 1L;
        Pacillian pacillian = createMockPacillian(userId);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder =
                     mockStatic(SecurityContextHolder.class)) {

            // Mock security context
            mockSecurityContext(userId.toString());
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);

            when(userRepository.findById(userId)).thenReturn(Optional.of(pacillian));

            // When
            RatingSummaryResponse result = ratingService.getCurrentUserRatingSummary();

            // Then
            assertNotNull(result);
            assertEquals(0.0, result.getAverageRating());
            assertEquals(0, result.getTotalRatings());

            verify(userRepository).findById(userId);
            verifyNoInteractions(ratingClientService);
        }
    }

    @Test
    void getCurrentUserRatingSummary_WithUserNotFound_ShouldReturnEmptySummary() {
        // Given
        Long userId = 1L;

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder =
                     mockStatic(SecurityContextHolder.class)) {

            // Mock security context
            mockSecurityContext(userId.toString());
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When
            RatingSummaryResponse result = ratingService.getCurrentUserRatingSummary();

            // Then
            assertNotNull(result);
            assertEquals(0.0, result.getAverageRating());
            assertEquals(0, result.getTotalRatings());
        }
    }

    @Test
    void getCurrentUserRatingSummary_WithSecurityException_ShouldReturnEmptySummary() {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder =
                     mockStatic(SecurityContextHolder.class)) {

            mockedSecurityContextHolder.when(SecurityContextHolder::getContext)
                    .thenThrow(new RuntimeException("Security error"));

            // When
            RatingSummaryResponse result = ratingService.getCurrentUserRatingSummary();

            // Then
            assertNotNull(result);
            assertEquals(0.0, result.getAverageRating());
            assertEquals(0, result.getTotalRatings());
        }
    }

    @Test
    void isRatingServiceHealthy_WhenHealthy_ShouldReturnTrueAndRecordMetric() {
        // Given
        when(ratingClientService.isRatingServiceHealthy()).thenReturn(true);

        // When
        boolean result = ratingService.isRatingServiceHealthy();

        // Then
        assertTrue(result);
        verify(ratingClientService).isRatingServiceHealthy();
        verify(meterRegistry).counter("rating_service_health_checks",
                Tags.of("status", "healthy"));
        verify(counter).increment();
    }

    @Test
    void isRatingServiceHealthy_WhenUnhealthy_ShouldReturnFalseAndRecordMetric() {
        // Given
        when(ratingClientService.isRatingServiceHealthy()).thenReturn(false);

        // When
        boolean result = ratingService.isRatingServiceHealthy();

        // Then
        assertFalse(result);
        verify(ratingClientService).isRatingServiceHealthy();
        verify(meterRegistry).counter("rating_service_health_checks",
                Tags.of("status", "unhealthy"));
        verify(counter).increment();
    }

    @Test
    void updateCaregiverRatingCache_WithValidCaregiver_ShouldUpdateSuccessfully() {
        // Given
        Long caregiverId = 1L;
        CareGiver caregiver = createMockCaregiver(caregiverId);
        RatingClientService.RatingSummary summary =
                new RatingClientService.RatingSummary(4.7, 15);

        when(careGiverRepository.findById(caregiverId)).thenReturn(Optional.of(caregiver));
        when(ratingClientService.getRatingSummary(caregiverId)).thenReturn(summary);

        // When
        ratingService.updateCaregiverRatingCache(caregiverId);

        // Then
        assertEquals(4.7, caregiver.getAverageRating());
        assertEquals(15, caregiver.getRatingCount());

        verify(careGiverRepository).findById(caregiverId);
        verify(ratingClientService).getRatingSummary(caregiverId);
        verify(careGiverRepository).save(caregiver);
        verify(meterRegistry).counter("rating_cache_updates_successful");
        verify(counter).increment();
    }

    @Test
    void updateCaregiverRatingCache_WithNonExistentCaregiver_ShouldRecordFailure() {
        // Given
        Long caregiverId = 1L;

        when(careGiverRepository.findById(caregiverId)).thenReturn(Optional.empty());

        // When
        ratingService.updateCaregiverRatingCache(caregiverId);

        // Then
        verify(careGiverRepository).findById(caregiverId);
        verifyNoInteractions(ratingClientService);
        verify(careGiverRepository, never()).save(any());
        verify(meterRegistry).counter("rating_cache_updates_failed");
        verify(counter).increment();
    }

    @Test
    void updateCaregiverRatingCache_WithClientException_ShouldRecordFailure() {
        // Given
        Long caregiverId = 1L;
        CareGiver caregiver = createMockCaregiver(caregiverId);

        when(careGiverRepository.findById(caregiverId)).thenReturn(Optional.of(caregiver));
        when(ratingClientService.getRatingSummary(caregiverId))
                .thenThrow(new RuntimeException("Client error"));

        // When
        ratingService.updateCaregiverRatingCache(caregiverId);

        // Then
        verify(careGiverRepository).findById(caregiverId);
        verify(ratingClientService).getRatingSummary(caregiverId);
        verify(careGiverRepository, never()).save(any());
        verify(meterRegistry).counter("rating_cache_updates_failed");
        verify(counter).increment();
    }

    @Test
    void updateAllCaregiverRatingCaches_WithMultipleCaregivers_ShouldUpdateAll() {
        // Given
        CareGiver caregiver1 = createMockCaregiver(1L);
        CareGiver caregiver2 = createMockCaregiver(2L);
        List<CareGiver> caregivers = Arrays.asList(caregiver1, caregiver2);

        RatingClientService.RatingSummary summary1 =
                new RatingClientService.RatingSummary(4.0, 10);
        RatingClientService.RatingSummary summary2 =
                new RatingClientService.RatingSummary(4.5, 12);

        when(careGiverRepository.findAll()).thenReturn(caregivers);
        when(careGiverRepository.findById(1L)).thenReturn(Optional.of(caregiver1));
        when(careGiverRepository.findById(2L)).thenReturn(Optional.of(caregiver2));
        when(ratingClientService.getRatingSummary(1L)).thenReturn(summary1);
        when(ratingClientService.getRatingSummary(2L)).thenReturn(summary2);

        // When
        ratingService.updateAllCaregiverRatingCaches();

        // Then
        verify(careGiverRepository).findAll();
        verify(careGiverRepository).findById(1L);
        verify(careGiverRepository).findById(2L);
        verify(ratingClientService).getRatingSummary(1L);
        verify(ratingClientService).getRatingSummary(2L);
        verify(careGiverRepository, times(2)).save(any(CareGiver.class));

        // Verify bulk update metrics
        verify(meterRegistry).counter("rating_bulk_cache_updates_total");
        verify(meterRegistry).counter("rating_bulk_cache_updates_successful");
        verify(meterRegistry).counter("rating_bulk_cache_updates_failed");
    }

    @Test
    void updateAllCaregiverRatingCaches_WithSomeFailures_ShouldContinueAndRecordMetrics() {
        // Given
        CareGiver caregiver1 = createMockCaregiver(1L);
        CareGiver caregiver2 = createMockCaregiver(2L);
        List<CareGiver> caregivers = Arrays.asList(caregiver1, caregiver2);

        RatingClientService.RatingSummary summary1 =
                new RatingClientService.RatingSummary(4.0, 10);

        when(careGiverRepository.findAll()).thenReturn(caregivers);
        when(careGiverRepository.findById(1L)).thenReturn(Optional.of(caregiver1));
        when(careGiverRepository.findById(2L)).thenReturn(Optional.empty()); // This will fail
        when(ratingClientService.getRatingSummary(1L)).thenReturn(summary1);

        // When
        ratingService.updateAllCaregiverRatingCaches();

        // Then
        verify(careGiverRepository).findAll();
        verify(careGiverRepository).findById(1L);
        verify(careGiverRepository).findById(2L);
        verify(ratingClientService).getRatingSummary(1L);
        verify(careGiverRepository).save(caregiver1);

        // Verify metrics recorded both successes and failures
        verify(meterRegistry).counter("rating_bulk_cache_updates_total");
        verify(meterRegistry).counter("rating_bulk_cache_updates_successful");
        verify(meterRegistry).counter("rating_bulk_cache_updates_failed");
    }

    @Test
    void updateAllCaregiverRatingCaches_WithRepositoryException_ShouldRecordError() {
        // Given
        when(careGiverRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When
        ratingService.updateAllCaregiverRatingCaches();

        // Then
        verify(careGiverRepository).findAll();
        verify(meterRegistry).counter("rating_bulk_cache_updates_errors");
        verify(counter).increment();
    }

    // Helper methods
    private void mockSecurityContext(String userId) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(userId);
    }

    private List<RatingResponseDto> createMockRatings() {
        return Arrays.asList(
                createRatingDto(1L, 5),
                createRatingDto(2L, 4)
        );
    }

    private RatingResponseDto createRatingDto(Long id, Integer score) {
        return RatingResponseDto.builder()
                .id(id)
                .consultationId(id)
                .doctorId(1L)
                .score(score)
                .comment("Test comment " + id)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private CareGiver createMockCaregiver(Long id) {
        CareGiver caregiver = new CareGiver();
        caregiver.setId(id);
        caregiver.setEmail("caregiver" + id + "@test.com");
        caregiver.setName("CareGiver " + id);
        caregiver.setSpeciality("General Medicine");
        caregiver.setWorkAddress("Hospital " + id);
        caregiver.setAverageRating(0.0);
        caregiver.setRatingCount(0);
        return caregiver;
    }

    private Pacillian createMockPacillian(Long id) {
        Pacillian pacillian = new Pacillian();
        pacillian.setId(id);
        pacillian.setEmail("pacillian" + id + "@test.com");
        pacillian.setName("Pacillian " + id);
        pacillian.setMedicalHistory("No major issues");
        return pacillian;
    }
}