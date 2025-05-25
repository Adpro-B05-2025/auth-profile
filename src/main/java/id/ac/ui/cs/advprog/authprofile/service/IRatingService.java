package id.ac.ui.cs.advprog.authprofile.service;

import id.ac.ui.cs.advprog.authprofile.dto.response.RatingResponseDto;
import id.ac.ui.cs.advprog.authprofile.dto.response.RatingSummaryResponse;

import java.util.List;

public interface IRatingService {

    /**
     * Get all ratings for a specific caregiver/doctor
     * @param doctorId the caregiver/doctor ID
     * @return list of ratings
     */
    List<RatingResponseDto> getRatingsByDoctorId(Long doctorId);

    /**
     * Get rating summary (average and count) for a specific caregiver/doctor
     * @param doctorId the caregiver/doctor ID
     * @return rating summary with average and total count
     */
    RatingSummaryResponse getRatingSummary(Long doctorId);

    /**
     * Get rating summary for the current authenticated user (if they are a caregiver)
     * @return rating summary for current user
     */
    RatingSummaryResponse getCurrentUserRatingSummary();

    /**
     * Check if the rating service is available and healthy
     * @return true if rating service is healthy, false otherwise
     */
    boolean isRatingServiceHealthy();

    /**
     * Update caregiver's cached rating data based on latest ratings
     * This method should be called periodically or when ratings are updated
     * @param caregiverId the caregiver ID to update
     */
    void updateCaregiverRatingCache(Long caregiverId);

    /**
     * Bulk update all caregivers' rating cache
     * This is useful for scheduled tasks to keep ratings up-to-date
     */
    void updateAllCaregiverRatingCaches();
}