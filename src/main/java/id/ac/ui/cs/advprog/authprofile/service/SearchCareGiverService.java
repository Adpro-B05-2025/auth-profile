package id.ac.ui.cs.advprog.authprofile.service;

import id.ac.ui.cs.advprog.authprofile.config.MonitoringConfig;
import id.ac.ui.cs.advprog.authprofile.dto.response.ProfileResponse;
import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.repository.CareGiverRepository;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class SearchCareGiverService {

    private static final Logger logger = LoggerFactory.getLogger(SearchCareGiverService.class);
    private static final String SEARCH_REQUESTS_TOTAL_METRIC = "search_requests_total";
    private static final String SEARCH_CAREGIVERS_RESULTS_TOTAL_METRIC = "search_caregivers_results_total";
    private static final String PAGINATED_TYPE = "paginated";
    private static final String AVERAGE_RATING_FIELD = "averageRating";

    private final CareGiverRepository careGiverRepository;
    private final MonitoringConfig monitoringConfig;

    @Autowired
    public SearchCareGiverService(CareGiverRepository careGiverRepository,
                                  MonitoringConfig monitoringConfig) {
        this.careGiverRepository = careGiverRepository;
        this.monitoringConfig = monitoringConfig;
    }

    /**
     * ASYNC: Main search operations for high load handling
     */
    @Async("searchTaskExecutor")
    @Timed(value = "search_caregivers_optimized_duration", description = "Time taken for optimized caregiver search")
    public CompletableFuture<List<ProfileResponse>> searchCareGiversOptimized(String name, String speciality) {
        logger.debug("Starting async search - name: {}, speciality: {}", name, speciality);

        // Use the registered search requests counter with proper tags
        monitoringConfig.meterRegistry.counter(SEARCH_REQUESTS_TOTAL_METRIC,
                Tags.of(
                        "type", "optimized",
                        "hasName", String.valueOf(name != null && !name.trim().isEmpty()),
                        "hasSpeciality", String.valueOf(speciality != null && !speciality.trim().isEmpty())
                )).increment();

        List<CareGiver> careGivers = careGiverRepository.findCareGiversWithFilters(
                (name != null && !name.trim().isEmpty()) ? name.trim() : null,
                (speciality != null && !speciality.trim().isEmpty()) ? speciality.trim() : null
        );

        List<ProfileResponse> results = careGivers.stream()
                .map(this::createLiteProfileResponse)
                .toList();

        logger.debug("Async search completed with {} results", results.size());

        // Record search result count using the registered counter
        monitoringConfig.meterRegistry.counter(SEARCH_CAREGIVERS_RESULTS_TOTAL_METRIC,
                Tags.of("type", "optimized")).increment(results.size());

        return CompletableFuture.completedFuture(results);
    }

    @Async("searchTaskExecutor")
    @Timed(value = "search_caregivers_paginated_duration", description = "Time taken for paginated caregiver search")
    public CompletableFuture<Page<ProfileResponse>> searchCareGiversPaginated(
            String name, String speciality, int page, int size) {

        logger.debug("Starting async paginated search");

        // Use the registered search requests counter
        monitoringConfig.meterRegistry.counter(SEARCH_REQUESTS_TOTAL_METRIC,
                Tags.of(
                        "type", PAGINATED_TYPE,
                        "hasName", String.valueOf(name != null && !name.trim().isEmpty()),
                        "hasSpeciality", String.valueOf(speciality != null && !speciality.trim().isEmpty())
                )).increment();

        // Validate parameters
        int validPage = Math.max(0, page);
        int validSize = (size <= 0 || size > 100) ? 10 : size;

        Pageable pageable = PageRequest.of(validPage, validSize,
                Sort.by(Sort.Direction.DESC, AVERAGE_RATING_FIELD)
                        .and(Sort.by(Sort.Direction.ASC, "name")));

        String cleanName = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
        String cleanSpeciality = (speciality != null && !speciality.trim().isEmpty()) ? speciality.trim() : null;

        Page<CareGiver> careGiversPage;
        if (cleanName != null && cleanSpeciality != null) {
            careGiversPage = careGiverRepository.findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(
                    cleanName, cleanSpeciality, pageable);
        } else if (cleanName != null) {
            careGiversPage = careGiverRepository.findByNameContainingIgnoreCase(cleanName, pageable);
        } else if (cleanSpeciality != null) {
            careGiversPage = careGiverRepository.findBySpecialityContainingIgnoreCase(cleanSpeciality, pageable);
        } else {
            careGiversPage = careGiverRepository.findAll(pageable);
        }

        Page<ProfileResponse> results = careGiversPage.map(this::createLiteProfileResponse);

        logger.debug("Async paginated search completed - {} total elements", results.getTotalElements());

        // Record pagination metrics
        monitoringConfig.meterRegistry.counter(SEARCH_CAREGIVERS_RESULTS_TOTAL_METRIC,
                Tags.of("type", PAGINATED_TYPE)).increment(results.getNumberOfElements());

        monitoringConfig.meterRegistry.gauge("search_caregivers_total_elements",
                Tags.of("type", PAGINATED_TYPE), results.getTotalElements());

        return CompletableFuture.completedFuture(results);
    }

    /**
     * SYNC: Cached autocomplete operations (fast after first call)
     */
    @Cacheable(value = "nameSuggestions", key = "#prefix")
    @Timed(value = "search_suggestions_name_duration", description = "Time taken to get name suggestions")
    public List<String> getNameSuggestions(String prefix) {
        logger.debug("Getting name suggestions for: {}", prefix);

        if (prefix == null || prefix.trim().length() < 2) {
            return List.of();
        }

        // Use the registered search requests counter
        monitoringConfig.meterRegistry.counter("search_suggestions_requests_total",
                Tags.of("type", "name_suggestions")
        ).increment();

        List<String> results = careGiverRepository.findNameSuggestions(prefix.trim());
        logger.debug("Found {} name suggestions", results.size());

        // Record suggestion result count
        monitoringConfig.meterRegistry.counter("search_suggestions_results_total",
                Tags.of("type", "name")).increment(results.size());

        return results;
    }

    @Cacheable(value = "specialitySuggestions", key = "#query")
    @Timed(value = "search_suggestions_speciality_duration", description = "Time taken to get speciality suggestions")
    public List<String> getSpecialitySuggestions(String query) {
        logger.debug("Getting speciality suggestions for: {}", query);

        if (query == null || query.trim().length() < 2) {
            return List.of();
        }

        // Use the registered search requests counter
        monitoringConfig.meterRegistry.counter("search_suggestions_requests_total",
                Tags.of("type", "speciality_suggestions")
        ).increment();

        List<String> results = careGiverRepository.findSpecialitySuggestions(query.trim());
        logger.debug("Found {} speciality suggestions", results.size());

        // Record suggestion result count
        monitoringConfig.meterRegistry.counter("search_suggestions_results_total",
                Tags.of("type", "speciality")).increment(results.size());

        return results;
    }

    /**
     * ASYNC: Advanced search with sorting (heavy operation)
     */
    @Async("searchTaskExecutor")
    @Timed(value = "search_caregivers_advanced_duration", description = "Time taken for advanced caregiver search")
    public CompletableFuture<Page<ProfileResponse>> searchCareGiversPaginatedWithSort(
            String name, String speciality, int page, int size, String sortBy, String sortDirection) {

        logger.debug("Starting async advanced search with sorting");

        // Record search request metrics
        recordAdvancedSearchMetrics(sortBy, sortDirection);

        // Validate and process parameters
        SearchParameters params = validateAndProcessParameters(page, size, sortBy, sortDirection);

        // Create pageable with validated parameters
        Pageable pageable = PageRequest.of(params.validPage(), params.validSize(),
                Sort.by(params.direction(), params.validSortBy()));

        // Clean input parameters
        String cleanName = cleanString(name);
        String cleanSpeciality = cleanString(speciality);

        // Execute search based on parameters
        Page<CareGiver> careGiversPage = executeAdvancedSearch(cleanName, cleanSpeciality, pageable);

        // Map results and record metrics
        Page<ProfileResponse> results = careGiversPage.map(this::createLiteProfileResponse);

        logger.debug("Async advanced search completed");

        // Record advanced search metrics
        monitoringConfig.meterRegistry.counter(SEARCH_CAREGIVERS_RESULTS_TOTAL_METRIC,
                Tags.of("type", "advanced")).increment(results.getNumberOfElements());

        return CompletableFuture.completedFuture(results);
    }

    /**
     * Records metrics for advanced search requests
     */
    private void recordAdvancedSearchMetrics(String sortBy, String sortDirection) {
        monitoringConfig.meterRegistry.counter(SEARCH_REQUESTS_TOTAL_METRIC,
                Tags.of(
                        "type", "advanced",
                        "sortBy", sortBy != null ? sortBy : "default",
                        "sortDirection", sortDirection != null ? sortDirection : "default"
                )).increment();
    }

    /**
     * Validates and processes search parameters
     */
    private SearchParameters validateAndProcessParameters(int page, int size, String sortBy, String sortDirection) {
        int validPage = Math.max(0, page);
        int validSize = (size <= 0 || size > 100) ? 10 : size;

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String validSortBy = validateSortField(sortBy);

        return new SearchParameters(validPage, validSize, direction, validSortBy);
    }

    /**
     * Validates sort field against allowed fields
     */
    private String validateSortField(String sortBy) {
        if (sortBy == null) {
            return AVERAGE_RATING_FIELD;
        }

        String[] allowedSortFields = {"name", "speciality", AVERAGE_RATING_FIELD, "ratingCount"};
        for (String field : allowedSortFields) {
            if (field.equalsIgnoreCase(sortBy)) {
                return field;
            }
        }
        return AVERAGE_RATING_FIELD;
    }

    /**
     * Cleans string input parameters
     */
    private String cleanString(String input) {
        return (input != null && !input.trim().isEmpty()) ? input.trim() : null;
    }

    /**
     * Executes the advanced search based on cleaned parameters
     */
    private Page<CareGiver> executeAdvancedSearch(String cleanName, String cleanSpeciality, Pageable pageable) {
        if (cleanName != null && cleanSpeciality != null) {
            return careGiverRepository.findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(
                    cleanName, cleanSpeciality, pageable);
        } else if (cleanName != null) {
            return careGiverRepository.findByNameContainingIgnoreCase(cleanName, pageable);
        } else if (cleanSpeciality != null) {
            return careGiverRepository.findBySpecialityContainingIgnoreCase(cleanSpeciality, pageable);
        } else {
            return careGiverRepository.findAll(pageable);
        }
    }

    /**
     * ASYNC: Get top-rated caregivers with pagination
     */
    @Async("searchTaskExecutor")
    @Timed(value = "search_caregivers_toprated_duration", description = "Time taken to get top-rated caregivers")
    public CompletableFuture<Page<ProfileResponse>> getTopRatedCareGivers(int page, int size) {
        logger.debug("Starting async top-rated caregivers search");

        // Use the registered search requests counter
        monitoringConfig.meterRegistry.counter(SEARCH_REQUESTS_TOTAL_METRIC,
                Tags.of("type", "toprated")
        ).increment();

        int validPage = Math.max(0, page);
        int validSize = (size <= 0 || size > 50) ? 10 : size;

        Pageable pageable = PageRequest.of(validPage, validSize,
                Sort.by(Sort.Direction.DESC, AVERAGE_RATING_FIELD)
                        .and(Sort.by(Sort.Direction.DESC, "ratingCount")));

        Page<CareGiver> careGiversPage = careGiverRepository.findAll(pageable);
        Page<ProfileResponse> results = careGiversPage.map(this::createLiteProfileResponse);

        logger.debug("Async top-rated search completed - {} total elements", results.getTotalElements());

        // Record top-rated search metrics
        monitoringConfig.meterRegistry.counter(SEARCH_CAREGIVERS_RESULTS_TOTAL_METRIC,
                Tags.of("type", "toprated")).increment(results.getNumberOfElements());

        return CompletableFuture.completedFuture(results);
    }

    /**
     * Create a lite version of ProfileResponse with essential information only
     */
    private ProfileResponse createLiteProfileResponse(CareGiver careGiver) {
        ProfileResponse response = new ProfileResponse();
        response.setId(careGiver.getId());
        response.setEmail(careGiver.getEmail());
        response.setName(careGiver.getName());
        response.setNik(null); // Hide for privacy
        response.setAddress(null); // Hide for privacy
        response.setPhoneNumber(careGiver.getPhoneNumber());
        response.setUserType("CAREGIVER");
        response.setSpeciality(careGiver.getSpeciality());
        response.setWorkAddress(careGiver.getWorkAddress());
        response.setAverageRating(careGiver.getAverageRating());
        return response;
    }

    /**
     * Record to hold validated search parameters
     */
    private record SearchParameters(int validPage, int validSize, Sort.Direction direction, String validSortBy) {}
}