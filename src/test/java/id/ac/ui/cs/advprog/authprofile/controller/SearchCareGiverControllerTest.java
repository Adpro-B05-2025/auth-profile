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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SearchCareGiverControllerTest {

    @Mock
    private SearchCareGiverService searchCareGiverService;

    @InjectMocks
    private SearchCareGiverController searchCareGiverController;

    private MockMvc mockMvc;
    private List<ProfileResponse> mockProfileResponses;
    private List<String> mockNameSuggestions;
    private List<String> mockSpecialitySuggestions;
    private Page<ProfileResponse> mockPagedResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(searchCareGiverController).build();

        // Setup mock data
        ProfileResponse profile1 = createMockProfileResponse(1L, "Dr. John Smith", "Cardiology");
        ProfileResponse profile2 = createMockProfileResponse(2L, "Dr. Jane Doe", "Pediatrics");
        ProfileResponse profile3 = createMockProfileResponse(3L, "Dr. Robert Johnson", "Cardiology");

        mockProfileResponses = Arrays.asList(profile1, profile2, profile3);
        mockNameSuggestions = Arrays.asList("Dr. John Smith", "Dr. Jane Doe", "Dr. Robert Johnson");
        mockSpecialitySuggestions = Arrays.asList("Cardiology", "Pediatrics", "Neurology");

        mockPagedResponse = new PageImpl<>(
                Arrays.asList(profile1, profile2),
                PageRequest.of(0, 2),
                3
        );
    }

    private ProfileResponse createMockProfileResponse(Long id, String name, String speciality) {
        ProfileResponse profile = new ProfileResponse();
        profile.setId(id);
        profile.setName(name);
        profile.setEmail(name.toLowerCase().replace(" ", ".") + "@example.com");
        profile.setSpeciality(speciality);
        profile.setUserType("CAREGIVER");
        profile.setAverageRating(4.5);
        profile.setPhoneNumber("08123456789" + id);
        profile.setWorkAddress("Medical Center " + id);
        return profile;
    }

    // Tests for searchCareGiversOptimized endpoint
    @Test
    void searchCareGiversOptimized_WithBothParameters_ShouldReturnFilteredResults() {
        // Arrange
        String name = "John";
        String speciality = "Cardiology";
        List<ProfileResponse> expectedResults = Arrays.asList(mockProfileResponses.get(0));

        when(searchCareGiverService.searchCareGiversOptimized(name, speciality))
                .thenReturn(expectedResults);

        // Act
        ResponseEntity<List<ProfileResponse>> response = searchCareGiverController
                .searchCareGiversOptimized(name, speciality);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Dr. John Smith", response.getBody().get(0).getName());
        assertEquals("Cardiology", response.getBody().get(0).getSpeciality());

        verify(searchCareGiverService).searchCareGiversOptimized(name, speciality);
    }

    @Test
    void searchCareGiversOptimized_WithOnlyNameParameter_ShouldReturnResults() {
        // Arrange
        String name = "Jane";
        List<ProfileResponse> expectedResults = Arrays.asList(mockProfileResponses.get(1));

        when(searchCareGiverService.searchCareGiversOptimized(name, null))
                .thenReturn(expectedResults);

        // Act
        ResponseEntity<List<ProfileResponse>> response = searchCareGiverController
                .searchCareGiversOptimized(name, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Dr. Jane Doe", response.getBody().get(0).getName());

        verify(searchCareGiverService).searchCareGiversOptimized(name, null);
    }

    @Test
    void searchCareGiversOptimized_WithOnlySpecialityParameter_ShouldReturnResults() {
        // Arrange
        String speciality = "Cardiology";
        List<ProfileResponse> expectedResults = Arrays.asList(
                mockProfileResponses.get(0), mockProfileResponses.get(2)
        );

        when(searchCareGiverService.searchCareGiversOptimized(null, speciality))
                .thenReturn(expectedResults);

        // Act
        ResponseEntity<List<ProfileResponse>> response = searchCareGiverController
                .searchCareGiversOptimized(null, speciality);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().stream()
                .allMatch(profile -> "Cardiology".equals(profile.getSpeciality())));

        verify(searchCareGiverService).searchCareGiversOptimized(null, speciality);
    }

    @Test
    void searchCareGiversOptimized_WithNoParameters_ShouldReturnAllResults() {
        // Arrange
        when(searchCareGiverService.searchCareGiversOptimized(null, null))
                .thenReturn(mockProfileResponses);

        // Act
        ResponseEntity<List<ProfileResponse>> response = searchCareGiverController
                .searchCareGiversOptimized(null, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());

        verify(searchCareGiverService).searchCareGiversOptimized(null, null);
    }

    @Test
    void searchCareGiversOptimized_WithEmptyResults_ShouldReturnEmptyList() {
        // Arrange
        String name = "NonExistent";
        String speciality = "Unknown";

        when(searchCareGiverService.searchCareGiversOptimized(name, speciality))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<ProfileResponse>> response = searchCareGiverController
                .searchCareGiversOptimized(name, speciality);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(searchCareGiverService).searchCareGiversOptimized(name, speciality);
    }

    // Tests for searchCareGiversOptimized endpoint using MockMvc
    @Test
    void searchCareGiversOptimized_MockMvc_WithParameters_ShouldReturnOk() throws Exception {
        // Arrange
        when(searchCareGiverService.searchCareGiversOptimized("John", "Cardiology"))
                .thenReturn(Arrays.asList(mockProfileResponses.get(0)));

        // Act & Assert
        mockMvc.perform(get("/api/caregiver/search-optimized")
                        .param("name", "John")
                        .param("speciality", "Cardiology"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Dr. John Smith"))
                .andExpect(jsonPath("$[0].speciality").value("Cardiology"));

        verify(searchCareGiverService).searchCareGiversOptimized("John", "Cardiology");
    }

    @Test
    void searchCareGiversOptimized_MockMvc_WithoutParameters_ShouldReturnOk() throws Exception {
        // Arrange
        when(searchCareGiverService.searchCareGiversOptimized(null, null))
                .thenReturn(mockProfileResponses);

        // Act & Assert
        mockMvc.perform(get("/api/caregiver/search-optimized"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));

        verify(searchCareGiverService).searchCareGiversOptimized(null, null);
    }

    // Tests for getNameSuggestions endpoint
    @Test
    void getNameSuggestions_WithValidPrefix_ShouldReturnSuggestions() {
        // Arrange
        String prefix = "Dr";

        when(searchCareGiverService.getNameSuggestions(prefix))
                .thenReturn(mockNameSuggestions);

        // Act
        ResponseEntity<List<String>> response = searchCareGiverController
                .getNameSuggestions(prefix);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        assertTrue(response.getBody().contains("Dr. John Smith"));
        assertTrue(response.getBody().contains("Dr. Jane Doe"));
        assertTrue(response.getBody().contains("Dr. Robert Johnson"));

        verify(searchCareGiverService).getNameSuggestions(prefix);
    }

    @Test
    void getNameSuggestions_WithEmptyPrefix_ShouldReturnEmptyList() {
        // Arrange
        String prefix = "";

        when(searchCareGiverService.getNameSuggestions(prefix))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<String>> response = searchCareGiverController
                .getNameSuggestions(prefix);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(searchCareGiverService).getNameSuggestions(prefix);
    }

    @Test
    void getNameSuggestions_MockMvc_ShouldReturnOk() throws Exception {
        // Arrange
        when(searchCareGiverService.getNameSuggestions("Dr"))
                .thenReturn(mockNameSuggestions);

        // Act & Assert
        mockMvc.perform(get("/api/caregiver/suggestions/names")
                        .param("prefix", "Dr"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("Dr. John Smith"));

        verify(searchCareGiverService).getNameSuggestions("Dr");
    }

    @Test
    void getNameSuggestions_MockMvc_WithoutPrefix_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/caregiver/suggestions/names"))
                .andExpect(status().isBadRequest());

        verify(searchCareGiverService, never()).getNameSuggestions(anyString());
    }

    // Tests for getSpecialitySuggestions endpoint
    @Test
    void getSpecialitySuggestions_WithValidQuery_ShouldReturnSuggestions() {
        // Arrange
        String query = "Card";
        List<String> expectedSuggestions = Arrays.asList("Cardiology", "Cardiac Surgery");

        when(searchCareGiverService.getSpecialitySuggestions(query))
                .thenReturn(expectedSuggestions);

        // Act
        ResponseEntity<List<String>> response = searchCareGiverController
                .getSpecialitySuggestions(query);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().contains("Cardiology"));
        assertTrue(response.getBody().contains("Cardiac Surgery"));

        verify(searchCareGiverService).getSpecialitySuggestions(query);
    }

    @Test
    void getSpecialitySuggestions_WithEmptyQuery_ShouldReturnEmptyList() {
        // Arrange
        String query = "";

        when(searchCareGiverService.getSpecialitySuggestions(query))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<String>> response = searchCareGiverController
                .getSpecialitySuggestions(query);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(searchCareGiverService).getSpecialitySuggestions(query);
    }

    @Test
    void getSpecialitySuggestions_MockMvc_ShouldReturnOk() throws Exception {
        // Arrange
        when(searchCareGiverService.getSpecialitySuggestions("Card"))
                .thenReturn(Arrays.asList("Cardiology", "Cardiac Surgery"));

        // Act & Assert
        mockMvc.perform(get("/api/caregiver/suggestions/specialities")
                        .param("query", "Card"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("Cardiology"));

        verify(searchCareGiverService).getSpecialitySuggestions("Card");
    }

    @Test
    void getSpecialitySuggestions_MockMvc_WithoutQuery_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/caregiver/suggestions/specialities"))
                .andExpect(status().isBadRequest());

        verify(searchCareGiverService, never()).getSpecialitySuggestions(anyString());
    }

    // Tests for searchCareGiversPaginated endpoint
    @Test
    void searchCareGiversPaginated_WithAllParameters_ShouldReturnPagedResults() {
        // Arrange
        String name = "Dr";
        String speciality = "Cardiology";
        int page = 0;
        int size = 2;

        when(searchCareGiverService.searchCareGiversPaginated(name, speciality, page, size))
                .thenReturn(mockPagedResponse);

        // Act
        ResponseEntity<Page<ProfileResponse>> response = searchCareGiverController
                .searchCareGiversPaginated(name, speciality, page, size);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals(3, response.getBody().getTotalElements());
        assertEquals(0, response.getBody().getNumber());
        assertEquals(2, response.getBody().getSize());

        verify(searchCareGiverService).searchCareGiversPaginated(name, speciality, page, size);
    }

    @Test
    void searchCareGiversPaginated_WithDefaultParameters_ShouldUseDefaults() {
        // Arrange
        when(searchCareGiverService.searchCareGiversPaginated(null, null, 0, 10))
                .thenReturn(mockPagedResponse);

        // Act
        ResponseEntity<Page<ProfileResponse>> response = searchCareGiverController
                .searchCareGiversPaginated(null, null, 0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(searchCareGiverService).searchCareGiversPaginated(null, null, 0, 10);
    }

    @Test
    void searchCareGiversPaginated_WithOnlySearchParameters_ShouldUseDefaultPaging() {
        // Arrange
        String name = "John";
        String speciality = "Cardiology";

        when(searchCareGiverService.searchCareGiversPaginated(name, speciality, 0, 10))
                .thenReturn(mockPagedResponse);

        // Act
        ResponseEntity<Page<ProfileResponse>> response = searchCareGiverController
                .searchCareGiversPaginated(name, speciality, 0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(searchCareGiverService).searchCareGiversPaginated(name, speciality, 0, 10);
    }

    @Test
    void searchCareGiversPaginated_MockMvc_WithAllParameters_ShouldReturnOk() throws Exception {
        // Arrange
        when(searchCareGiverService.searchCareGiversPaginated("Dr", "Cardiology", 0, 2))
                .thenReturn(mockPagedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/caregiver/search-paginated")
                        .param("name", "Dr")
                        .param("speciality", "Cardiology")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(2));

        verify(searchCareGiverService).searchCareGiversPaginated("Dr", "Cardiology", 0, 2);
    }

    @Test
    void searchCareGiversPaginated_MockMvc_WithDefaultParameters_ShouldReturnOk() throws Exception {
        // Arrange
        when(searchCareGiverService.searchCareGiversPaginated(null, null, 0, 10))
                .thenReturn(mockPagedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/caregiver/search-paginated"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());

        verify(searchCareGiverService).searchCareGiversPaginated(null, null, 0, 10);
    }

    // NEW TESTS FOR ENHANCED ENDPOINTS

    // Tests for searchCareGiversAdvanced endpoint
    @Test
    void searchCareGiversAdvanced_WithAllParameters_ShouldReturnPagedResults() {
        // Arrange
        String name = "Dr";
        String speciality = "Cardiology";
        int page = 0;
        int size = 10;
        String sortBy = "averageRating";
        String sortDirection = "desc";

        when(searchCareGiverService.searchCareGiversPaginatedWithSort(name, speciality, page, size, sortBy, sortDirection))
                .thenReturn(mockPagedResponse);

        // Act
        ResponseEntity<Page<ProfileResponse>> response = searchCareGiverController
                .searchCareGiversAdvanced(name, speciality, page, size, sortBy, sortDirection);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());

        verify(searchCareGiverService).searchCareGiversPaginatedWithSort(name, speciality, page, size, sortBy, sortDirection);
    }

    @Test
    void searchCareGiversAdvanced_MockMvc_WithCustomSorting_ShouldReturnOk() throws Exception {
        // Arrange
        when(searchCareGiverService.searchCareGiversPaginatedWithSort("John", "Cardiology", 0, 5, "name", "asc"))
                .thenReturn(mockPagedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/caregiver/search-advanced")
                        .param("name", "John")
                        .param("speciality", "Cardiology")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "name")
                        .param("sortDirection", "asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));

        verify(searchCareGiverService).searchCareGiversPaginatedWithSort("John", "Cardiology", 0, 5, "name", "asc");
    }

    @Test
    void searchCareGiversAdvanced_MockMvc_WithDefaultParameters_ShouldReturnOk() throws Exception {
        // Arrange
        when(searchCareGiverService.searchCareGiversPaginatedWithSort(null, null, 0, 10, "averageRating", "desc"))
                .thenReturn(mockPagedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/caregiver/search-advanced"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());

        verify(searchCareGiverService).searchCareGiversPaginatedWithSort(null, null, 0, 10, "averageRating", "desc");
    }

    // Tests for getTopRatedCareGivers endpoint
    @Test
    void getTopRatedCareGivers_WithDefaultParameters_ShouldReturnPagedResults() {
        // Arrange
        int page = 0;
        int size = 10;

        when(searchCareGiverService.getTopRatedCareGivers(page, size))
                .thenReturn(mockPagedResponse);

        // Act
        ResponseEntity<Page<ProfileResponse>> response = searchCareGiverController
                .getTopRatedCareGivers(page, size);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());

        verify(searchCareGiverService).getTopRatedCareGivers(page, size);
    }

    @Test
    void getTopRatedCareGivers_MockMvc_ShouldReturnOk() throws Exception {
        // Arrange
        when(searchCareGiverService.getTopRatedCareGivers(0, 10))
                .thenReturn(mockPagedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/caregiver/top-rated"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3));

        verify(searchCareGiverService).getTopRatedCareGivers(0, 10);
    }

    @Test
    void getTopRatedCareGivers_MockMvc_WithCustomPagination_ShouldReturnOk() throws Exception {
        // Arrange
        when(searchCareGiverService.getTopRatedCareGivers(1, 5))
                .thenReturn(mockPagedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/caregiver/top-rated")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());

        verify(searchCareGiverService).getTopRatedCareGivers(1, 5);
    }

    // Integration-style tests for error handling
    @Test
    void searchCareGiversOptimized_WhenServiceThrowsException_ShouldPropagateException() {
        // Arrange
        String name = "Test";
        String speciality = "Test";

        when(searchCareGiverService.searchCareGiversOptimized(name, speciality))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            searchCareGiverController.searchCareGiversOptimized(name, speciality);
        });

        verify(searchCareGiverService).searchCareGiversOptimized(name, speciality);
    }

    @Test
    void getNameSuggestions_WhenServiceThrowsException_ShouldPropagateException() {
        // Arrange
        String prefix = "Test";

        when(searchCareGiverService.getNameSuggestions(prefix))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            searchCareGiverController.getNameSuggestions(prefix);
        });

        verify(searchCareGiverService).getNameSuggestions(prefix);
    }

    @Test
    void getSpecialitySuggestions_WhenServiceThrowsException_ShouldPropagateException() {
        // Arrange
        String query = "Test";

        when(searchCareGiverService.getSpecialitySuggestions(query))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            searchCareGiverController.getSpecialitySuggestions(query);
        });

        verify(searchCareGiverService).getSpecialitySuggestions(query);
    }

    @Test
    void searchCareGiversPaginated_WhenServiceThrowsException_ShouldPropagateException() {
        // Arrange
        when(searchCareGiverService.searchCareGiversPaginated(null, null, 0, 10))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            searchCareGiverController.searchCareGiversPaginated(null, null, 0, 10);
        });

        verify(searchCareGiverService).searchCareGiversPaginated(null, null, 0, 10);
    }

    @Test
    void searchCareGiversAdvanced_WhenServiceThrowsException_ShouldPropagateException() {
        // Arrange
        when(searchCareGiverService.searchCareGiversPaginatedWithSort(null, null, 0, 10, "averageRating", "desc"))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            searchCareGiverController.searchCareGiversAdvanced(null, null, 0, 10, "averageRating", "desc");
        });

        verify(searchCareGiverService).searchCareGiversPaginatedWithSort(null, null, 0, 10, "averageRating", "desc");
    }

    @Test
    void getTopRatedCareGivers_WhenServiceThrowsException_ShouldPropagateException() {
        // Arrange
        when(searchCareGiverService.getTopRatedCareGivers(0, 10))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            searchCareGiverController.getTopRatedCareGivers(0, 10);
        });

        verify(searchCareGiverService).getTopRatedCareGivers(0, 10);
    }


    // Additional edge case tests
    @Test
    void searchCareGiversAdvanced_WithInvalidSortParameters_ShouldStillCallService() {
        // Arrange
        String sortBy = "invalidField";
        String sortDirection = "invalidDirection";

        when(searchCareGiverService.searchCareGiversPaginatedWithSort(null, null, 0, 10, sortBy, sortDirection))
                .thenReturn(mockPagedResponse);

        // Act
        ResponseEntity<Page<ProfileResponse>> response = searchCareGiverController
                .searchCareGiversAdvanced(null, null, 0, 10, sortBy, sortDirection);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(searchCareGiverService).searchCareGiversPaginatedWithSort(null, null, 0, 10, sortBy, sortDirection);
    }

    @Test
    void getTopRatedCareGivers_WithLargePageSize_ShouldCallService() {
        // Arrange
        int page = 0;
        int size = 100; // Large page size

        when(searchCareGiverService.getTopRatedCareGivers(page, size))
                .thenReturn(mockPagedResponse);

        // Act
        ResponseEntity<Page<ProfileResponse>> response = searchCareGiverController
                .getTopRatedCareGivers(page, size);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(searchCareGiverService).getTopRatedCareGivers(page, size);
    }



    // MockMvc tests for enhanced endpoints with edge cases
    @Test
    void searchCareGiversAdvanced_MockMvc_WithNegativePageSize_ShouldReturnOk() throws Exception {
        // Arrange - Service should handle validation
        when(searchCareGiverService.searchCareGiversPaginatedWithSort(null, null, -1, -5, "averageRating", "desc"))
                .thenReturn(mockPagedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/caregiver/search-advanced")
                        .param("page", "-1")
                        .param("size", "-5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(searchCareGiverService).searchCareGiversPaginatedWithSort(null, null, -1, -5, "averageRating", "desc");
    }

    @Test
    void getTopRatedCareGivers_MockMvc_WithZeroSize_ShouldReturnOk() throws Exception {
        // Arrange
        when(searchCareGiverService.getTopRatedCareGivers(0, 0))
                .thenReturn(mockPagedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/caregiver/top-rated")
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(searchCareGiverService).getTopRatedCareGivers(0, 0);
    }

    // Test for concurrent requests (simulation)
    @Test
    void multipleEndpoints_CalledConcurrently_ShouldNotInterfere() {
        // Arrange
        when(searchCareGiverService.searchCareGiversOptimized(anyString(), anyString()))
                .thenReturn(mockProfileResponses);
        when(searchCareGiverService.getNameSuggestions(anyString()))
                .thenReturn(mockNameSuggestions);
        when(searchCareGiverService.searchCareGiversPaginated(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(mockPagedResponse);

        // Act - Simulate multiple calls
        ResponseEntity<List<ProfileResponse>> optimizedResponse =
                searchCareGiverController.searchCareGiversOptimized("test", "test");
        ResponseEntity<List<String>> suggestionsResponse =
                searchCareGiverController.getNameSuggestions("test");
        ResponseEntity<Page<ProfileResponse>> paginatedResponse =
                searchCareGiverController.searchCareGiversPaginated("test", "test", 0, 10);

        // Assert
        assertEquals(HttpStatus.OK, optimizedResponse.getStatusCode());
        assertEquals(HttpStatus.OK, suggestionsResponse.getStatusCode());
        assertEquals(HttpStatus.OK, paginatedResponse.getStatusCode());

        // Verify all service calls were made
        verify(searchCareGiverService).searchCareGiversOptimized("test", "test");
        verify(searchCareGiverService).getNameSuggestions("test");
        verify(searchCareGiverService).searchCareGiversPaginated("test", "test", 0, 10);
    }
}