package id.ac.ui.cs.advprog.authprofile.service;

import id.ac.ui.cs.advprog.authprofile.dto.response.ProfileResponse;
import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.Role;
import id.ac.ui.cs.advprog.authprofile.repository.CareGiverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchCareGiverServiceTest {

    @Mock
    private CareGiverRepository careGiverRepository;

    @InjectMocks
    private SearchCareGiverService searchCareGiverService;

    private CareGiver careGiver1;
    private CareGiver careGiver2;
    private CareGiver careGiver3;
    private List<CareGiver> mockCareGivers;

    @BeforeEach
    void setUp() {
        // Create test caregivers
        careGiver1 = createTestCareGiver(1L, "Dr. John Smith", "john.smith@example.com",
                "Cardiology", "123 Medical Center", 4.5, 10);

        careGiver2 = createTestCareGiver(2L, "Dr. Jane Doe", "jane.doe@example.com",
                "Pediatrics", "456 Children's Hospital", 4.8, 15);

        careGiver3 = createTestCareGiver(3L, "Dr. Robert Johnson", "robert.johnson@example.com",
                "Cardiology", "789 Heart Clinic", 4.2, 8);

        mockCareGivers = Arrays.asList(careGiver1, careGiver2, careGiver3);
    }

    private CareGiver createTestCareGiver(Long id, String name, String email, String speciality,
                                          String workAddress, Double rating, Integer ratingCount) {
        CareGiver careGiver = new CareGiver();
        careGiver.setId(id);
        careGiver.setName(name);
        careGiver.setEmail(email);
        careGiver.setNik("123456789012345" + id);
        careGiver.setAddress("Home Address " + id);
        careGiver.setPhoneNumber("08123456789" + id);
        careGiver.setSpeciality(speciality);
        careGiver.setWorkAddress(workAddress);
        careGiver.setAverageRating(rating);
        careGiver.setRatingCount(ratingCount);

        // Set roles
        Set<Role> roles = new HashSet<>();
        Role careGiverRole = new Role();
        careGiverRole.setId(2);
        careGiverRole.setName(Role.ERole.ROLE_CAREGIVER);
        roles.add(careGiverRole);
        careGiver.setRoles(roles);

        return careGiver;
    }

    // Tests for searchCareGiversOptimized method
    @Test
    void searchCareGiversOptimized_WithBothNameAndSpeciality_ShouldReturnFilteredResults() {
        // Arrange
        String name = "John";
        String speciality = "Cardiology";
        List<CareGiver> expectedCareGivers = Arrays.asList(careGiver1);

        when(careGiverRepository.findCareGiversWithFilters(name, speciality))
                .thenReturn(expectedCareGivers);

        // Act
        List<ProfileResponse> result = searchCareGiverService.searchCareGiversOptimized(name, speciality);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Dr. John Smith", result.get(0).getName());
        assertEquals("Cardiology", result.get(0).getSpeciality());
        assertNull(result.get(0).getNik()); // Should be hidden for security
        assertNull(result.get(0).getAddress()); // Should be hidden for security

        verify(careGiverRepository).findCareGiversWithFilters(name, speciality);
    }

    @Test
    void searchCareGiversOptimized_WithOnlyName_ShouldReturnFilteredResults() {
        // Arrange
        String name = "Jane";
        String speciality = null;
        List<CareGiver> expectedCareGivers = Arrays.asList(careGiver2);

        when(careGiverRepository.findCareGiversWithFilters(name, null))
                .thenReturn(expectedCareGivers);

        // Act
        List<ProfileResponse> result = searchCareGiverService.searchCareGiversOptimized(name, speciality);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Dr. Jane Doe", result.get(0).getName());
        assertEquals("Pediatrics", result.get(0).getSpeciality());

        verify(careGiverRepository).findCareGiversWithFilters(name, null);
    }

    @Test
    void searchCareGiversOptimized_WithOnlySpeciality_ShouldReturnFilteredResults() {
        // Arrange
        String name = null;
        String speciality = "Cardiology";
        List<CareGiver> expectedCareGivers = Arrays.asList(careGiver1, careGiver3);

        when(careGiverRepository.findCareGiversWithFilters(null, speciality))
                .thenReturn(expectedCareGivers);

        // Act
        List<ProfileResponse> result = searchCareGiverService.searchCareGiversOptimized(name, speciality);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> "Cardiology".equals(r.getSpeciality())));

        verify(careGiverRepository).findCareGiversWithFilters(null, speciality);
    }

    @Test
    void searchCareGiversOptimized_WithEmptyStrings_ShouldPassNullToRepository() {
        // Arrange
        String name = "  ";
        String speciality = "";

        when(careGiverRepository.findCareGiversWithFilters(null, null))
                .thenReturn(mockCareGivers);

        // Act
        List<ProfileResponse> result = searchCareGiverService.searchCareGiversOptimized(name, speciality);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        verify(careGiverRepository).findCareGiversWithFilters(null, null);
    }

    @Test
    void searchCareGiversOptimized_WithNoFilters_ShouldReturnAllResults() {
        // Arrange
        when(careGiverRepository.findCareGiversWithFilters(null, null))
                .thenReturn(mockCareGivers);

        // Act
        List<ProfileResponse> result = searchCareGiverService.searchCareGiversOptimized(null, null);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        verify(careGiverRepository).findCareGiversWithFilters(null, null);
    }

    @Test
    void searchCareGiversOptimized_WithEmptyResultFromRepository_ShouldReturnEmptyList() {
        // Arrange
        String name = "NonExistent";
        String speciality = "Unknown";

        when(careGiverRepository.findCareGiversWithFilters(name, speciality))
                .thenReturn(Collections.emptyList());

        // Act
        List<ProfileResponse> result = searchCareGiverService.searchCareGiversOptimized(name, speciality);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(careGiverRepository).findCareGiversWithFilters(name, speciality);
    }

    // Tests for getAllCareGiversLite method
    @Test
    void getAllCareGiversLite_ShouldReturnAllCareGivers() {
        // Arrange
        when(careGiverRepository.findAll()).thenReturn(mockCareGivers);

        // Act
        List<ProfileResponse> result = searchCareGiverService.getAllCareGiversLite();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        // Verify that sensitive information is hidden
        result.forEach(profile -> {
            assertNull(profile.getNik());
            assertNull(profile.getAddress());
            assertEquals("CAREGIVER", profile.getUserType());
            assertNotNull(profile.getName());
            assertNotNull(profile.getEmail());
            assertNotNull(profile.getSpeciality());
            assertNotNull(profile.getWorkAddress());
            assertNotNull(profile.getAverageRating());
        });

        verify(careGiverRepository).findAll();
    }

    @Test
    void getAllCareGiversLite_WithEmptyRepository_ShouldReturnEmptyList() {
        // Arrange
        when(careGiverRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<ProfileResponse> result = searchCareGiverService.getAllCareGiversLite();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(careGiverRepository).findAll();
    }

    // Tests for getNameSuggestions method
    @Test
    void getNameSuggestions_WithValidPrefix_ShouldReturnSuggestions() {
        // Arrange
        String prefix = "Dr";
        List<String> expectedSuggestions = Arrays.asList("Dr. John Smith", "Dr. Jane Doe", "Dr. Robert Johnson");

        when(careGiverRepository.findNameSuggestions(prefix)).thenReturn(expectedSuggestions);

        // Act
        List<String> result = searchCareGiverService.getNameSuggestions(prefix);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.containsAll(expectedSuggestions));

        verify(careGiverRepository).findNameSuggestions(prefix);
    }

    @Test
    void getNameSuggestions_WithShortPrefix_ShouldReturnEmptyList() {
        // Arrange
        String prefix = "D";

        // Act
        List<String> result = searchCareGiverService.getNameSuggestions(prefix);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify repository is not called for short prefixes
        verify(careGiverRepository, never()).findNameSuggestions(anyString());
    }

    @Test
    void getNameSuggestions_WithNullPrefix_ShouldReturnEmptyList() {
        // Act
        List<String> result = searchCareGiverService.getNameSuggestions(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(careGiverRepository, never()).findNameSuggestions(anyString());
    }

    @Test
    void getNameSuggestions_WithWhitespacePrefix_ShouldReturnEmptyList() {
        // Act
        List<String> result = searchCareGiverService.getNameSuggestions("  ");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(careGiverRepository, never()).findNameSuggestions(anyString());
    }

    @Test
    void getNameSuggestions_WithTrimmedInput_ShouldPassTrimmedValueToRepository() {
        // Arrange
        String prefix = "  Dr. John  ";
        String expectedTrimmedPrefix = "Dr. John";
        List<String> expectedSuggestions = Arrays.asList("Dr. John Smith");

        when(careGiverRepository.findNameSuggestions(expectedTrimmedPrefix))
                .thenReturn(expectedSuggestions);

        // Act
        List<String> result = searchCareGiverService.getNameSuggestions(prefix);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(careGiverRepository).findNameSuggestions(expectedTrimmedPrefix);
    }

    // Tests for getSpecialitySuggestions method
    @Test
    void getSpecialitySuggestions_WithValidQuery_ShouldReturnSuggestions() {
        // Arrange
        String query = "Card";
        List<String> expectedSuggestions = Arrays.asList("Cardiology", "Cardiac Surgery");

        when(careGiverRepository.findSpecialitySuggestions(query)).thenReturn(expectedSuggestions);

        // Act
        List<String> result = searchCareGiverService.getSpecialitySuggestions(query);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(expectedSuggestions));

        verify(careGiverRepository).findSpecialitySuggestions(query);
    }

    @Test
    void getSpecialitySuggestions_WithShortQuery_ShouldReturnEmptyList() {
        // Arrange
        String query = "C";

        // Act
        List<String> result = searchCareGiverService.getSpecialitySuggestions(query);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(careGiverRepository, never()).findSpecialitySuggestions(anyString());
    }

    @Test
    void getSpecialitySuggestions_WithNullQuery_ShouldReturnEmptyList() {
        // Act
        List<String> result = searchCareGiverService.getSpecialitySuggestions(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(careGiverRepository, never()).findSpecialitySuggestions(anyString());
    }

    @Test
    void getSpecialitySuggestions_WithTrimmedInput_ShouldPassTrimmedValueToRepository() {
        // Arrange
        String query = "  Cardiology  ";
        String expectedTrimmedQuery = "Cardiology";
        List<String> expectedSuggestions = Arrays.asList("Cardiology");

        when(careGiverRepository.findSpecialitySuggestions(expectedTrimmedQuery))
                .thenReturn(expectedSuggestions);

        // Act
        List<String> result = searchCareGiverService.getSpecialitySuggestions(query);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(careGiverRepository).findSpecialitySuggestions(expectedTrimmedQuery);
    }

    // Tests for searchCareGiversPaginated method
    @Test
    void searchCareGiversPaginated_WithBothNameAndSpeciality_ShouldReturnPagedResults() {
        // Arrange
        String name = "Dr";
        String speciality = "Cardiology";
        int page = 0;
        int size = 2;

        List<CareGiver> filteredCareGivers = Arrays.asList(careGiver1, careGiver3);
        Page<CareGiver> mockPage = new PageImpl<>(filteredCareGivers, PageRequest.of(page, size), filteredCareGivers.size());

        when(careGiverRepository.findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(eq("Dr"), eq("Cardiology"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginated(name, speciality, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(0, result.getNumber());
        assertEquals(2, result.getSize());

        verify(careGiverRepository).findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(eq("Dr"), eq("Cardiology"), any(Pageable.class));
    }

    @Test
    void searchCareGiversPaginated_WithOnlyName_ShouldReturnPagedResults() {
        // Arrange
        String name = "Dr";
        String speciality = null;
        int page = 0;
        int size = 10;

        List<CareGiver> filteredCareGivers = Arrays.asList(careGiver1, careGiver2, careGiver3);
        Page<CareGiver> mockPage = new PageImpl<>(filteredCareGivers, PageRequest.of(page, size), filteredCareGivers.size());

        when(careGiverRepository.findByNameContainingIgnoreCase(eq("Dr"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginated(name, speciality, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getContent().size());

        verify(careGiverRepository).findByNameContainingIgnoreCase(eq("Dr"), any(Pageable.class));
    }

    @Test
    void searchCareGiversPaginated_WithOnlySpeciality_ShouldReturnPagedResults() {
        // Arrange
        String name = null;
        String speciality = "Cardiology";
        int page = 0;
        int size = 10;

        List<CareGiver> filteredCareGivers = Arrays.asList(careGiver1, careGiver3);
        Page<CareGiver> mockPage = new PageImpl<>(filteredCareGivers, PageRequest.of(page, size), filteredCareGivers.size());

        when(careGiverRepository.findBySpecialityContainingIgnoreCase(eq("Cardiology"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginated(name, speciality, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        verify(careGiverRepository).findBySpecialityContainingIgnoreCase(eq("Cardiology"), any(Pageable.class));
    }

    @Test
    void searchCareGiversPaginated_WithNoFilters_ShouldReturnAllResults() {
        // Arrange
        String name = null;
        String speciality = null;
        int page = 0;
        int size = 10;

        Page<CareGiver> mockPage = new PageImpl<>(mockCareGivers, PageRequest.of(page, size), mockCareGivers.size());

        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginated(name, speciality, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getContent().size());

        verify(careGiverRepository).findAll(any(Pageable.class));
    }

    @Test
    void searchCareGiversPaginated_WithNegativePageAndSize_ShouldValidateParameters() {
        // Arrange
        String name = "Dr";
        String speciality = "Cardiology";
        int page = -1;
        int size = -5;

        List<CareGiver> filteredCareGivers = Arrays.asList(careGiver1);
        Page<CareGiver> mockPage = new PageImpl<>(filteredCareGivers, PageRequest.of(0, 10), 1);

        when(careGiverRepository.findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(eq("Dr"), eq("Cardiology"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginated(name, speciality, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        // Verify that corrected parameters were used (page=0, size=10)
        verify(careGiverRepository).findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(eq("Dr"), eq("Cardiology"),
                argThat((Pageable pageable) -> pageable.getPageNumber() == 0 && pageable.getPageSize() == 10));
    }

    @Test
    void searchCareGiversPaginated_WithLargeSize_ShouldLimitToMaxSize() {
        // Arrange
        String name = "Test";
        String speciality = null;
        int page = 0;
        int size = 200; // Exceeds max size

        Page<CareGiver> mockPage = new PageImpl<>(Arrays.asList(careGiver1), PageRequest.of(0, 10), 1);

        when(careGiverRepository.findByNameContainingIgnoreCase(eq("Test"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginated(name, speciality, page, size);

        // Assert
        assertNotNull(result);

        // Verify that size was limited to 10 (since 200 > 100)
        verify(careGiverRepository).findByNameContainingIgnoreCase(eq("Test"),
                argThat((Pageable pageable) -> pageable.getPageSize() == 10));
    }

    @Test
    void searchCareGiversPaginated_WithEmptyStringFilters_ShouldCleanParameters() {
        // Arrange
        String name = "  "; // Whitespace only
        String speciality = ""; // Empty string
        int page = 0;
        int size = 10;

        Page<CareGiver> mockPage = new PageImpl<>(mockCareGivers, PageRequest.of(0, 10), 3);

        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginated(name, speciality, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getContent().size());

        // Verify that cleaned parameters (null) were passed to repository
        verify(careGiverRepository).findAll(any(Pageable.class));
    }

    // Tests for searchCareGiversPaginatedWithSort method
    @Test
    void searchCareGiversPaginatedWithSort_ShouldReturnSortedPagedResults() {
        // Arrange
        String name = "Dr";
        String speciality = "Cardiology";
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "asc";

        Page<CareGiver> mockPage = new PageImpl<>(Arrays.asList(careGiver1, careGiver3),
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")), 2);

        when(careGiverRepository.findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(eq("Dr"), eq("Cardiology"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginatedWithSort(
                name, speciality, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        // Verify correct sorting was applied
        verify(careGiverRepository).findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(eq("Dr"), eq("Cardiology"),
                argThat((Pageable pageable) -> pageable.getSort().getOrderFor("name") != null &&
                        pageable.getSort().getOrderFor("name").getDirection() == Sort.Direction.ASC));
    }

    @Test
    void searchCareGiversPaginatedWithSort_WithInvalidSortField_ShouldUseDefaultSort() {
        // Arrange
        String name = "Dr";
        String speciality = "Cardiology";
        int page = 0;
        int size = 10;
        String sortBy = "invalidField"; // Not in allowed fields
        String sortDirection = "asc";

        Page<CareGiver> mockPage = new PageImpl<>(Arrays.asList(careGiver1),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "averageRating")), 1);

        when(careGiverRepository.findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(eq("Dr"), eq("Cardiology"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginatedWithSort(
                name, speciality, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        // Verify that default sort field "averageRating" was used instead of invalid field
        verify(careGiverRepository).findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(eq("Dr"), eq("Cardiology"),
                argThat((Pageable pageable) -> pageable.getSort().getOrderFor("averageRating") != null));
    }

    @Test
    void searchCareGiversPaginatedWithSort_WithNullSortBy_ShouldUseDefaultSort() {
        // Arrange
        String name = "Test";
        String speciality = null;
        int page = 0;
        int size = 5;
        String sortBy = null; // Null sort field
        String sortDirection = "desc";

        Page<CareGiver> mockPage = new PageImpl<>(Arrays.asList(careGiver1),
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "averageRating")), 1);

        when(careGiverRepository.findByNameContainingIgnoreCase(eq("Test"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginatedWithSort(
                name, speciality, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        // Verify that default sort field "averageRating" was used
        verify(careGiverRepository).findByNameContainingIgnoreCase(eq("Test"),
                argThat((Pageable pageable) -> pageable.getSort().getOrderFor("averageRating") != null &&
                        pageable.getSort().getOrderFor("averageRating").getDirection() == Sort.Direction.DESC));
    }

    // Tests for getTopRatedCareGivers method
    @Test
    void getTopRatedCareGivers_ShouldReturnTopRatedResults() {
        // Arrange
        int page = 0;
        int size = 10;

        // Sort caregivers by rating (highest first)
        List<CareGiver> sortedCareGivers = Arrays.asList(careGiver2, careGiver1, careGiver3); // 4.8, 4.5, 4.2
        Page<CareGiver> mockPage = new PageImpl<>(sortedCareGivers,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "averageRating", "ratingCount")), 3);

        when(careGiverRepository.findAll(any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.getTopRatedCareGivers(page, size);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getContent().size());

        // Verify sorting by rating (highest first)
        assertEquals("Dr. Jane Doe", result.getContent().get(0).getName()); // 4.8 rating
        assertEquals("Dr. John Smith", result.getContent().get(1).getName()); // 4.5 rating
        assertEquals("Dr. Robert Johnson", result.getContent().get(2).getName()); // 4.2 rating

        verify(careGiverRepository).findAll(argThat((Pageable pageable) ->
                pageable.getSort().getOrderFor("averageRating") != null &&
                        pageable.getSort().getOrderFor("averageRating").getDirection() == Sort.Direction.DESC));
    }

    @Test
    void getTopRatedCareGivers_WithNegativePageAndSize_ShouldValidateParameters() {
        // Arrange
        int page = -5;
        int size = -10;

        Page<CareGiver> mockPage = new PageImpl<>(mockCareGivers, PageRequest.of(0, 10), 3);

        when(careGiverRepository.findAll(any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.getTopRatedCareGivers(page, size);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getContent().size());

        // Verify that corrected parameters were used (page=0, size=10)
        verify(careGiverRepository).findAll(argThat((Pageable pageable) ->
                pageable.getPageNumber() == 0 && pageable.getPageSize() == 10));
    }

    @Test
    void getTopRatedCareGivers_WithZeroSize_ShouldUseDefaultSize() {
        // Arrange
        int page = 0;
        int size = 0; // Zero size should be corrected to default

        Page<CareGiver> mockPage = new PageImpl<>(Arrays.asList(careGiver1), PageRequest.of(0, 10), 1);

        when(careGiverRepository.findAll(any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.getTopRatedCareGivers(page, size);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        // Verify that default size (10) was used
        verify(careGiverRepository).findAll(argThat((Pageable pageable) ->
                pageable.getPageSize() == 10));
    }

    @Test
    void getTopRatedCareGivers_WithLargeSize_ShouldLimitToMaxSize() {
        // Arrange
        int page = 0;
        int size = 100; // Exceeds max size of 50 for top-rated method

        Page<CareGiver> mockPage = new PageImpl<>(Arrays.asList(careGiver1), PageRequest.of(0, 10), 1);

        when(careGiverRepository.findAll(any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.getTopRatedCareGivers(page, size);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        // Verify that size was limited to default (10) since 100 > 50
        verify(careGiverRepository).findAll(argThat((Pageable pageable) ->
                pageable.getPageSize() == 10));
    }

    @Test
    void getTopRatedCareGivers_WithValidSize_ShouldUseSpecifiedSize() {
        // Arrange
        int page = 1;
        int size = 25; // Valid size within limits

        Page<CareGiver> mockPage = new PageImpl<>(Arrays.asList(careGiver2), PageRequest.of(1, 25), 1);

        when(careGiverRepository.findAll(any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.getTopRatedCareGivers(page, size);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        // Verify that specified size was used
        verify(careGiverRepository).findAll(argThat((Pageable pageable) ->
                pageable.getPageNumber() == 1 && pageable.getPageSize() == 25));
    }

    // Test for createLiteProfileResponse (private method tested through public methods)
    @Test
    void createLiteProfileResponse_ShouldHideSensitiveInformation() {
        // This test verifies the private method behavior through public methods
        // Arrange
        when(careGiverRepository.findAll()).thenReturn(Arrays.asList(careGiver1));

        // Act
        List<ProfileResponse> result = searchCareGiverService.getAllCareGiversLite();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        ProfileResponse profile = result.get(0);
        assertEquals(careGiver1.getId(), profile.getId());
        assertEquals(careGiver1.getName(), profile.getName());
        assertEquals(careGiver1.getEmail(), profile.getEmail());
        assertEquals(careGiver1.getPhoneNumber(), profile.getPhoneNumber());
        assertEquals(careGiver1.getSpeciality(), profile.getSpeciality());
        assertEquals(careGiver1.getWorkAddress(), profile.getWorkAddress());
        assertEquals(careGiver1.getAverageRating(), profile.getAverageRating());
        assertEquals("CAREGIVER", profile.getUserType());

        // Verify sensitive information is hidden
        assertNull(profile.getNik());
        assertNull(profile.getAddress());
    }

    // Additional edge case tests for comprehensive coverage
    @Test
    void searchCareGiversPaginated_WithSortingValidation_ShouldUsePredefinedSort() {
        // Arrange
        String name = "Test";
        String speciality = "Test";
        int page = 0;
        int size = 5;

        Page<CareGiver> mockPage = new PageImpl<>(Arrays.asList(careGiver1), PageRequest.of(0, 5), 1);

        when(careGiverRepository.findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(eq("Test"), eq("Test"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginated(name, speciality, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        // Verify that the predefined sorting (averageRating DESC, name ASC) was used
        verify(careGiverRepository).findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(eq("Test"), eq("Test"),
                argThat((Pageable pageable) ->
                        pageable.getSort().getOrderFor("averageRating") != null &&
                                pageable.getSort().getOrderFor("averageRating").getDirection() == Sort.Direction.DESC &&
                                pageable.getSort().getOrderFor("name") != null &&
                                pageable.getSort().getOrderFor("name").getDirection() == Sort.Direction.ASC));
    }

    @Test
    void searchCareGiversPaginatedWithSort_WithMixedCaseSortDirection_ShouldHandleCorrectly() {
        // Arrange
        String name = "Test";
        String speciality = null;
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "DESC"; // Mixed case

        Page<CareGiver> mockPage = new PageImpl<>(Arrays.asList(careGiver1), PageRequest.of(0, 10), 1);

        when(careGiverRepository.findByNameContainingIgnoreCase(eq("Test"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginatedWithSort(
                name, speciality, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        // Verify that DESC direction was correctly parsed
        verify(careGiverRepository).findByNameContainingIgnoreCase(eq("Test"),
                argThat((Pageable pageable) -> pageable.getSort().getOrderFor("name") != null &&
                        pageable.getSort().getOrderFor("name").getDirection() == Sort.Direction.DESC));
    }

    @Test
    void searchCareGiversPaginatedWithSort_WithRandomSortDirection_ShouldDefaultToAsc() {
        // Arrange
        String name = null;
        String speciality = "Test";
        int page = 0;
        int size = 10;
        String sortBy = "speciality";
        String sortDirection = "random"; // Invalid direction

        Page<CareGiver> mockPage = new PageImpl<>(Arrays.asList(careGiver1), PageRequest.of(0, 10), 1);

        when(careGiverRepository.findBySpecialityContainingIgnoreCase(eq("Test"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginatedWithSort(
                name, speciality, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        // Verify that ASC direction was used as default for invalid direction
        verify(careGiverRepository).findBySpecialityContainingIgnoreCase(eq("Test"),
                argThat((Pageable pageable) -> pageable.getSort().getOrderFor("speciality") != null &&
                        pageable.getSort().getOrderFor("speciality").getDirection() == Sort.Direction.ASC));
    }

    @Test
    void getTopRatedCareGivers_WithExactBoundarySize_ShouldUseSpecifiedSize() {
        // Arrange
        int page = 0;
        int size = 50; // Exact boundary (max allowed for top-rated)

        Page<CareGiver> mockPage = new PageImpl<>(mockCareGivers, PageRequest.of(0, 50), 3);

        when(careGiverRepository.findAll(any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.getTopRatedCareGivers(page, size);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getContent().size());

        // Verify that size 50 was used (at boundary)
        verify(careGiverRepository).findAll(argThat((Pageable pageable) ->
                pageable.getPageSize() == 50));
    }

    @Test
    void getTopRatedCareGivers_WithSizeJustOverBoundary_ShouldUseDefaultSize() {
        // Arrange
        int page = 0;
        int size = 51; // Just over boundary (max is 50)

        Page<CareGiver> mockPage = new PageImpl<>(Arrays.asList(careGiver1), PageRequest.of(0, 10), 1);

        when(careGiverRepository.findAll(any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.getTopRatedCareGivers(page, size);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        // Verify that default size (10) was used since 51 > 50
        verify(careGiverRepository).findAll(argThat((Pageable pageable) ->
                pageable.getPageSize() == 10));
    }

    @Test
    void searchCareGiversPaginatedWithSort_WithValidSpecialitySort_ShouldUseSpecifiedSort() {
        // Arrange
        String name = null;
        String speciality = "Cardiology";
        int page = 0;
        int size = 10;
        String sortBy = "speciality";
        String sortDirection = "asc";

        Page<CareGiver> mockPage = new PageImpl<>(Arrays.asList(careGiver1), PageRequest.of(0, 10), 1);
        when(careGiverRepository.findBySpecialityContainingIgnoreCase(eq("Cardiology"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginatedWithSort(
                name, speciality, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(result);
        verify(careGiverRepository).findBySpecialityContainingIgnoreCase(eq("Cardiology"),
                argThat((Pageable pageable) -> pageable.getSort().getOrderFor("speciality") != null &&
                        pageable.getSort().getOrderFor("speciality").getDirection() == Sort.Direction.ASC));
    }

    @Test
    void searchCareGiversPaginatedWithSort_WithRatingCountSort_ShouldUseSpecifiedSort() {
        // Arrange
        String name = "Test";
        String speciality = null;
        int page = 0;
        int size = 10;
        String sortBy = "ratingCount";
        String sortDirection = "desc";

        Page<CareGiver> mockPage = new PageImpl<>(Arrays.asList(careGiver1), PageRequest.of(0, 10), 1);
        when(careGiverRepository.findByNameContainingIgnoreCase(eq("Test"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginatedWithSort(
                name, speciality, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(result);
        verify(careGiverRepository).findByNameContainingIgnoreCase(eq("Test"),
                argThat((Pageable pageable) -> pageable.getSort().getOrderFor("ratingCount") != null &&
                        pageable.getSort().getOrderFor("ratingCount").getDirection() == Sort.Direction.DESC));
    }

    // ADDITIONAL TESTS FOR UNCOVERED CODE PATHS

    @Test
    void searchCareGiversPaginated_WithNegativePage_ShouldSetPageToZero() {
        // Test the specific line: if (page < 0) page = 0;
        // Arrange
        String name = "Dr";
        String speciality = "Cardiology";
        int page = -3; // Negative page to trigger the condition
        int size = 5;

        Page<CareGiver> mockPage = new PageImpl<>(Arrays.asList(careGiver1), PageRequest.of(0, 5), 1);
        when(careGiverRepository.findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(eq("Dr"), eq("Cardiology"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginated(name, speciality, page, size);

        // Assert
        assertNotNull(result);
        verify(careGiverRepository).findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(eq("Dr"), eq("Cardiology"),
                argThat((Pageable pageable) -> pageable.getPageNumber() == 0)); // Verify page was set to 0
    }

    @Test
    void searchCareGiversPaginated_WithZeroSize_ShouldSetSizeToTen() {
        // Test the specific line: if (size <= 0 || size > 100) size = 10;
        // Arrange
        String name = "Dr";
        String speciality = "Cardiology";
        int page = 1;
        int size = 0; // Zero size to trigger the condition

        Page<CareGiver> mockPage = new PageImpl<>(Arrays.asList(careGiver1), PageRequest.of(1, 10), 1);
        when(careGiverRepository.findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(eq("Dr"), eq("Cardiology"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginated(name, speciality, page, size);

        // Assert
        assertNotNull(result);
        verify(careGiverRepository).findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(eq("Dr"), eq("Cardiology"),
                argThat((Pageable pageable) -> pageable.getPageSize() == 10)); // Verify size was set to 10
    }

    @Test
    void searchCareGiversPaginated_WithSizeOver100_ShouldSetSizeToTen() {
        // Test the specific line: if (size <= 0 || size > 100) size = 10;
        // Arrange
        String name = "Dr";
        String speciality = "Cardiology";
        int page = 0;
        int size = 150; // Size > 100 to trigger the condition

        Page<CareGiver> mockPage = new PageImpl<>(Arrays.asList(careGiver1), PageRequest.of(0, 10), 1);
        when(careGiverRepository.findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(eq("Dr"), eq("Cardiology"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginated(name, speciality, page, size);

        // Assert
        assertNotNull(result);
        verify(careGiverRepository).findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(eq("Dr"), eq("Cardiology"),
                argThat((Pageable pageable) -> pageable.getPageSize() == 10)); // Verify size was set to 10
    }

    @Test
    void searchCareGiversPaginated_WithEmptyStringName_ShouldCleanToNull() {
        // Test the specific line: String cleanName = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
        // Arrange
        String name = ""; // Empty string to trigger cleaning to null
        String speciality = "Cardiology";
        int page = 0;
        int size = 10;

        Page<CareGiver> mockPage = new PageImpl<>(Arrays.asList(careGiver1, careGiver3), PageRequest.of(0, 10), 2);
        when(careGiverRepository.findBySpecialityContainingIgnoreCase(eq("Cardiology"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginated(name, speciality, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        // Verify that only speciality filter was used (name was cleaned to null)
        verify(careGiverRepository).findBySpecialityContainingIgnoreCase(eq("Cardiology"), any(Pageable.class));
        verify(careGiverRepository, never()).findByNameContainingIgnoreCase(anyString(), any(Pageable.class));
        verify(careGiverRepository, never()).findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(anyString(), anyString(), any(Pageable.class));
    }

    @Test
    void searchCareGiversPaginated_WithWhitespaceOnlySpeciality_ShouldCleanToNull() {
        // Test the specific line: String cleanSpeciality = (speciality != null && !speciality.trim().isEmpty()) ? speciality.trim() : null;
        // Arrange
        String name = "Dr";
        String speciality = "   "; // Whitespace only to trigger cleaning to null
        int page = 0;
        int size = 10;

        Page<CareGiver> mockPage = new PageImpl<>(mockCareGivers, PageRequest.of(0, 10), 3);
        when(careGiverRepository.findByNameContainingIgnoreCase(eq("Dr"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginated(name, speciality, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        // Verify that only name filter was used (speciality was cleaned to null)
        verify(careGiverRepository).findByNameContainingIgnoreCase(eq("Dr"), any(Pageable.class));
        verify(careGiverRepository, never()).findBySpecialityContainingIgnoreCase(anyString(), any(Pageable.class));
        verify(careGiverRepository, never()).findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(anyString(), anyString(), any(Pageable.class));
    }

    @Test
    void searchCareGiversPaginated_WithBothFiltersNullAfterCleaning_ShouldUseFindAll() {
        // Test the specific line: } else { careGiversPage = careGiverRepository.findAll(pageable); }
        // Arrange
        String name = ""; // Empty string will be cleaned to null
        String speciality = "   "; // Whitespace only will be cleaned to null
        int page = 0;
        int size = 10;

        Page<CareGiver> mockPage = new PageImpl<>(mockCareGivers, PageRequest.of(0, 10), 3);
        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginated(name, speciality, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        // Verify that findAll was called (both filters were cleaned to null)
        verify(careGiverRepository).findAll(any(Pageable.class));
        verify(careGiverRepository, never()).findByNameContainingIgnoreCase(anyString(), any(Pageable.class));
        verify(careGiverRepository, never()).findBySpecialityContainingIgnoreCase(anyString(), any(Pageable.class));
        verify(careGiverRepository, never()).findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(anyString(), anyString(), any(Pageable.class));
    }

    @Test
    void searchCareGiversPaginatedWithSort_WithNegativePage_ShouldSetPageToZero() {
        // Test parameter validation in the WithSort method
        // Arrange
        String name = "Test";
        String speciality = null;
        int page = -2; // Negative page to trigger the condition
        int size = 5;
        String sortBy = "name";
        String sortDirection = "asc";

        Page<CareGiver> mockPage = new PageImpl<>(Arrays.asList(careGiver1), PageRequest.of(0, 5), 1);
        when(careGiverRepository.findByNameContainingIgnoreCase(eq("Test"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginatedWithSort(
                name, speciality, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(result);
        verify(careGiverRepository).findByNameContainingIgnoreCase(eq("Test"),
                argThat((Pageable pageable) -> pageable.getPageNumber() == 0)); // Verify page was set to 0
    }

    @Test
    void searchCareGiversPaginatedWithSort_WithZeroSize_ShouldSetSizeToTen() {
        // Test parameter validation in the WithSort method
        // Arrange
        String name = null;
        String speciality = "Test";
        int page = 0;
        int size = 0; // Zero size to trigger the condition
        String sortBy = "speciality";
        String sortDirection = "desc";

        Page<CareGiver> mockPage = new PageImpl<>(Arrays.asList(careGiver1), PageRequest.of(0, 10), 1);
        when(careGiverRepository.findBySpecialityContainingIgnoreCase(eq("Test"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginatedWithSort(
                name, speciality, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(result);
        verify(careGiverRepository).findBySpecialityContainingIgnoreCase(eq("Test"),
                argThat((Pageable pageable) -> pageable.getPageSize() == 10)); // Verify size was set to 10
    }

    @Test
    void searchCareGiversPaginatedWithSort_WithBothFiltersNullAfterCleaning_ShouldUseFindAll() {
        // Test the else branch in the WithSort method
        // Arrange
        String name = null; // Null name
        String speciality = ""; // Empty string will be cleaned to null
        int page = 0;
        int size = 15;
        String sortBy = "averageRating";
        String sortDirection = "desc";

        Page<CareGiver> mockPage = new PageImpl<>(mockCareGivers, PageRequest.of(0, 15), 3);
        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginatedWithSort(
                name, speciality, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        // Verify that findAll was called with custom sorting
        verify(careGiverRepository).findAll(argThat((Pageable pageable) ->
                pageable.getSort().getOrderFor("averageRating") != null &&
                        pageable.getSort().getOrderFor("averageRating").getDirection() == Sort.Direction.DESC));
        verify(careGiverRepository, never()).findByNameContainingIgnoreCase(anyString(), any(Pageable.class));
        verify(careGiverRepository, never()).findBySpecialityContainingIgnoreCase(anyString(), any(Pageable.class));
        verify(careGiverRepository, never()).findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(anyString(), anyString(), any(Pageable.class));
    }

    @Test
    void searchCareGiversPaginated_WithSize101_ShouldSetSizeToTen() {
        // Test the specific condition: size > 100 (when size <= 0 is false)
        // Arrange
        String name = "Dr";
        String speciality = "Cardiology";
        int page = 0;
        int size = 101; // Exactly 101 to hit size > 100 condition

        Page<CareGiver> mockPage = new PageImpl<>(Arrays.asList(careGiver1), PageRequest.of(0, 10), 1);
        when(careGiverRepository.findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(eq("Dr"), eq("Cardiology"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginated(name, speciality, page, size);

        // Assert
        assertNotNull(result);
        verify(careGiverRepository).findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(eq("Dr"), eq("Cardiology"),
                argThat((Pageable pageable) -> pageable.getPageSize() == 10));
    }

    @Test
    void searchCareGiversPaginated_WithNameContainingOnlySpaces_ShouldCleanToNull() {
        // Test the specific condition: name != null is true, but !name.trim().isEmpty() is false
        // Arrange
        String name = "   "; // Not null, but trim().isEmpty() is true
        String speciality = "Cardiology";
        int page = 0;
        int size = 10;

        Page<CareGiver> mockPage = new PageImpl<>(Arrays.asList(careGiver1, careGiver3), PageRequest.of(0, 10), 2);
        when(careGiverRepository.findBySpecialityContainingIgnoreCase(eq("Cardiology"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginated(name, speciality, page, size);

        // Assert
        assertNotNull(result);
        verify(careGiverRepository).findBySpecialityContainingIgnoreCase(eq("Cardiology"), any(Pageable.class));
        verify(careGiverRepository, never()).findByNameContainingIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    void searchCareGiversPaginatedWithSort_WithSize101_ShouldSetSizeToTen() {
        // Test the same condition in WithSort method
        // Arrange
        String name = "Test";
        String speciality = null;
        int page = 0;
        int size = 101; // Exactly 101 to hit size > 100 condition
        String sortBy = "name";
        String sortDirection = "asc";

        Page<CareGiver> mockPage = new PageImpl<>(Arrays.asList(careGiver1), PageRequest.of(0, 10), 1);
        when(careGiverRepository.findByNameContainingIgnoreCase(eq("Test"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginatedWithSort(
                name, speciality, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(result);
        verify(careGiverRepository).findByNameContainingIgnoreCase(eq("Test"),
                argThat((Pageable pageable) -> pageable.getPageSize() == 10));
    }

    @Test
    void searchCareGiversPaginatedWithSort_WithNameContainingOnlySpaces_ShouldCleanToNull() {
        // Test the same condition in WithSort method
        // Arrange
        String name = "   "; // Not null, but trim().isEmpty() is true
        String speciality = "Test";
        int page = 0;
        int size = 10;
        String sortBy = "speciality";
        String sortDirection = "desc";

        Page<CareGiver> mockPage = new PageImpl<>(Arrays.asList(careGiver1), PageRequest.of(0, 10), 1);
        when(careGiverRepository.findBySpecialityContainingIgnoreCase(eq("Test"), any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<ProfileResponse> result = searchCareGiverService.searchCareGiversPaginatedWithSort(
                name, speciality, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(result);
        verify(careGiverRepository).findBySpecialityContainingIgnoreCase(eq("Test"), any(Pageable.class));
        verify(careGiverRepository, never()).findByNameContainingIgnoreCase(anyString(), any(Pageable.class));
    }

}