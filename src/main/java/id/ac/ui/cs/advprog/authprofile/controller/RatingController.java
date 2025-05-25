package id.ac.ui.cs.advprog.authprofile.controller;

import id.ac.ui.cs.advprog.authprofile.dto.response.RatingResponseDto;
import id.ac.ui.cs.advprog.authprofile.dto.response.RatingSummaryResponse;
import id.ac.ui.cs.advprog.authprofile.security.annotation.RequiresAuthorization;
import id.ac.ui.cs.advprog.authprofile.service.IRatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private static final Logger logger = LoggerFactory.getLogger(RatingController.class);

    private final IRatingService ratingService;

    @Autowired
    public RatingController(IRatingService ratingService) {
        this.ratingService = ratingService;
    }

    /**
     * Get all ratings for a specific caregiver/doctor
     */
    @GetMapping("/doctor/{doctorId}")
    @RequiresAuthorization(action = "VIEW_CAREGIVER", resourceIdExpression = "#doctorId")
    public ResponseEntity<List<RatingResponseDto>> getRatingsByDoctorId(@PathVariable Long doctorId) {
        logger.debug("Getting ratings for doctor ID: {}", doctorId);

        try {
            List<RatingResponseDto> ratings = ratingService.getRatingsByDoctorId(doctorId);
            return ResponseEntity.ok(ratings);
        } catch (Exception e) {
            logger.error("Failed to get ratings for doctor {}: {}", doctorId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get rating summary for a specific caregiver/doctor
     */
    @GetMapping("/doctor/{doctorId}/summary")
    @RequiresAuthorization(action = "VIEW_CAREGIVER", resourceIdExpression = "#doctorId")
    public ResponseEntity<RatingSummaryResponse> getRatingSummary(@PathVariable Long doctorId) {
        logger.debug("Getting rating summary for doctor ID: {}", doctorId);

        try {
            RatingSummaryResponse summary = ratingService.getRatingSummary(doctorId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("Failed to get rating summary for doctor {}: {}", doctorId, e.getMessage());
            // Return empty summary instead of error for better UX
            return ResponseEntity.ok(new RatingSummaryResponse(0.0, 0));
        }
    }

    /**
     * Get rating summary for the current authenticated user (if they are a caregiver)
     */
    @GetMapping("/my-summary")
    @RequiresAuthorization(action = "VIEW_OWN_PROFILE")
    public ResponseEntity<RatingSummaryResponse> getCurrentUserRatingSummary() {
        logger.debug("Getting rating summary for current user");

        try {
            RatingSummaryResponse summary = ratingService.getCurrentUserRatingSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("Failed to get rating summary for current user: {}", e.getMessage());
            return ResponseEntity.ok(new RatingSummaryResponse(0.0, 0));
        }
    }

    /**
     * Health check endpoint for rating service integration
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkRatingServiceHealth() {
        logger.debug("Checking rating service health");

        boolean healthy = ratingService.isRatingServiceHealthy();

        Map<String, Object> response = new HashMap<>();
        response.put("ratingServiceHealthy", healthy);
        response.put("status", healthy ? "UP" : "DOWN");
        response.put("timestamp", System.currentTimeMillis());

        HttpStatus status = healthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(response);
    }

}