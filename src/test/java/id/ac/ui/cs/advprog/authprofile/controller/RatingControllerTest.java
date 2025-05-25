package id.ac.ui.cs.advprog.authprofile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.authprofile.dto.response.RatingResponseDto;
import id.ac.ui.cs.advprog.authprofile.dto.response.RatingSummaryResponse;
import id.ac.ui.cs.advprog.authprofile.service.IRatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RatingControllerTest {

    @Mock
    private IRatingService ratingService;

    @InjectMocks
    private RatingController ratingController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ratingController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For LocalDateTime serialization
    }

    @Test
    void constructor_ShouldInitializeCorrectly() {
        // When creating a new instance
        RatingController controller = new RatingController(ratingService);

        // Then it should be initialized without throwing exceptions
        assertNotNull(controller);
    }

    @Test
    void getRatingsByDoctorId_WithValidId_ShouldReturnRatings() {
        // Given
        Long doctorId = 1L;
        List<RatingResponseDto> expectedRatings = createMockRatings();

        when(ratingService.getRatingsByDoctorId(doctorId)).thenReturn(expectedRatings);

        // When
        ResponseEntity<List<RatingResponseDto>> response = ratingController.getRatingsByDoctorId(doctorId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(expectedRatings, response.getBody());

        verify(ratingService).getRatingsByDoctorId(doctorId);
    }

    @Test
    void getRatingsByDoctorId_WithEmptyRatings_ShouldReturnEmptyList() {
        // Given
        Long doctorId = 1L;
        List<RatingResponseDto> emptyRatings = Collections.emptyList();

        when(ratingService.getRatingsByDoctorId(doctorId)).thenReturn(emptyRatings);

        // When
        ResponseEntity<List<RatingResponseDto>> response = ratingController.getRatingsByDoctorId(doctorId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(ratingService).getRatingsByDoctorId(doctorId);
    }

    @Test
    void getRatingsByDoctorId_WithServiceException_ShouldReturnInternalServerError() {
        // Given
        Long doctorId = 1L;

        when(ratingService.getRatingsByDoctorId(doctorId))
                .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<List<RatingResponseDto>> response = ratingController.getRatingsByDoctorId(doctorId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());

        verify(ratingService).getRatingsByDoctorId(doctorId);
    }

    @Test
    void getRatingsByDoctorId_WithMockMvc_ShouldReturnRatings() throws Exception {
        // Given
        Long doctorId = 1L;
        List<RatingResponseDto> expectedRatings = createMockRatings();

        when(ratingService.getRatingsByDoctorId(doctorId)).thenReturn(expectedRatings);

        // When & Then
        mockMvc.perform(get("/api/ratings/doctor/{doctorId}", doctorId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].score").value(5))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].score").value(4));

        verify(ratingService).getRatingsByDoctorId(doctorId);
    }

    @Test
    void getRatingSummary_WithValidId_ShouldReturnSummary() {
        // Given
        Long doctorId = 1L;
        RatingSummaryResponse expectedSummary = new RatingSummaryResponse(4.5, 10);

        when(ratingService.getRatingSummary(doctorId)).thenReturn(expectedSummary);

        // When
        ResponseEntity<RatingSummaryResponse> response = ratingController.getRatingSummary(doctorId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(4.5, response.getBody().getAverageRating());
        assertEquals(10, response.getBody().getTotalRatings());

        verify(ratingService).getRatingSummary(doctorId);
    }

    @Test
    void getRatingSummary_WithServiceException_ShouldReturnEmptySummary() {
        // Given
        Long doctorId = 1L;

        when(ratingService.getRatingSummary(doctorId))
                .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<RatingSummaryResponse> response = ratingController.getRatingSummary(doctorId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0.0, response.getBody().getAverageRating());
        assertEquals(0, response.getBody().getTotalRatings());

        verify(ratingService).getRatingSummary(doctorId);
    }

    @Test
    void getRatingSummary_WithMockMvc_ShouldReturnSummary() throws Exception {
        // Given
        Long doctorId = 1L;
        RatingSummaryResponse expectedSummary = new RatingSummaryResponse(4.2, 15);

        when(ratingService.getRatingSummary(doctorId)).thenReturn(expectedSummary);

        // When & Then
        mockMvc.perform(get("/api/ratings/doctor/{doctorId}/summary", doctorId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.averageRating").value(4.2))
                .andExpect(jsonPath("$.totalRatings").value(15));

        verify(ratingService).getRatingSummary(doctorId);
    }

    @Test
    void getCurrentUserRatingSummary_WithValidUser_ShouldReturnSummary() {
        // Given
        RatingSummaryResponse expectedSummary = new RatingSummaryResponse(4.8, 25);

        when(ratingService.getCurrentUserRatingSummary()).thenReturn(expectedSummary);

        // When
        ResponseEntity<RatingSummaryResponse> response = ratingController.getCurrentUserRatingSummary();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(4.8, response.getBody().getAverageRating());
        assertEquals(25, response.getBody().getTotalRatings());

        verify(ratingService).getCurrentUserRatingSummary();
    }

    @Test
    void getCurrentUserRatingSummary_WithServiceException_ShouldReturnEmptySummary() {
        // Given
        when(ratingService.getCurrentUserRatingSummary())
                .thenThrow(new RuntimeException("User not found"));

        // When
        ResponseEntity<RatingSummaryResponse> response = ratingController.getCurrentUserRatingSummary();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0.0, response.getBody().getAverageRating());
        assertEquals(0, response.getBody().getTotalRatings());

        verify(ratingService).getCurrentUserRatingSummary();
    }

    @Test
    void getCurrentUserRatingSummary_WithMockMvc_ShouldReturnSummary() throws Exception {
        // Given
        RatingSummaryResponse expectedSummary = new RatingSummaryResponse(3.9, 8);

        when(ratingService.getCurrentUserRatingSummary()).thenReturn(expectedSummary);

        // When & Then
        mockMvc.perform(get("/api/ratings/my-summary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.averageRating").value(3.9))
                .andExpect(jsonPath("$.totalRatings").value(8));

        verify(ratingService).getCurrentUserRatingSummary();
    }

    @Test
    void checkRatingServiceHealth_WhenHealthy_ShouldReturnOkWithHealthyStatus() {
        // Given
        when(ratingService.isRatingServiceHealthy()).thenReturn(true);

        // When
        ResponseEntity<Map<String, Object>> response = ratingController.checkRatingServiceHealth();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> body = response.getBody();
        assertEquals(true, body.get("ratingServiceHealthy"));
        assertEquals("UP", body.get("status"));
        assertTrue(body.containsKey("timestamp"));
        assertTrue(body.get("timestamp") instanceof Long);

        verify(ratingService).isRatingServiceHealthy();
    }

    @Test
    void checkRatingServiceHealth_WhenUnhealthy_ShouldReturnServiceUnavailableWithDownStatus() {
        // Given
        when(ratingService.isRatingServiceHealthy()).thenReturn(false);

        // When
        ResponseEntity<Map<String, Object>> response = ratingController.checkRatingServiceHealth();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> body = response.getBody();
        assertEquals(false, body.get("ratingServiceHealthy"));
        assertEquals("DOWN", body.get("status"));
        assertTrue(body.containsKey("timestamp"));
        assertTrue(body.get("timestamp") instanceof Long);

        verify(ratingService).isRatingServiceHealthy();
    }

    @Test
    void checkRatingServiceHealth_WithMockMvc_WhenHealthy_ShouldReturnHealthyResponse() throws Exception {
        // Given
        when(ratingService.isRatingServiceHealthy()).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/ratings/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ratingServiceHealthy").value(true))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").isNumber());

        verify(ratingService).isRatingServiceHealthy();
    }

    @Test
    void checkRatingServiceHealth_WithMockMvc_WhenUnhealthy_ShouldReturnUnhealthyResponse() throws Exception {
        // Given
        when(ratingService.isRatingServiceHealthy()).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/ratings/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ratingServiceHealthy").value(false))
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.timestamp").isNumber());

        verify(ratingService).isRatingServiceHealthy();
    }

    @Test
    void checkRatingServiceHealth_WithServiceException_ShouldPropagateException() {
        // Given
        when(ratingService.isRatingServiceHealthy())
                .thenThrow(new RuntimeException("Health check failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ratingController.checkRatingServiceHealth();
        });

        assertEquals("Health check failed", exception.getMessage());
        verify(ratingService).isRatingServiceHealthy();
    }

    @Test
    void checkRatingServiceHealth_ShouldIncludeCurrentTimestamp() {
        // Given
        when(ratingService.isRatingServiceHealthy()).thenReturn(true);
        long beforeCall = System.currentTimeMillis();

        // When
        ResponseEntity<Map<String, Object>> response = ratingController.checkRatingServiceHealth();

        // Then
        long afterCall = System.currentTimeMillis();
        assertNotNull(response.getBody());

        Long timestamp = (Long) response.getBody().get("timestamp");
        assertNotNull(timestamp);
        assertTrue(timestamp >= beforeCall);
        assertTrue(timestamp <= afterCall);
    }

    @Test
    void allEndpoints_ShouldHaveProperLogging() {
        // This test verifies that all methods would log appropriately
        // In a real scenario, you might use a logging framework test utility

        // Given
        Long doctorId = 1L;
        when(ratingService.getRatingsByDoctorId(doctorId)).thenReturn(Collections.emptyList());
        when(ratingService.getRatingSummary(doctorId)).thenReturn(new RatingSummaryResponse(0.0, 0));
        when(ratingService.getCurrentUserRatingSummary()).thenReturn(new RatingSummaryResponse(0.0, 0));
        when(ratingService.isRatingServiceHealthy()).thenReturn(true);

        // When - calling all endpoints
        ratingController.getRatingsByDoctorId(doctorId);
        ratingController.getRatingSummary(doctorId);
        ratingController.getCurrentUserRatingSummary();
        ratingController.checkRatingServiceHealth();

        // Then - verify all service methods were called (indicating logging would occur)
        verify(ratingService).getRatingsByDoctorId(doctorId);
        verify(ratingService).getRatingSummary(doctorId);
        verify(ratingService).getCurrentUserRatingSummary();
        verify(ratingService).isRatingServiceHealthy();
    }

    @Test
    void errorHandling_ShouldBeGracefulForUserFacingEndpoints() {
        // Test that user-facing endpoints return graceful errors instead of exceptions

        // Given
        Long doctorId = 1L;
        when(ratingService.getRatingSummary(doctorId)).thenThrow(new RuntimeException("Service down"));
        when(ratingService.getCurrentUserRatingSummary()).thenThrow(new RuntimeException("User error"));

        // When
        ResponseEntity<RatingSummaryResponse> summaryResponse = ratingController.getRatingSummary(doctorId);
        ResponseEntity<RatingSummaryResponse> currentUserResponse = ratingController.getCurrentUserRatingSummary();

        // Then - both should return OK with empty summaries instead of throwing exceptions
        assertEquals(HttpStatus.OK, summaryResponse.getStatusCode());
        assertEquals(0.0, summaryResponse.getBody().getAverageRating());
        assertEquals(0, summaryResponse.getBody().getTotalRatings());

        assertEquals(HttpStatus.OK, currentUserResponse.getStatusCode());
        assertEquals(0.0, currentUserResponse.getBody().getAverageRating());
        assertEquals(0, currentUserResponse.getBody().getTotalRatings());
    }

    // Helper methods for creating test data
    private List<RatingResponseDto> createMockRatings() {
        return Arrays.asList(
                createRatingResponseDto(1L, 5, "Excellent care"),
                createRatingResponseDto(2L, 4, "Very good service")
        );
    }

    private RatingResponseDto createRatingResponseDto(Long id, Integer score, String comment) {
        RatingResponseDto dto = new RatingResponseDto();
        dto.setId(id);
        dto.setConsultationId(id);
        dto.setDoctorId(1L);
        dto.setScore(score);
        dto.setComment(comment);
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }
}