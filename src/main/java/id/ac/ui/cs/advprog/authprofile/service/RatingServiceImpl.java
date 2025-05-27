package id.ac.ui.cs.advprog.authprofile.service;

import id.ac.ui.cs.advprog.authprofile.client.RatingClientService;
import id.ac.ui.cs.advprog.authprofile.config.MonitoringConfig;
import id.ac.ui.cs.advprog.authprofile.dto.response.RatingResponseDto;
import id.ac.ui.cs.advprog.authprofile.dto.response.RatingSummaryResponse;
import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.repository.CareGiverRepository;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Tags;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RatingServiceImpl implements IRatingService {

    private static final Logger logger = LoggerFactory.getLogger(RatingServiceImpl.class);

    private final RatingClientService ratingClientService;
    private final CareGiverRepository careGiverRepository;
    private final UserRepository userRepository;
    private final MonitoringConfig monitoringConfig;

    @Autowired
    public RatingServiceImpl(RatingClientService ratingClientService,
                             CareGiverRepository careGiverRepository,
                             UserRepository userRepository,
                             MonitoringConfig monitoringConfig) {
        this.ratingClientService = ratingClientService;
        this.careGiverRepository = careGiverRepository;
        this.userRepository = userRepository;
        this.monitoringConfig = monitoringConfig;
    }

    @Override
    @Timed(value = "rating_service_get_ratings_duration", description = "Time taken to get ratings by doctor ID")
    public List<RatingResponseDto> getRatingsByDoctorId(Long doctorId) {
        logger.debug("Getting ratings for doctor ID: {}", doctorId);

        // Record rating request
        monitoringConfig.meterRegistry.counter("rating_requests_total",
                Tags.of("operation", "get_ratings")).increment();

        try {
            List<RatingResponseDto> ratings = ratingClientService.getRatingsByDoctorId(doctorId);

            // Record success
            monitoringConfig.meterRegistry.counter("rating_requests_successful",
                    Tags.of("operation", "get_ratings")).increment();

            logger.debug("Retrieved {} ratings for doctor ID: {}", ratings.size(), doctorId);
            return ratings;

        } catch (Exception e) {
            // Record failure
            monitoringConfig.meterRegistry.counter("rating_requests_failed",
                    Tags.of("operation", "get_ratings", "reason", "client_error")).increment();

            logger.error("Failed to get ratings for doctor ID: {}", doctorId, e);
            throw e;
        }
    }

    @Override
    @Cacheable(value = "ratingSummary", key = "#doctorId")
    @Timed(value = "rating_service_get_summary_duration", description = "Time taken to get rating summary")
    public RatingSummaryResponse getRatingSummary(Long doctorId) {
        logger.debug("Getting rating summary for doctor ID: {}", doctorId);

        // Record summary request
        monitoringConfig.meterRegistry.counter("rating_summary_requests_total").increment();

        try {
            RatingClientService.RatingSummary summary = ratingClientService.getRatingSummary(doctorId);

            RatingSummaryResponse response = new RatingSummaryResponse(
                    summary.getAverageRating(),
                    summary.getTotalRatings()
            );

            // Record success
            monitoringConfig.meterRegistry.counter("rating_summary_requests_successful").increment();

            logger.debug("Rating summary for doctor {}: avg={}, total={}",
                    doctorId, response.getAverageRating(), response.getTotalRatings());

            return response;

        } catch (Exception e) {
            // Record failure
            monitoringConfig.meterRegistry.counter("rating_summary_requests_failed").increment();

            logger.error("Failed to get rating summary for doctor ID: {}", doctorId, e);
            // Return empty summary instead of throwing exception
            return new RatingSummaryResponse(0.0, 0);
        }
    }

    @Override
    @Timed(value = "rating_service_get_current_user_summary_duration", description = "Time taken to get current user rating summary")
    public RatingSummaryResponse getCurrentUserRatingSummary() {
        logger.debug("Getting rating summary for current user");

        try {
            // Get current user from security context
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            Long userId = Long.parseLong(userDetails.getUsername());

            // Check if user is a caregiver
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            if (!(user instanceof CareGiver)) {
                logger.warn("Current user {} is not a caregiver, returning empty rating summary", userId);
                return new RatingSummaryResponse(0.0, 0);
            }

            return getRatingSummary(userId);

        } catch (Exception e) {
            logger.error("Failed to get rating summary for current user", e);
            return new RatingSummaryResponse(0.0, 0);
        }
    }

    @Override
    @Timed(value = "rating_service_health_check_duration", description = "Time taken to check rating service health")
    public boolean isRatingServiceHealthy() {
        boolean healthy = ratingClientService.isRatingServiceHealthy();

        // Record health check result
        monitoringConfig.meterRegistry.counter("rating_service_health_checks",
                Tags.of("status", healthy ? "healthy" : "unhealthy")).increment();

        return healthy;
    }

    @Override
    @Transactional
    @Timed(value = "rating_service_update_cache_duration", description = "Time taken to update caregiver rating cache")
    public void updateCaregiverRatingCache(Long caregiverId) {
        logger.debug("Updating rating cache for caregiver: {}", caregiverId);

        try {
            CareGiver caregiver = careGiverRepository.findById(caregiverId)
                    .orElseThrow(() -> new EntityNotFoundException("Caregiver not found: " + caregiverId));

            RatingClientService.RatingSummary summary = ratingClientService.getRatingSummary(caregiverId);

            // Update caregiver's rating fields
            caregiver.setAverageRating(summary.getAverageRating());
            caregiver.setRatingCount(summary.getTotalRatings());

            careGiverRepository.save(caregiver);

            // Record cache update
            monitoringConfig.meterRegistry.counter("rating_cache_updates_successful").increment();

            logger.info("Updated rating cache for caregiver {}: avg={}, count={}",
                    caregiverId, summary.getAverageRating(), summary.getTotalRatings());

        } catch (Exception e) {
            monitoringConfig.meterRegistry.counter("rating_cache_updates_failed").increment();
            logger.error("Failed to update rating cache for caregiver: {}", caregiverId, e);
        }
    }

    @Override
    @Async("searchTaskExecutor")
    @Timed(value = "rating_service_bulk_update_duration", description = "Time taken to bulk update all caregiver rating caches")
    public void updateAllCaregiverRatingCaches() {
        logger.info("Starting bulk update of all caregiver rating caches");

        try {
            List<CareGiver> allCaregivers = careGiverRepository.findAll();
            int totalCaregivers = allCaregivers.size();

            BulkUpdateResult result = processBulkCacheUpdate(allCaregivers);

            logger.info("Bulk update completed: {} total, {} successful, {} failed",
                    totalCaregivers, result.successCount(), result.failureCount());

            // Record bulk update metrics
            monitoringConfig.meterRegistry.counter("rating_bulk_cache_updates_total").increment(totalCaregivers);
            monitoringConfig.meterRegistry.counter("rating_bulk_cache_updates_successful").increment(result.successCount());
            monitoringConfig.meterRegistry.counter("rating_bulk_cache_updates_failed").increment(result.failureCount());

        } catch (Exception e) {
            logger.error("Failed to perform bulk rating cache update", e);
            monitoringConfig.meterRegistry.counter("rating_bulk_cache_updates_errors").increment();
        }
    }

    /**
     * Processes the bulk cache update for all caregivers
     *
     * @param caregivers List of caregivers to update
     * @return BulkUpdateResult containing success and failure counts
     */
    private BulkUpdateResult processBulkCacheUpdate(List<CareGiver> caregivers) {
        int successCount = 0;
        int failureCount = 0;

        for (CareGiver caregiver : caregivers) {
            try {
                updateCaregiverRatingCache(caregiver.getId());
                successCount++;
            } catch (Exception e) {
                failureCount++;
                logger.warn("Failed to update cache for caregiver {}: {}", caregiver.getId(), e.getMessage());
            }
        }

        return new BulkUpdateResult(successCount, failureCount);
    }

    /**
     * Record to hold bulk update results
     */
    private record BulkUpdateResult(int successCount, int failureCount) {}
}