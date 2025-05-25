package id.ac.ui.cs.advprog.authprofile.controller;

import id.ac.ui.cs.advprog.authprofile.dto.response.ProfileResponse;
import id.ac.ui.cs.advprog.authprofile.service.SearchCareGiverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchCareGiverControllerTest {

    @Mock
    private SearchCareGiverService searchCareGiverService;

    @InjectMocks
    private SearchCareGiverController controller;

    private ProfileResponse profileResponse1;
    private ProfileResponse profileResponse2;
    private List<ProfileResponse> profileResponses;
    private Page<ProfileResponse> profilePage;

    @BeforeEach
    void setUp() {
        profileResponse1 = new ProfileResponse();
        profileResponse1.setId(1L);
        profileResponse1.setName("Dr. John Smith");
        profileResponse1.setSpeciality("Cardiology");
        profileResponse1.setAverageRating(4.5);

        profileResponse2 = new ProfileResponse();
        profileResponse2.setId(2L);
        profileResponse2.setName("Dr. Jane Johnson");
        profileResponse2.setSpeciality("Pediatrics");
        profileResponse2.setAverageRating(4.8);

        profileResponses = Arrays.asList(profileResponse1, profileResponse2);
        profilePage = new PageImpl<>(profileResponses, PageRequest.of(0, 10), 2);
    }

    // ========== ASYNC SEARCH TESTS ==========

    @Test
    void testSearchCareGiversOptimized_Success() throws ExecutionException, InterruptedException {
        // Given
        when(searchCareGiverService.searchCareGiversOptimized("John", "Cardiology"))
                .thenReturn(CompletableFuture.completedFuture(List.of(profileResponse1)));

        // When
        CompletableFuture<ResponseEntity<List<ProfileResponse>>> result =
                controller.searchCareGiversOptimized("John", "Cardiology");

        // Then
        ResponseEntity<List<ProfileResponse>> response = result.get();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Dr. John Smith", response.getBody().get(0).getName());

        verify(searchCareGiverService).searchCareGiversOptimized("John", "Cardiology");
    }

    @Test
    void testSearchCareGiversOptimized_WithNullParameters() throws ExecutionException, InterruptedException {
        // Given
        when(searchCareGiverService.searchCareGiversOptimized(null, null))
                .thenReturn(CompletableFuture.completedFuture(profileResponses));

        // When
        CompletableFuture<ResponseEntity<List<ProfileResponse>>> result =
                controller.searchCareGiversOptimized(null, null);

        // Then
        ResponseEntity<List<ProfileResponse>> response = result.get();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());

        verify(searchCareGiverService).searchCareGiversOptimized(null, null);
    }

    @Test
    void testSearchCareGiversOptimized_ServiceException() throws ExecutionException, InterruptedException {
        // Given
        CompletableFuture<List<ProfileResponse>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Database error"));
        when(searchCareGiverService.searchCareGiversOptimized(anyString(), anyString()))
                .thenReturn(failedFuture);

        // When
        CompletableFuture<ResponseEntity<List<ProfileResponse>>> result =
                controller.searchCareGiversOptimized("John", "Cardiology");

        // Then
        ResponseEntity<List<ProfileResponse>> response = result.get();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testSearchCareGiversOptimized_EmptyResults() throws ExecutionException, InterruptedException {
        // Given
        when(searchCareGiverService.searchCareGiversOptimized("NonExistent", "InvalidSpeciality"))
                .thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

        // When
        CompletableFuture<ResponseEntity<List<ProfileResponse>>> result =
                controller.searchCareGiversOptimized("NonExistent", "InvalidSpeciality");

        // Then
        ResponseEntity<List<ProfileResponse>> response = result.get();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
    }

    // ========== SYNC AUTOCOMPLETE TESTS ==========

    @Test
    void testGetNameSuggestions_Success() {
        // Given
        List<String> suggestions = Arrays.asList("Dr. John", "Dr. Jane");
        when(searchCareGiverService.getNameSuggestions("Dr")).thenReturn(suggestions);

        // When
        ResponseEntity<List<String>> result = controller.getNameSuggestions("Dr");

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().size());
        assertEquals("Dr. John", result.getBody().get(0));

        verify(searchCareGiverService).getNameSuggestions("Dr");
    }

    @Test
    void testGetNameSuggestions_ShortPrefix() {
        // When
        ResponseEntity<List<String>> result = controller.getNameSuggestions("D");

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNull(result.getBody());

        verify(searchCareGiverService, never()).getNameSuggestions(anyString());
    }

    @Test
    void testGetNameSuggestions_NullPrefix() {
        // When
        ResponseEntity<List<String>> result = controller.getNameSuggestions(null);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNull(result.getBody());

        verify(searchCareGiverService, never()).getNameSuggestions(anyString());
    }

    @Test
    void testGetNameSuggestions_WhitespacePrefix() {
        // When
        ResponseEntity<List<String>> result = controller.getNameSuggestions("  ");

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNull(result.getBody());

        verify(searchCareGiverService, never()).getNameSuggestions(anyString());
    }

    @Test
    void testGetNameSuggestions_ServiceException() {
        // Given
        when(searchCareGiverService.getNameSuggestions("Dr"))
                .thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<List<String>> result = controller.getNameSuggestions("Dr");

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void testGetSpecialitySuggestions_Success() {
        // Given
        List<String> suggestions = Arrays.asList("Cardiology", "Pediatrics");
        when(searchCareGiverService.getSpecialitySuggestions("Card")).thenReturn(suggestions);

        // When
        ResponseEntity<List<String>> result = controller.getSpecialitySuggestions("Card");

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().size());
        assertEquals("Cardiology", result.getBody().get(0));

        verify(searchCareGiverService).getSpecialitySuggestions("Card");
    }

    @Test
    void testGetSpecialitySuggestions_ShortQuery() {
        // When
        ResponseEntity<List<String>> result = controller.getSpecialitySuggestions("C");

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNull(result.getBody());

        verify(searchCareGiverService, never()).getSpecialitySuggestions(anyString());
    }

    @Test
    void testGetSpecialitySuggestions_NullQuery() {
        // When
        ResponseEntity<List<String>> result = controller.getSpecialitySuggestions(null);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNull(result.getBody());

        verify(searchCareGiverService, never()).getSpecialitySuggestions(anyString());
    }

    @Test
    void testGetSpecialitySuggestions_WhitespaceQuery() {
        // When
        ResponseEntity<List<String>> result = controller.getSpecialitySuggestions("   ");

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNull(result.getBody());

        verify(searchCareGiverService, never()).getSpecialitySuggestions(anyString());
    }

    @Test
    void testGetSpecialitySuggestions_ServiceException() {
        // Given
        when(searchCareGiverService.getSpecialitySuggestions("Card"))
                .thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<List<String>> result = controller.getSpecialitySuggestions("Card");

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNull(result.getBody());
    }

    // ========== ASYNC PAGINATED SEARCH TESTS ==========

    @Test
    void testSearchCareGiversPaginated_Success() throws ExecutionException, InterruptedException {
        // Given
        when(searchCareGiverService.searchCareGiversPaginated("John", "Cardiology", 0, 10))
                .thenReturn(CompletableFuture.completedFuture(profilePage));

        // When
        CompletableFuture<ResponseEntity<Page<ProfileResponse>>> result =
                controller.searchCareGiversPaginated("John", "Cardiology", 0, 10);

        // Then
        ResponseEntity<Page<ProfileResponse>> response = result.get();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getTotalElements());
        assertEquals(2, response.getBody().getContent().size());

        verify(searchCareGiverService).searchCareGiversPaginated("John", "Cardiology", 0, 10);
    }

    @Test
    void testSearchCareGiversPaginated_WithDefaultParameters() throws ExecutionException, InterruptedException {
        // Given
        when(searchCareGiverService.searchCareGiversPaginated(null, null, 0, 10))
                .thenReturn(CompletableFuture.completedFuture(profilePage));

        // When
        CompletableFuture<ResponseEntity<Page<ProfileResponse>>> result =
                controller.searchCareGiversPaginated(null, null, 0, 10);

        // Then
        ResponseEntity<Page<ProfileResponse>> response = result.get();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getTotalElements());

        verify(searchCareGiverService).searchCareGiversPaginated(null, null, 0, 10);
    }

    @Test
    void testSearchCareGiversPaginated_ServiceException() throws ExecutionException, InterruptedException {
        // Given
        CompletableFuture<Page<ProfileResponse>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Database error"));
        when(searchCareGiverService.searchCareGiversPaginated(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(failedFuture);

        // When
        CompletableFuture<ResponseEntity<Page<ProfileResponse>>> result =
                controller.searchCareGiversPaginated("John", "Cardiology", 0, 10);

        // Then
        ResponseEntity<Page<ProfileResponse>> response = result.get();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ========== ASYNC ADVANCED SEARCH TESTS ==========

    @Test
    void testSearchCareGiversAdvanced_Success() throws ExecutionException, InterruptedException {
        // Given
        when(searchCareGiverService.searchCareGiversPaginatedWithSort(
                "John", "Cardiology", 0, 10, "averageRating", "desc"))
                .thenReturn(CompletableFuture.completedFuture(profilePage));

        // When
        CompletableFuture<ResponseEntity<Page<ProfileResponse>>> result =
                controller.searchCareGiversAdvanced("John", "Cardiology", 0, 10, "averageRating", "desc");

        // Then
        ResponseEntity<Page<ProfileResponse>> response = result.get();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getTotalElements());

        verify(searchCareGiverService).searchCareGiversPaginatedWithSort(
                "John", "Cardiology", 0, 10, "averageRating", "desc");
    }

    @Test
    void testSearchCareGiversAdvanced_WithAllValidSortFields() throws ExecutionException, InterruptedException {
        // Test all valid sort fields
        String[] validSortFields = {"name", "speciality", "averageRating", "ratingCount"};

        for (String sortField : validSortFields) {
            // Given
            when(searchCareGiverService.searchCareGiversPaginatedWithSort(
                    anyString(), anyString(), anyInt(), anyInt(), eq(sortField), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(profilePage));

            // When
            CompletableFuture<ResponseEntity<Page<ProfileResponse>>> result =
                    controller.searchCareGiversAdvanced("John", "Cardiology", 0, 10, sortField, "asc");

            // Then
            ResponseEntity<Page<ProfileResponse>> response = result.get();
            assertEquals(HttpStatus.OK, response.getStatusCode());

            // Reset mock for next iteration
            reset(searchCareGiverService);
        }
    }

    @Test
    void testSearchCareGiversAdvanced_WithValidSortDirections() throws ExecutionException, InterruptedException {
        // Test both valid sort directions
        String[] validDirections = {"asc", "desc"};

        for (String direction : validDirections) {
            // Given
            when(searchCareGiverService.searchCareGiversPaginatedWithSort(
                    anyString(), anyString(), anyInt(), anyInt(), anyString(), eq(direction)))
                    .thenReturn(CompletableFuture.completedFuture(profilePage));

            // When
            CompletableFuture<ResponseEntity<Page<ProfileResponse>>> result =
                    controller.searchCareGiversAdvanced("John", "Cardiology", 0, 10, "name", direction);

            // Then
            ResponseEntity<Page<ProfileResponse>> response = result.get();
            assertEquals(HttpStatus.OK, response.getStatusCode());

            // Reset mock for next iteration
            reset(searchCareGiverService);
        }
    }

    @Test
    void testSearchCareGiversAdvanced_InvalidSortField() throws ExecutionException, InterruptedException {
        // When
        CompletableFuture<ResponseEntity<Page<ProfileResponse>>> result =
                controller.searchCareGiversAdvanced("John", "Cardiology", 0, 10, "invalidField", "desc");

        // Then
        ResponseEntity<Page<ProfileResponse>> response = result.get();
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        verify(searchCareGiverService, never()).searchCareGiversPaginatedWithSort(
                anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    void testSearchCareGiversAdvanced_InvalidSortDirection() throws ExecutionException, InterruptedException {
        // When
        CompletableFuture<ResponseEntity<Page<ProfileResponse>>> result =
                controller.searchCareGiversAdvanced("John", "Cardiology", 0, 10, "name", "invalid");

        // Then
        ResponseEntity<Page<ProfileResponse>> response = result.get();
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        verify(searchCareGiverService, never()).searchCareGiversPaginatedWithSort(
                anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    void testSearchCareGiversAdvanced_NullSortField() throws ExecutionException, InterruptedException {
        // When
        CompletableFuture<ResponseEntity<Page<ProfileResponse>>> result =
                controller.searchCareGiversAdvanced("John", "Cardiology", 0, 10, null, "desc");

        // Then
        ResponseEntity<Page<ProfileResponse>> response = result.get();
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testSearchCareGiversAdvanced_NullSortDirection() throws ExecutionException, InterruptedException {
        // When
        CompletableFuture<ResponseEntity<Page<ProfileResponse>>> result =
                controller.searchCareGiversAdvanced("John", "Cardiology", 0, 10, "name", null);

        // Then
        ResponseEntity<Page<ProfileResponse>> response = result.get();
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testSearchCareGiversAdvanced_ServiceException() throws ExecutionException, InterruptedException {
        // Given
        CompletableFuture<Page<ProfileResponse>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Database error"));
        when(searchCareGiverService.searchCareGiversPaginatedWithSort(
                anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(failedFuture);

        // When
        CompletableFuture<ResponseEntity<Page<ProfileResponse>>> result =
                controller.searchCareGiversAdvanced("John", "Cardiology", 0, 10, "name", "asc");

        // Then
        ResponseEntity<Page<ProfileResponse>> response = result.get();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ========== ASYNC TOP-RATED TESTS ==========

    @Test
    void testGetTopRatedCareGivers_Success() throws ExecutionException, InterruptedException {
        // Given
        when(searchCareGiverService.getTopRatedCareGivers(0, 10))
                .thenReturn(CompletableFuture.completedFuture(profilePage));

        // When
        CompletableFuture<ResponseEntity<Page<ProfileResponse>>> result =
                controller.getTopRatedCareGivers(0, 10);

        // Then
        ResponseEntity<Page<ProfileResponse>> response = result.get();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getTotalElements());

        verify(searchCareGiverService).getTopRatedCareGivers(0, 10);
    }

    @Test
    void testGetTopRatedCareGivers_DefaultParameters() throws ExecutionException, InterruptedException {
        // Given - testing with default parameter values
        when(searchCareGiverService.getTopRatedCareGivers(0, 10))
                .thenReturn(CompletableFuture.completedFuture(profilePage));

        // When - call without explicit parameters (using defaults)
        CompletableFuture<ResponseEntity<Page<ProfileResponse>>> result =
                controller.getTopRatedCareGivers(0, 10);

        // Then
        ResponseEntity<Page<ProfileResponse>> response = result.get();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getTotalElements());

        verify(searchCareGiverService).getTopRatedCareGivers(0, 10);
    }

    @Test
    void testGetTopRatedCareGivers_ServiceException() throws ExecutionException, InterruptedException {
        // Given
        CompletableFuture<Page<ProfileResponse>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Database error"));
        when(searchCareGiverService.getTopRatedCareGivers(anyInt(), anyInt()))
                .thenReturn(failedFuture);

        // When
        CompletableFuture<ResponseEntity<Page<ProfileResponse>>> result =
                controller.getTopRatedCareGivers(0, 10);

        // Then
        ResponseEntity<Page<ProfileResponse>> response = result.get();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ========== HELPER METHOD TESTS ==========

    @Test
    void testIsValidSortField_AllValidFields() throws Exception {
        // Given - use reflection to access private method
        java.lang.reflect.Method method = SearchCareGiverController.class
                .getDeclaredMethod("isValidSortField", String.class);
        method.setAccessible(true);

        String[] validFields = {"name", "speciality", "averageRating", "ratingCount"};

        // When & Then
        for (String field : validFields) {
            Boolean result = (Boolean) method.invoke(controller, field);
            assertTrue(result, "Field " + field + " should be valid");
        }
    }

    @Test
    void testIsValidSortField_InvalidFields() throws Exception {
        // Given - use reflection to access private method
        java.lang.reflect.Method method = SearchCareGiverController.class
                .getDeclaredMethod("isValidSortField", String.class);
        method.setAccessible(true);

        String[] invalidFields = {"invalidField", "email", "id", "", null};

        // When & Then
        for (String field : invalidFields) {
            Boolean result = (Boolean) method.invoke(controller, field);
            assertFalse(result, "Field " + field + " should be invalid");
        }
    }

    @Test
    void testIsValidSortDirection_ValidDirections() throws Exception {
        // Given - use reflection to access private method
        java.lang.reflect.Method method = SearchCareGiverController.class
                .getDeclaredMethod("isValidSortDirection", String.class);
        method.setAccessible(true);

        String[] validDirections = {"asc", "desc", "ASC", "DESC", "Asc", "Desc"};

        // When & Then
        for (String direction : validDirections) {
            Boolean result = (Boolean) method.invoke(controller, direction);
            assertTrue(result, "Direction " + direction + " should be valid");
        }
    }

    @Test
    void testIsValidSortDirection_InvalidDirections() throws Exception {
        // Given - use reflection to access private method
        java.lang.reflect.Method method = SearchCareGiverController.class
                .getDeclaredMethod("isValidSortDirection", String.class);
        method.setAccessible(true);

        String[] invalidDirections = {"ascending", "descending", "up", "down", "", null, "invalid"};

        // When & Then
        for (String direction : invalidDirections) {
            Boolean result = (Boolean) method.invoke(controller, direction);
            assertFalse(result, "Direction " + direction + " should be invalid");
        }
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    void testSearchCareGiversOptimized_WithEmptyStrings() throws ExecutionException, InterruptedException {
        // Given
        when(searchCareGiverService.searchCareGiversOptimized("", ""))
                .thenReturn(CompletableFuture.completedFuture(profileResponses));

        // When
        CompletableFuture<ResponseEntity<List<ProfileResponse>>> result =
                controller.searchCareGiversOptimized("", "");

        // Then
        ResponseEntity<List<ProfileResponse>> response = result.get();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());

        verify(searchCareGiverService).searchCareGiversOptimized("", "");
    }

    @Test
    void testSearchCareGiversPaginated_WithNegativePageAndLargeSize() throws ExecutionException, InterruptedException {
        // Given
        when(searchCareGiverService.searchCareGiversPaginated(null, null, -1, 150))
                .thenReturn(CompletableFuture.completedFuture(profilePage));

        // When
        CompletableFuture<ResponseEntity<Page<ProfileResponse>>> result =
                controller.searchCareGiversPaginated(null, null, -1, 150);

        // Then
        ResponseEntity<Page<ProfileResponse>> response = result.get();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getTotalElements());

        verify(searchCareGiverService).searchCareGiversPaginated(null, null, -1, 150);
    }

    @Test
    void testGetNameSuggestions_ExactlyTwoCharacters() {
        // Given
        List<String> suggestions = Arrays.asList("Dr");
        when(searchCareGiverService.getNameSuggestions("Dr")).thenReturn(suggestions);

        // When
        ResponseEntity<List<String>> result = controller.getNameSuggestions("Dr");

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());

        verify(searchCareGiverService).getNameSuggestions("Dr");
    }

    @Test
    void testGetSpecialitySuggestions_ExactlyTwoCharacters() {
        // Given
        List<String> suggestions = Arrays.asList("Cardiology");
        when(searchCareGiverService.getSpecialitySuggestions("Ca")).thenReturn(suggestions);

        // When
        ResponseEntity<List<String>> result = controller.getSpecialitySuggestions("Ca");

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());

        verify(searchCareGiverService).getSpecialitySuggestions("Ca");
    }
}