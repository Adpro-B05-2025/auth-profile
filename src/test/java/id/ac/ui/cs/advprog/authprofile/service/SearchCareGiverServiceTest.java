package id.ac.ui.cs.advprog.authprofile.service;

import id.ac.ui.cs.advprog.authprofile.config.MonitoringConfig;
import id.ac.ui.cs.advprog.authprofile.dto.response.ProfileResponse;
import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.repository.CareGiverRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class SearchCareGiverServiceTest {

    @Mock
    private CareGiverRepository careGiverRepository;

    @Mock
    private MonitoringConfig monitoringConfig;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter mockCounter;

    @Mock
    private Gauge mockGauge;

    @InjectMocks
    private SearchCareGiverService searchCareGiverService;

    private CareGiver careGiver1;
    private CareGiver careGiver2;
    private List<CareGiver> careGivers;

    @BeforeEach
    void setUp() {
        careGiver1 = new CareGiver();
        careGiver1.setId(1L);
        careGiver1.setEmail("dr.smith@example.com");
        careGiver1.setName("Dr. John Smith");
        careGiver1.setPhoneNumber("123-456-7890");
        careGiver1.setSpeciality("Cardiology");
        careGiver1.setWorkAddress("123 Medical Center");
        careGiver1.setAverageRating(4.5);

        careGiver2 = new CareGiver();
        careGiver2.setId(2L);
        careGiver2.setEmail("dr.johnson@example.com");
        careGiver2.setName("Dr. Jane Johnson");
        careGiver2.setPhoneNumber("098-765-4321");
        careGiver2.setSpeciality("Pediatrics");
        careGiver2.setWorkAddress("456 Health Plaza");
        careGiver2.setAverageRating(4.8);

        careGivers = Arrays.asList(careGiver1, careGiver2);

        // Setup monitoring config mocks with lenient stubbing
        monitoringConfig.meterRegistry = meterRegistry;
        lenient().when(meterRegistry.counter(anyString(), any(Tags.class))).thenReturn(mockCounter);
        lenient().when(meterRegistry.gauge(anyString(), any(Tags.class), any(), any())).thenReturn(mockGauge);
    }

    // EXISTING TESTS (keeping all your current tests)
    @Test
    void testSearchCareGiversOptimized_WithBothNameAndSpeciality() throws ExecutionException, InterruptedException {
        // Given
        when(careGiverRepository.findCareGiversWithFilters("John", "Cardiology"))
                .thenReturn(List.of(careGiver1));

        // When
        CompletableFuture<List<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversOptimized("John", "Cardiology");

        // Then
        List<ProfileResponse> responses = result.get();
        assertEquals(1, responses.size());
        assertEquals("Dr. John Smith", responses.get(0).getName());
        assertEquals("Cardiology", responses.get(0).getSpeciality());
        assertNull(responses.get(0).getNik()); // Should be hidden
        assertNull(responses.get(0).getAddress()); // Should be hidden

        verify(careGiverRepository).findCareGiversWithFilters("John", "Cardiology");
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversOptimized_WithNameOnly() throws ExecutionException, InterruptedException {
        // Given
        when(careGiverRepository.findCareGiversWithFilters("Smith", null))
                .thenReturn(List.of(careGiver1));

        // When
        CompletableFuture<List<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversOptimized("Smith", "");

        // Then
        List<ProfileResponse> responses = result.get();
        assertEquals(1, responses.size());
        verify(careGiverRepository).findCareGiversWithFilters("Smith", null);
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversOptimized_WithSpecialityOnly() throws ExecutionException, InterruptedException {
        // Given
        when(careGiverRepository.findCareGiversWithFilters(null, "Pediatrics"))
                .thenReturn(List.of(careGiver2));

        // When
        CompletableFuture<List<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversOptimized(null, "Pediatrics");

        // Then
        List<ProfileResponse> responses = result.get();
        assertEquals(1, responses.size());
        assertEquals("Pediatrics", responses.get(0).getSpeciality());
        verify(careGiverRepository).findCareGiversWithFilters(null, "Pediatrics");
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversOptimized_WithNoFilters() throws ExecutionException, InterruptedException {
        // Given
        when(careGiverRepository.findCareGiversWithFilters(null, null))
                .thenReturn(careGivers);

        // When
        CompletableFuture<List<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversOptimized(null, null);

        // Then
        List<ProfileResponse> responses = result.get();
        assertEquals(2, responses.size());
        verify(careGiverRepository).findCareGiversWithFilters(null, null);
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversOptimized_WithWhitespaceInputs() throws ExecutionException, InterruptedException {
        // Given
        when(careGiverRepository.findCareGiversWithFilters(null, null))
                .thenReturn(careGivers);

        // When
        CompletableFuture<List<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversOptimized("  ", "  ");

        // Then
        List<ProfileResponse> responses = result.get();
        assertEquals(2, responses.size());
        verify(careGiverRepository).findCareGiversWithFilters(null, null);
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversPaginated_WithBothFilters() throws ExecutionException, InterruptedException {
        // Given
        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "averageRating")
                        .and(Sort.by(Sort.Direction.ASC, "name")));
        Page<CareGiver> page = new PageImpl<>(List.of(careGiver1), pageable, 1);

        when(careGiverRepository.findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(
                eq("John"), eq("Cardiology"), any(Pageable.class)))
                .thenReturn(page);

        // When
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversPaginated("John", "Cardiology", 0, 10);

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(1, responses.getTotalElements());
        assertEquals("Dr. John Smith", responses.getContent().get(0).getName());
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversPaginated_WithNameOnly() throws ExecutionException, InterruptedException {
        // Given
        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "averageRating")
                        .and(Sort.by(Sort.Direction.ASC, "name")));
        Page<CareGiver> page = new PageImpl<>(List.of(careGiver1), pageable, 1);

        when(careGiverRepository.findByNameContainingIgnoreCase(eq("Smith"), any(Pageable.class)))
                .thenReturn(page);

        // When
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversPaginated("Smith", null, 0, 10);

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(1, responses.getTotalElements());
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversPaginated_WithSpecialityOnly() throws ExecutionException, InterruptedException {
        // Given
        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "averageRating")
                        .and(Sort.by(Sort.Direction.ASC, "name")));
        Page<CareGiver> page = new PageImpl<>(List.of(careGiver2), pageable, 1);

        when(careGiverRepository.findBySpecialityContainingIgnoreCase(eq("Pediatrics"), any(Pageable.class)))
                .thenReturn(page);

        // When
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversPaginated(null, "Pediatrics", 0, 10);

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(1, responses.getTotalElements());
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversPaginated_WithNoFilters() throws ExecutionException, InterruptedException {
        // Given
        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "averageRating")
                        .and(Sort.by(Sort.Direction.ASC, "name")));
        Page<CareGiver> page = new PageImpl<>(careGivers, pageable, 2);

        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversPaginated(null, null, 0, 10);

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(2, responses.getTotalElements());
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversPaginated_WithInvalidParameters() throws ExecutionException, InterruptedException {
        // Given - negative page and invalid size
        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "averageRating")
                        .and(Sort.by(Sort.Direction.ASC, "name")));
        Page<CareGiver> page = new PageImpl<>(careGivers, pageable, 2);

        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversPaginated(null, null, -1, 150);

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(2, responses.getTotalElements());
        // Verify that invalid parameters were corrected (page=0, size=10)
        verify(careGiverRepository).findAll(argThat((Pageable pageable1) ->
                pageable1.getPageNumber() == 0 && pageable1.getPageSize() == 10));
        verify(mockCounter, atLeastOnce()).increment();
    }

    // NEW TESTS FOR MISSING COVERAGE

    @Test
    void testSearchCareGiversPaginated_WithZeroSize() throws ExecutionException, InterruptedException {
        // Given - test size <= 0 branch
        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "averageRating")
                        .and(Sort.by(Sort.Direction.ASC, "name")));
        Page<CareGiver> page = new PageImpl<>(careGivers, pageable, 2);

        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversPaginated(null, null, 0, 0);

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(2, responses.getTotalElements());
        // Verify that size was corrected to 10
        verify(careGiverRepository).findAll(argThat((Pageable pageable1) ->
                pageable1.getPageSize() == 10));
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversPaginated_WithExactly100Size() throws ExecutionException, InterruptedException {
        // Given - test exact boundary size == 100 (should be allowed)
        Pageable pageable = PageRequest.of(0, 100,
                Sort.by(Sort.Direction.DESC, "averageRating")
                        .and(Sort.by(Sort.Direction.ASC, "name")));
        Page<CareGiver> page = new PageImpl<>(careGivers, pageable, 2);

        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversPaginated(null, null, 0, 100);

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(2, responses.getTotalElements());
        // Verify that size 100 is allowed
        verify(careGiverRepository).findAll(argThat((Pageable pageable1) ->
                pageable1.getPageSize() == 100));
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversPaginated_WithEmptyStringFilters() throws ExecutionException, InterruptedException {
        // Given - test empty string filters (different from whitespace)
        Page<CareGiver> page = new PageImpl<>(careGivers, PageRequest.of(0, 10), 2);
        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversPaginated("", "", 0, 10);

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(2, responses.getTotalElements());
        verify(careGiverRepository).findAll(any(Pageable.class));
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversPaginatedWithSort_AllSortFields() throws ExecutionException, InterruptedException {
        // Test all valid sort fields
        String[] sortFields = {"name", "speciality", "averageRating", "ratingCount"};
        String[] directions = {"asc", "desc"};

        for (String field : sortFields) {
            for (String direction : directions) {
                // Given
                Sort.Direction expectedDirection = "desc".equalsIgnoreCase(direction) ?
                        Sort.Direction.DESC : Sort.Direction.ASC;
                Pageable pageable = PageRequest.of(0, 10, Sort.by(expectedDirection, field));
                Page<CareGiver> page = new PageImpl<>(careGivers, pageable, 2);

                when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

                // When
                CompletableFuture<Page<ProfileResponse>> result =
                        searchCareGiverService.searchCareGiversPaginatedWithSort(
                                null, null, 0, 10, field, direction);

                // Then
                Page<ProfileResponse> responses = result.get();
                assertEquals(2, responses.getTotalElements());

                // Reset mock for next iteration
                reset(careGiverRepository);
            }
        }
    }

    @Test
    void testSearchCareGiversPaginatedWithSort_InvalidSortField() throws ExecutionException, InterruptedException {
        // Given - invalid sort field should default to "averageRating"
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "averageRating"));
        Page<CareGiver> page = new PageImpl<>(careGivers, pageable, 2);

        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversPaginatedWithSort(
                        null, null, 0, 10, "invalidField", "desc");

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(2, responses.getTotalElements());
        verify(careGiverRepository).findAll(argThat((Pageable pageable1) ->
                pageable1.getSort().getOrderFor("averageRating") != null));
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversPaginatedWithSort_NullSortBy() throws ExecutionException, InterruptedException {
        // Given - null sortBy should default to "averageRating"
        Page<CareGiver> page = new PageImpl<>(careGivers, PageRequest.of(0, 10), 2);
        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversPaginatedWithSort(
                        null, null, 0, 10, null, "desc");

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(2, responses.getTotalElements());
        verify(careGiverRepository).findAll(argThat((Pageable pageable1) ->
                pageable1.getSort().getOrderFor("averageRating") != null));
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversPaginatedWithSort_WithBothFilters() throws ExecutionException, InterruptedException {
        // Given - test with both name and speciality filters
        Page<CareGiver> page = new PageImpl<>(List.of(careGiver1), PageRequest.of(0, 10), 1);
        when(careGiverRepository.findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(
                eq("John"), eq("Cardiology"), any(Pageable.class))).thenReturn(page);

        // When
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversPaginatedWithSort(
                        "John", "Cardiology", 0, 10, "name", "asc");

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(1, responses.getTotalElements());
        verify(careGiverRepository).findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(
                eq("John"), eq("Cardiology"), any(Pageable.class));
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversPaginatedWithSort_WithNameOnly() throws ExecutionException, InterruptedException {
        // Given - test with name filter only
        Page<CareGiver> page = new PageImpl<>(List.of(careGiver1), PageRequest.of(0, 10), 1);
        when(careGiverRepository.findByNameContainingIgnoreCase(eq("Smith"), any(Pageable.class)))
                .thenReturn(page);

        // When
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversPaginatedWithSort(
                        "Smith", null, 0, 10, "name", "asc");

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(1, responses.getTotalElements());
        verify(careGiverRepository).findByNameContainingIgnoreCase(eq("Smith"), any(Pageable.class));
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversPaginatedWithSort_WithSpecialityOnly() throws ExecutionException, InterruptedException {
        // Given - test with speciality filter only
        Page<CareGiver> page = new PageImpl<>(List.of(careGiver2), PageRequest.of(0, 10), 1);
        when(careGiverRepository.findBySpecialityContainingIgnoreCase(eq("Pediatrics"), any(Pageable.class)))
                .thenReturn(page);

        // When
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversPaginatedWithSort(
                        null, "Pediatrics", 0, 10, "speciality", "desc");

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(1, responses.getTotalElements());
        verify(careGiverRepository).findBySpecialityContainingIgnoreCase(eq("Pediatrics"), any(Pageable.class));
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversPaginatedWithSort_InvalidParameters() throws ExecutionException, InterruptedException {
        // Given - test parameter validation
        Page<CareGiver> page = new PageImpl<>(careGivers, PageRequest.of(0, 10), 2);
        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversPaginatedWithSort(
                        null, null, -5, 150, "invalidField", "invalid");

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(2, responses.getTotalElements());
        // Verify parameters were corrected
        verify(careGiverRepository).findAll(argThat((Pageable pageable1) ->
                pageable1.getPageNumber() == 0 && pageable1.getPageSize() == 10));
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testGetTopRatedCareGivers() throws ExecutionException, InterruptedException {
        // Given
        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "averageRating")
                        .and(Sort.by(Sort.Direction.DESC, "ratingCount")));
        Page<CareGiver> page = new PageImpl<>(careGivers, pageable, 2);

        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.getTopRatedCareGivers(0, 10);

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(2, responses.getTotalElements());
        verify(careGiverRepository).findAll(argThat((Pageable pageable1) ->
                pageable1.getSort().getOrderFor("averageRating") != null &&
                        pageable1.getSort().getOrderFor("ratingCount") != null));
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testGetTopRatedCareGivers_InvalidParameters() throws ExecutionException, InterruptedException {
        // Given - invalid parameters should be corrected
        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "averageRating")
                        .and(Sort.by(Sort.Direction.DESC, "ratingCount")));
        Page<CareGiver> page = new PageImpl<>(careGivers, pageable, 2);

        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.getTopRatedCareGivers(-1, 100);

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(2, responses.getTotalElements());
        // Verify that invalid parameters were corrected (page=0, size=10)
        verify(careGiverRepository).findAll(argThat((Pageable pageable1) ->
                pageable1.getPageNumber() == 0 && pageable1.getPageSize() == 10));
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testGetTopRatedCareGivers_ExactBoundarySize() throws ExecutionException, InterruptedException {
        // Given - test exact boundary size == 50 (should be allowed)
        Page<CareGiver> page = new PageImpl<>(careGivers, PageRequest.of(0, 50), 2);
        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.getTopRatedCareGivers(0, 50);

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(2, responses.getTotalElements());
        verify(careGiverRepository).findAll(argThat((Pageable pageable1) ->
                pageable1.getPageSize() == 50));
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testGetTopRatedCareGivers_ZeroSize() throws ExecutionException, InterruptedException {
        // Given - test size <= 0 branch
        Page<CareGiver> page = new PageImpl<>(careGivers, PageRequest.of(0, 10), 2);
        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.getTopRatedCareGivers(0, 0);

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(2, responses.getTotalElements());
        verify(careGiverRepository).findAll(argThat((Pageable pageable1) ->
                pageable1.getPageSize() == 10));
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testGetNameSuggestions_ValidPrefix() {
        // Given
        List<String> suggestions = Arrays.asList("Dr. John", "Dr. Jane");
        when(careGiverRepository.findNameSuggestions("Dr")).thenReturn(suggestions);

        // When
        List<String> result = searchCareGiverService.getNameSuggestions("Dr");

        // Then
        assertEquals(2, result.size());
        assertEquals("Dr. John", result.get(0));
        verify(careGiverRepository).findNameSuggestions("Dr");
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testGetNameSuggestions_ShortPrefix() {
        // When
        List<String> result = searchCareGiverService.getNameSuggestions("D");

        // Then
        assertEquals(0, result.size());
        verify(careGiverRepository, never()).findNameSuggestions(anyString());
        verify(mockCounter, never()).increment();
    }

    @Test
    void testGetNameSuggestions_NullPrefix() {
        // When
        List<String> result = searchCareGiverService.getNameSuggestions(null);

        // Then
        assertEquals(0, result.size());
        verify(careGiverRepository, never()).findNameSuggestions(anyString());
        verify(mockCounter, never()).increment();
    }

    @Test
    void testGetNameSuggestions_WhitespacePrefix() {
        // When
        List<String> result = searchCareGiverService.getNameSuggestions("  ");

        // Then
        assertEquals(0, result.size());
        verify(careGiverRepository, never()).findNameSuggestions(anyString());
        verify(mockCounter, never()).increment();
    }

    @Test
    void testGetNameSuggestions_ExactTwoCharacters() {
        // Given - test exact boundary of 2 characters (should be allowed)
        List<String> suggestions = Arrays.asList("Dr");
        when(careGiverRepository.findNameSuggestions("Dr")).thenReturn(suggestions);

        // When
        List<String> result = searchCareGiverService.getNameSuggestions("Dr");

        // Then
        assertEquals(1, result.size());
        assertEquals("Dr", result.get(0));
        verify(careGiverRepository).findNameSuggestions("Dr");
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testGetSpecialitySuggestions_ValidQuery() {
        // Given
        List<String> suggestions = Arrays.asList("Cardiology", "Pediatrics");
        when(careGiverRepository.findSpecialitySuggestions("Card")).thenReturn(suggestions);

        // When
        List<String> result = searchCareGiverService.getSpecialitySuggestions("Card");

        // Then
        assertEquals(2, result.size());
        assertEquals("Cardiology", result.get(0));
        verify(careGiverRepository).findSpecialitySuggestions("Card");
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testGetSpecialitySuggestions_ShortQuery() {
        // When
        List<String> result = searchCareGiverService.getSpecialitySuggestions("C");

        // Then
        assertEquals(0, result.size());
        verify(careGiverRepository, never()).findSpecialitySuggestions(anyString());
        verify(mockCounter, never()).increment();
    }

    @Test
    void testGetSpecialitySuggestions_NullQuery() {
        // When
        List<String> result = searchCareGiverService.getSpecialitySuggestions(null);

        // Then
        assertEquals(0, result.size());
        verify(careGiverRepository, never()).findSpecialitySuggestions(anyString());
        verify(mockCounter, never()).increment();
    }

    @Test
    void testGetSpecialitySuggestions_WhitespaceQuery() {
        // When
        List<String> result = searchCareGiverService.getSpecialitySuggestions("   ");

        // Then
        assertEquals(0, result.size());
        verify(careGiverRepository, never()).findSpecialitySuggestions(anyString());
        verify(mockCounter, never()).increment();
    }

    @Test
    void testGetSpecialitySuggestions_ExactTwoCharacters() {
        // Given - test exact boundary of 2 characters (should be allowed)
        List<String> suggestions = Arrays.asList("Cardiology");
        when(careGiverRepository.findSpecialitySuggestions("Ca")).thenReturn(suggestions);

        // When
        List<String> result = searchCareGiverService.getSpecialitySuggestions("Ca");

        // Then
        assertEquals(1, result.size());
        assertEquals("Cardiology", result.get(0));
        verify(careGiverRepository).findSpecialitySuggestions("Ca");
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testCreateLiteProfileResponse() throws Exception {
        // Given - use reflection to access private method
        java.lang.reflect.Method method = SearchCareGiverService.class
                .getDeclaredMethod("createLiteProfileResponse", CareGiver.class);
        method.setAccessible(true);

        // When
        ProfileResponse result = (ProfileResponse) method.invoke(searchCareGiverService, careGiver1);

        // Then
        assertEquals(1L, result.getId());
        assertEquals("dr.smith@example.com", result.getEmail());
        assertEquals("Dr. John Smith", result.getName());
        assertNull(result.getNik()); // Should be hidden for security
        assertNull(result.getAddress()); // Should be hidden for security
        assertEquals("123-456-7890", result.getPhoneNumber());
        assertEquals("CAREGIVER", result.getUserType());
        assertEquals("Cardiology", result.getSpeciality());
        assertEquals("123 Medical Center", result.getWorkAddress());
        assertEquals(4.5, result.getAverageRating());
    }

    @Test
    void testEmptyResultsHandling() throws ExecutionException, InterruptedException {
        // Given
        when(careGiverRepository.findCareGiversWithFilters(anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        // When
        CompletableFuture<List<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversOptimized("NonExistent", "InvalidSpeciality");

        // Then
        List<ProfileResponse> responses = result.get();
        assertEquals(0, responses.size());
        verify(mockCounter, atLeastOnce()).increment();
    }

    // ADDITIONAL TESTS FOR COMPLETE COVERAGE

    @Test
    void testSearchCareGiversPaginatedWithSort_EmptyStringDirection() throws ExecutionException, InterruptedException {
        // Given - test empty string direction (should default to ASC)
        Page<CareGiver> page = new PageImpl<>(careGivers, PageRequest.of(0, 10), 2);
        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversPaginatedWithSort(
                        null, null, 0, 10, "name", "");

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(2, responses.getTotalElements());
        verify(careGiverRepository).findAll(argThat((Pageable pageable1) ->
                pageable1.getSort().getOrderFor("name").getDirection() == Sort.Direction.ASC));
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversPaginatedWithSort_CaseInsensitiveDirection() throws ExecutionException, InterruptedException {
        // Given - test case insensitive direction matching
        Page<CareGiver> page = new PageImpl<>(careGivers, PageRequest.of(0, 10), 2);
        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When - test with mixed case "DESC"
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversPaginatedWithSort(
                        null, null, 0, 10, "name", "DeSc");

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(2, responses.getTotalElements());
        verify(careGiverRepository).findAll(argThat((Pageable pageable1) ->
                pageable1.getSort().getOrderFor("name").getDirection() == Sort.Direction.DESC));
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversPaginatedWithSort_CaseInsensitiveSortField() throws ExecutionException, InterruptedException {
        // Given - test case insensitive sort field matching
        Page<CareGiver> page = new PageImpl<>(careGivers, PageRequest.of(0, 10), 2);
        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When - test with mixed case sort field
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversPaginatedWithSort(
                        null, null, 0, 10, "SpEcIaLiTy", "asc");

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(2, responses.getTotalElements());
        verify(careGiverRepository).findAll(argThat((Pageable pageable1) ->
                pageable1.getSort().getOrderFor("speciality") != null));
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversPaginatedWithSort_WithWhitespaceFilters() throws ExecutionException, InterruptedException {
        // Given - test whitespace handling in filters
        Page<CareGiver> page = new PageImpl<>(careGivers, PageRequest.of(0, 10), 2);
        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversPaginatedWithSort(
                        "  ", "  ", 0, 10, "name", "asc");

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(2, responses.getTotalElements());
        // Should call findAll because whitespace filters are treated as null
        verify(careGiverRepository).findAll(any(Pageable.class));
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversPaginatedWithSort_WithTrimmedFilters() throws ExecutionException, InterruptedException {
        // Given - test that filters are properly trimmed
        Page<CareGiver> page = new PageImpl<>(List.of(careGiver1), PageRequest.of(0, 10), 1);
        when(careGiverRepository.findByNameContainingIgnoreCase(eq("John"), any(Pageable.class)))
                .thenReturn(page);

        // When - pass name with leading/trailing spaces
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversPaginatedWithSort(
                        "  John  ", null, 0, 10, "name", "asc");

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(1, responses.getTotalElements());
        // Verify that the trimmed name "John" was used
        verify(careGiverRepository).findByNameContainingIgnoreCase(eq("John"), any(Pageable.class));
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testGetNameSuggestions_WithLeadingTrailingSpaces() {
        // Given - test prefix with spaces that gets trimmed to valid length
        List<String> suggestions = Arrays.asList("Dr. John");
        when(careGiverRepository.findNameSuggestions("Dr")).thenReturn(suggestions);

        // When
        List<String> result = searchCareGiverService.getNameSuggestions("  Dr  ");

        // Then
        assertEquals(1, result.size());
        assertEquals("Dr. John", result.get(0));
        verify(careGiverRepository).findNameSuggestions("Dr");
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testGetSpecialitySuggestions_WithLeadingTrailingSpaces() {
        // Given - test query with spaces that gets trimmed to valid length
        List<String> suggestions = Arrays.asList("Cardiology");
        when(careGiverRepository.findSpecialitySuggestions("Card")).thenReturn(suggestions);

        // When
        List<String> result = searchCareGiverService.getSpecialitySuggestions("  Card  ");

        // Then
        assertEquals(1, result.size());
        assertEquals("Cardiology", result.get(0));
        verify(careGiverRepository).findSpecialitySuggestions("Card");
        verify(mockCounter, atLeastOnce()).increment();
    }

    // Test constructor for complete coverage
    @Test
    void testConstructor() {
        // Given
        CareGiverRepository mockRepository = mock(CareGiverRepository.class);
        MonitoringConfig mockMonitoringConfig = mock(MonitoringConfig.class);

        // When
        SearchCareGiverService service = new SearchCareGiverService(mockRepository, mockMonitoringConfig);

        // Then
        assertNotNull(service);
        // Verify the repository was injected correctly by testing a method call
        service.getNameSuggestions("a"); // Short prefix, won't call repository
        verify(mockRepository, never()).findNameSuggestions(anyString());
    }

    // Additional edge case tests for parameter validation
    @Test
    void testSearchCareGiversPaginated_ExactBoundaryConditions() throws ExecutionException, InterruptedException {
        // Test various boundary conditions for size parameter
        Page<CareGiver> page = new PageImpl<>(careGivers, PageRequest.of(0, 10), 2);
        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // Test size = 101 (should be corrected to 10)
        CompletableFuture<Page<ProfileResponse>> result1 =
                searchCareGiverService.searchCareGiversPaginated(null, null, 0, 101);
        Page<ProfileResponse> responses1 = result1.get();
        assertEquals(2, responses1.getTotalElements());

        reset(careGiverRepository);
        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // Test size = -1 (should be corrected to 10)
        CompletableFuture<Page<ProfileResponse>> result2 =
                searchCareGiverService.searchCareGiversPaginated(null, null, 0, -1);
        Page<ProfileResponse> responses2 = result2.get();
        assertEquals(2, responses2.getTotalElements());
    }

    @Test
    void testGetTopRatedCareGivers_BoundaryConditions() throws ExecutionException, InterruptedException {
        // Test various boundary conditions for size parameter in getTopRatedCareGivers
        Page<CareGiver> page = new PageImpl<>(careGivers, PageRequest.of(0, 10), 2);
        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // Test size = 51 (should be corrected to 10)
        CompletableFuture<Page<ProfileResponse>> result1 =
                searchCareGiverService.getTopRatedCareGivers(0, 51);
        Page<ProfileResponse> responses1 = result1.get();
        assertEquals(2, responses1.getTotalElements());

        reset(careGiverRepository);
        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // Test size = -5 (should be corrected to 10)
        CompletableFuture<Page<ProfileResponse>> result2 =
                searchCareGiverService.getTopRatedCareGivers(0, -5);
        Page<ProfileResponse> responses2 = result2.get();
        assertEquals(2, responses2.getTotalElements());
    }

    @Test
    void testSearchCareGiversPaginatedWithSort_SizeExactly100() throws ExecutionException, InterruptedException {
        // Given - test size == 100 (should be allowed, not corrected to 10)
        Page<CareGiver> page = new PageImpl<>(careGivers, PageRequest.of(0, 100), 2);
        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversPaginatedWithSort(
                        null, null, 0, 100, "name", "asc");

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(2, responses.getTotalElements());
        // Verify that size 100 is allowed (not corrected to 10)
        verify(careGiverRepository).findAll(argThat((Pageable pageable1) ->
                pageable1.getPageSize() == 100));
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversPaginatedWithSort_SizeExactly101() throws ExecutionException, InterruptedException {
        // Given - test size > 100 (should be corrected to 10)
        Page<CareGiver> page = new PageImpl<>(careGivers, PageRequest.of(0, 10), 2);
        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversPaginatedWithSort(
                        null, null, 0, 101, "name", "asc");

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(2, responses.getTotalElements());
        // Verify that size 101 was corrected to 10
        verify(careGiverRepository).findAll(argThat((Pageable pageable1) ->
                pageable1.getPageSize() == 10));
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversPaginatedWithSort_ForLoopAllFields() throws ExecutionException, InterruptedException {
        // Test that the for loop checks all fields and breaks when found
        String[] fieldsToTest = {"name", "speciality", "averageRating", "ratingCount"};

        for (String field : fieldsToTest) {
            // Given
            Page<CareGiver> page = new PageImpl<>(careGivers, PageRequest.of(0, 10), 2);
            when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

            // When
            CompletableFuture<Page<ProfileResponse>> result =
                    searchCareGiverService.searchCareGiversPaginatedWithSort(
                            null, null, 0, 10, field, "asc");

            // Then
            Page<ProfileResponse> responses = result.get();
            assertEquals(2, responses.getTotalElements());
            verify(careGiverRepository).findAll(argThat((Pageable pageable1) ->
                    pageable1.getSort().getOrderFor(field) != null));

            // Reset mock for next iteration
            reset(careGiverRepository);
        }
    }

    @Test
    void testSearchCareGiversPaginatedWithSort_ForLoopNoMatch() throws ExecutionException, InterruptedException {
        // Given - test when sortBy doesn't match any allowed field (should use default "averageRating")
        Page<CareGiver> page = new PageImpl<>(careGivers, PageRequest.of(0, 10), 2);
        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When - use a field that doesn't match any in the allowed array
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversPaginatedWithSort(
                        null, null, 0, 10, "nonExistentField", "asc");

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(2, responses.getTotalElements());
        // Should use default "averageRating" since no match was found
        verify(careGiverRepository).findAll(argThat((Pageable pageable1) ->
                pageable1.getSort().getOrderFor("averageRating") != null));
        verify(mockCounter, atLeastOnce()).increment();
    }

    @Test
    void testSearchCareGiversPaginatedWithSort_SizeZero() throws ExecutionException, InterruptedException {
        // Given - test size == 0 (should be corrected to 10)
        Page<CareGiver> page = new PageImpl<>(careGivers, PageRequest.of(0, 10), 2);
        when(careGiverRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        CompletableFuture<Page<ProfileResponse>> result =
                searchCareGiverService.searchCareGiversPaginatedWithSort(
                        null, null, 0, 0, "name", "asc");

        // Then
        Page<ProfileResponse> responses = result.get();
        assertEquals(2, responses.getTotalElements());
        verify(careGiverRepository).findAll(argThat((Pageable pageable1) ->
                pageable1.getPageSize() == 10));
        verify(mockCounter, atLeastOnce()).increment();
    }
}