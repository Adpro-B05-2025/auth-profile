package id.ac.ui.cs.advprog.authprofile.scheduler;

import id.ac.ui.cs.advprog.authprofile.service.IRatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler to periodically update caregiver rating caches
 * This ensures that the rating data in the auth-profile service stays up-to-date
 * with the rating service
 */
@Component
@ConditionalOnProperty(value = "rating.cache.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class RatingCacheScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RatingCacheScheduler.class);

    private final IRatingService ratingService;
    private final boolean schedulerEnabled;

    @Autowired
    public RatingCacheScheduler(IRatingService ratingService,
                                @Value("${rating.cache.scheduler.enabled:true}") boolean schedulerEnabled) {
        this.ratingService = ratingService;
        this.schedulerEnabled = schedulerEnabled;

        if (schedulerEnabled) {
            logger.info("Rating cache scheduler is ENABLED");
        } else {
            logger.info("Rating cache scheduler is DISABLED");
        }
    }

    /**
     * Update all caregiver rating caches every hour
     * This can be configured via application properties
     */
    @Scheduled(fixedRateString = "${rating.cache.update.interval:3600000}") // Default: 1 hour
    public void updateCaregiverRatingCaches() {
        if (!schedulerEnabled) {
            return;
        }

        logger.info("Starting scheduled rating cache update");

        try {
            // Check if rating service is healthy before attempting update
            if (!ratingService.isRatingServiceHealthy()) {
                logger.warn("Rating service is not healthy, skipping cache update");
                return;
            }

            ratingService.updateAllCaregiverRatingCaches();
            logger.info("Scheduled rating cache update completed successfully");

        } catch (Exception e) {
            logger.error("Scheduled rating cache update failed", e);
        }
    }

    /**
     * Health check of rating service every 5 minutes
     * This helps monitor the connectivity to the rating service
     */
    @Scheduled(fixedRateString = "${rating.health.check.interval:300000}") // Default: 5 minutes
    public void checkRatingServiceHealth() {
        if (!schedulerEnabled) {
            return;
        }

        try {
            boolean healthy = ratingService.isRatingServiceHealthy();
            if (healthy) {
                logger.debug("Rating service health check: OK");
            } else {
                logger.warn("Rating service health check: FAILED - Service may be down");
            }
        } catch (Exception e) {
            logger.error("Rating service health check failed with exception", e);
        }
    }
}