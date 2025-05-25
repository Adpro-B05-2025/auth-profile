package id.ac.ui.cs.advprog.authprofile.client;

import id.ac.ui.cs.advprog.authprofile.dto.response.ApiResponseDto;
import id.ac.ui.cs.advprog.authprofile.dto.response.RatingResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingClientServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private RatingClientService ratingClientService;
    private final String ratingServiceUrl = "http://localhost:8083";

    @BeforeEach
    void setUp() {
        ratingClientService = new RatingClientService(restTemplate, ratingServiceUrl);
    }

    @Test
    void constructor_ShouldInitializeCorrectly() {
        // When creating a new instance
        RatingClientService service = new RatingClientService(restTemplate, ratingServiceUrl);

        // Then it should be initialized without throwing exceptions
        assertNotNull(service);
    }

    @Test
    void constructor_WithDefaultUrl_ShouldUseDefaultValue() {
        // When creating with null URL (testing default value behavior)
        RatingClientService service = new RatingClientService(restTemplate, null);

        // Then it should be created successfully
        assertNotNull(service);
    }

    @Test
    void getRatingsByDoctorId_WithValidResponse_ShouldReturnRatings() {
        // Given
        Long doctorId = 1L;
        List<RatingResponseDto> expectedRatings = createMockRatings();
        ApiResponseDto<RatingResponseDto> apiResponse = createSuccessfulApiResponse(expectedRatings);
        ResponseEntity<ApiResponseDto<RatingResponseDto>> responseEntity =
                new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(ratingServiceUrl + "/api/rating/doctor/" + doctorId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // When
        List<RatingResponseDto> result = ratingClientService.getRatingsByDoctorId(doctorId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedRatings, result);

        verify(restTemplate).exchange(
                eq(ratingServiceUrl + "/api/rating/doctor/" + doctorId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void getRatingsByDoctorId_WithNullDoctorId_ShouldReturnEmptyList() {
        // When
        List<RatingResponseDto> result = ratingClientService.getRatingsByDoctorId(null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify no REST call was made
        verifyNoInteractions(restTemplate);
    }

    @Test
    void getRatingsByDoctorId_WithUnsuccessfulApiResponse_ShouldReturnEmptyList() {
        // Given
        Long doctorId = 1L;
        ApiResponseDto<RatingResponseDto> apiResponse = createUnsuccessfulApiResponse();
        ResponseEntity<ApiResponseDto<RatingResponseDto>> responseEntity =
                new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // When
        List<RatingResponseDto> result = ratingClientService.getRatingsByDoctorId(doctorId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getRatingsByDoctorId_WithNullResponseBody_ShouldReturnEmptyList() {
        // Given
        Long doctorId = 1L;
        ResponseEntity<ApiResponseDto<RatingResponseDto>> responseEntity =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // When
        List<RatingResponseDto> result = ratingClientService.getRatingsByDoctorId(doctorId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getRatingsByDoctorId_WithNonOkStatus_ShouldReturnEmptyList() {
        // Given
        Long doctorId = 1L;
        ResponseEntity<ApiResponseDto<RatingResponseDto>> responseEntity =
                new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // When
        List<RatingResponseDto> result = ratingClientService.getRatingsByDoctorId(doctorId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getRatingsByDoctorId_WithRestClientException_ShouldReturnEmptyList() {
        // Given
        Long doctorId = 1L;

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RestClientException("Connection failed"));

        // When
        List<RatingResponseDto> result = ratingClientService.getRatingsByDoctorId(doctorId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getRatingsByDoctorId_WithGenericException_ShouldReturnEmptyList() {
        // Given
        Long doctorId = 1L;

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("Unexpected error"));

        // When
        List<RatingResponseDto> result = ratingClientService.getRatingsByDoctorId(doctorId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getRatingSummary_WithValidRatings_ShouldCalculateCorrectly() {
        // Given
        Long doctorId = 1L;
        List<RatingResponseDto> ratings = Arrays.asList(
                createRatingDto(1L, 5),
                createRatingDto(2L, 4),
                createRatingDto(3L, 3)
        );

        ApiResponseDto<RatingResponseDto> apiResponse = createSuccessfulApiResponse(ratings);
        ResponseEntity<ApiResponseDto<RatingResponseDto>> responseEntity =
                new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // When
        RatingClientService.RatingSummary result = ratingClientService.getRatingSummary(doctorId);

        // Then
        assertNotNull(result);
        assertEquals(4.0, result.getAverageRating(), 0.01); // (5+4+3)/3 = 4.0
        assertEquals(3, result.getTotalRatings());
    }

    @Test
    void getRatingSummary_WithNoRatings_ShouldReturnZeroValues() {
        // Given
        Long doctorId = 1L;

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RestClientException("Service unavailable"));

        // When
        RatingClientService.RatingSummary result = ratingClientService.getRatingSummary(doctorId);

        // Then
        assertNotNull(result);
        assertEquals(0.0, result.getAverageRating());
        assertEquals(0, result.getTotalRatings());
    }

    @Test
    void getRatingSummary_WithSingleRating_ShouldReturnCorrectAverage() {
        // Given
        Long doctorId = 1L;
        List<RatingResponseDto> ratings = Collections.singletonList(createRatingDto(1L, 5));

        ApiResponseDto<RatingResponseDto> apiResponse = createSuccessfulApiResponse(ratings);
        ResponseEntity<ApiResponseDto<RatingResponseDto>> responseEntity =
                new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // When
        RatingClientService.RatingSummary result = ratingClientService.getRatingSummary(doctorId);

        // Then
        assertNotNull(result);
        assertEquals(5.0, result.getAverageRating());
        assertEquals(1, result.getTotalRatings());
    }

    @Test
    void isRatingServiceHealthy_WithSuccessfulResponse_ShouldReturnTrue() {
        // Given
        ResponseEntity<String> healthResponse = new ResponseEntity<>("OK", HttpStatus.OK);

        when(restTemplate.getForEntity(ratingServiceUrl + "/actuator/health", String.class))
                .thenReturn(healthResponse);

        // When
        boolean result = ratingClientService.isRatingServiceHealthy();

        // Then
        assertTrue(result);
        verify(restTemplate).getForEntity(ratingServiceUrl + "/actuator/health", String.class);
    }

    @Test
    void isRatingServiceHealthy_WithNonOkStatus_ShouldReturnFalse() {
        // Given
        ResponseEntity<String> healthResponse = new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.getForEntity(ratingServiceUrl + "/actuator/health", String.class))
                .thenReturn(healthResponse);

        // When
        boolean result = ratingClientService.isRatingServiceHealthy();

        // Then
        assertFalse(result);
    }

    @Test
    void isRatingServiceHealthy_WithException_ShouldReturnFalse() {
        // Given
        when(restTemplate.getForEntity(ratingServiceUrl + "/actuator/health", String.class))
                .thenThrow(new RestClientException("Connection failed"));

        // When
        boolean result = ratingClientService.isRatingServiceHealthy();

        // Then
        assertFalse(result);
    }

    @Test
    void ratingSummary_ShouldHaveCorrectGetters() {
        // Given
        double expectedAverage = 4.5;
        int expectedTotal = 10;

        // When
        RatingClientService.RatingSummary summary = new RatingClientService.RatingSummary(expectedAverage, expectedTotal);

        // Then
        assertEquals(expectedAverage, summary.getAverageRating());
        assertEquals(expectedTotal, summary.getTotalRatings());
    }

    @Test
    void getRatingsByDoctorId_ShouldSetCorrectHeaders() {
        // Given
        Long doctorId = 1L;
        List<RatingResponseDto> expectedRatings = createMockRatings();
        ApiResponseDto<RatingResponseDto> apiResponse = createSuccessfulApiResponse(expectedRatings);
        ResponseEntity<ApiResponseDto<RatingResponseDto>> responseEntity =
                new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // When
        ratingClientService.getRatingsByDoctorId(doctorId);

        // Then
        verify(restTemplate).exchange(
                eq(ratingServiceUrl + "/api/rating/doctor/" + doctorId),
                eq(HttpMethod.GET),
                argThat(entity -> {
                    HttpEntity<?> httpEntity = (HttpEntity<?>) entity;
                    return httpEntity.getHeaders().getContentType().equals(MediaType.APPLICATION_JSON);
                }),
                any(ParameterizedTypeReference.class)
        );
    }

    // Helper methods for creating test data
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

    private ApiResponseDto<RatingResponseDto> createSuccessfulApiResponse(List<RatingResponseDto> ratings) {
        return ApiResponseDto.<RatingResponseDto>builder()
                .success(1)
                .message("Success")
                .data(ratings)
                .build();
    }

    private ApiResponseDto<RatingResponseDto> createUnsuccessfulApiResponse() {
        return ApiResponseDto.<RatingResponseDto>builder()
                .success(0)
                .message("Failed to get ratings")
                .data(null)
                .build();
    }
}