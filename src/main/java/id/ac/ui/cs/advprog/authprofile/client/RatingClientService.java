package id.ac.ui.cs.advprog.authprofile.client;

import id.ac.ui.cs.advprog.authprofile.dto.response.ApiResponseDto;
import id.ac.ui.cs.advprog.authprofile.dto.response.RatingResponseDto;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class RatingClientService {

    private static final Logger logger = LoggerFactory.getLogger(RatingClientService.class);

    private final RestTemplate restTemplate;
    private final String ratingServiceUrl;

    public RatingClientService(RestTemplate restTemplate,
                               @Value("${service.rating.url:http://localhost:8083}") String ratingServiceUrl) {
        this.restTemplate = restTemplate;
        this.ratingServiceUrl = ratingServiceUrl;
        logger.info("RatingClientService initialized with URL: {}", ratingServiceUrl);
    }

    @Timed(value = "rating_client_get_by_doctor_duration", description = "Time taken to get ratings by doctor ID")
    public List<RatingResponseDto> getRatingsByDoctorId(Long doctorId) {
        if (doctorId == null) {
            logger.warn("Doctor ID is null, returning empty list");
            return Collections.emptyList();
        }

        String url = ratingServiceUrl + "/api/rating/doctor/" + doctorId;
        logger.debug("Fetching ratings for doctor {} from URL: {}", doctorId, url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponseDto<RatingResponseDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<ApiResponseDto<RatingResponseDto>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ApiResponseDto<RatingResponseDto> responseBody = response.getBody();

                if (responseBody.getSuccess() == 1 && responseBody.getData() != null) {
                    logger.debug("Successfully fetched {} ratings for doctor {}",
                            responseBody.getData().size(), doctorId);
                    return responseBody.getData();
                } else {
                    logger.warn("Rating service returned unsuccessful response for doctor {}: {}",
                            doctorId, responseBody.getMessage());
                    return Collections.emptyList();
                }
            } else {
                logger.warn("Unexpected response status for doctor {}: {}", doctorId, response.getStatusCode());
                return Collections.emptyList();
            }

        } catch (RestClientException e) {
            logger.error("Failed to fetch ratings for doctor {} from rating service: {}", doctorId, e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Unexpected error while fetching ratings for doctor {}: {}", doctorId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Timed(value = "rating_client_get_summary_duration", description = "Time taken to get rating summary")
    public RatingSummary getRatingSummary(Long doctorId) {
        List<RatingResponseDto> ratings = getRatingsByDoctorId(doctorId);

        if (ratings.isEmpty()) {
            return new RatingSummary(0.0, 0);
        }

        double sum = ratings.stream()
                .mapToInt(RatingResponseDto::getScore)
                .sum();

        double average = sum / ratings.size();

        return new RatingSummary(average, ratings.size());
    }

    @Timed(value = "rating_client_health_check_duration", description = "Time taken to check rating service health")
    public boolean isRatingServiceHealthy() {
        try {
            String healthUrl = ratingServiceUrl + "/actuator/health";
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            boolean healthy = response.getStatusCode() == HttpStatus.OK;
            logger.debug("Rating service health check: {}", healthy ? "UP" : "DOWN");
            return healthy;
        } catch (Exception e) {
            logger.warn("Rating service health check failed: {}", e.getMessage());
            return false;
        }
    }

    // Inner class for rating summary
    public static class RatingSummary {
        private final double averageRating;
        private final int totalRatings;

        public RatingSummary(double averageRating, int totalRatings) {
            this.averageRating = averageRating;
            this.totalRatings = totalRatings;
        }

        public double getAverageRating() {
            return averageRating;
        }

        public int getTotalRatings() {
            return totalRatings;
        }
    }
}