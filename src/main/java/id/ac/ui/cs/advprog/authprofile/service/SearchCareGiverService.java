package id.ac.ui.cs.advprog.authprofile.service;

import id.ac.ui.cs.advprog.authprofile.dto.response.ProfileResponse;
import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.repository.CareGiverRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class SearchCareGiverService {

    private static final Logger logger = LoggerFactory.getLogger(SearchCareGiverService.class);
    private final CareGiverRepository careGiverRepository;

    @Autowired
    public SearchCareGiverService(CareGiverRepository careGiverRepository) {
        this.careGiverRepository = careGiverRepository;
    }

    /**
     * ASYNC: Main search operations for high load handling
     */
    @Async("searchTaskExecutor")
    public CompletableFuture<List<ProfileResponse>> searchCareGiversOptimized(String name, String speciality) {
        logger.debug("Starting async search - name: {}, speciality: {}", name, speciality);

        List<CareGiver> careGivers = careGiverRepository.findCareGiversWithFilters(
                (name != null && !name.trim().isEmpty()) ? name.trim() : null,
                (speciality != null && !speciality.trim().isEmpty()) ? speciality.trim() : null
        );

        List<ProfileResponse> results = careGivers.stream()
                .map(this::createLiteProfileResponse)
                .collect(Collectors.toList());

        logger.debug("Async search completed with {} results", results.size());
        return CompletableFuture.completedFuture(results);
    }

    @Async("searchTaskExecutor")
    public CompletableFuture<Page<ProfileResponse>> searchCareGiversPaginated(
            String name, String speciality, int page, int size) {

        logger.debug("Starting async paginated search");

        // Validate parameters
        int validPage = Math.max(0, page);
        int validSize = (size <= 0 || size > 100) ? 10 : size;

        Pageable pageable = PageRequest.of(validPage, validSize,
                Sort.by(Sort.Direction.DESC, "averageRating")
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
        return CompletableFuture.completedFuture(results);
    }

    /**
     * SYNC: Cached autocomplete operations (fast after first call)
     */
    @Cacheable(value = "nameSuggestions", key = "#prefix")
    public List<String> getNameSuggestions(String prefix) {
        logger.debug("Getting name suggestions for: {}", prefix);

        if (prefix == null || prefix.trim().length() < 2) {
            return List.of();
        }

        List<String> results = careGiverRepository.findNameSuggestions(prefix.trim());
        logger.debug("Found {} name suggestions", results.size());
        return results;
    }

    @Cacheable(value = "specialitySuggestions", key = "#query")
    public List<String> getSpecialitySuggestions(String query) {
        logger.debug("Getting speciality suggestions for: {}", query);

        if (query == null || query.trim().length() < 2) {
            return List.of();
        }

        List<String> results = careGiverRepository.findSpecialitySuggestions(query.trim());
        logger.debug("Found {} speciality suggestions", results.size());
        return results;
    }

    /**
     * ASYNC: Advanced search with sorting (heavy operation)
     */
    @Async("searchTaskExecutor")
    public CompletableFuture<Page<ProfileResponse>> searchCareGiversPaginatedWithSort(
            String name, String speciality, int page, int size, String sortBy, String sortDirection) {

        logger.debug("Starting async advanced search with sorting");

        // Parameter validation and processing (same as before)
        int validPage = Math.max(0, page);
        int validSize = (size <= 0 || size > 100) ? 10 : size;

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String[] allowedSortFields = {"name", "speciality", "averageRating", "ratingCount"};
        String validSortBy = "averageRating";

        if (sortBy != null) {
            for (String field : allowedSortFields) {
                if (field.equalsIgnoreCase(sortBy)) {
                    validSortBy = field;
                    break;
                }
            }
        }

        Pageable pageable = PageRequest.of(validPage, validSize, Sort.by(direction, validSortBy));

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

        logger.debug("Async advanced search completed");
        return CompletableFuture.completedFuture(results);
    }

    /**
     * ASYNC: Get top-rated caregivers with pagination
     */
    @Async("searchTaskExecutor")
    public CompletableFuture<Page<ProfileResponse>> getTopRatedCareGivers(int page, int size) {
        logger.debug("Starting async top-rated caregivers search");

        int validPage = Math.max(0, page);
        int validSize = (size <= 0 || size > 50) ? 10 : size;

        Pageable pageable = PageRequest.of(validPage, validSize,
                Sort.by(Sort.Direction.DESC, "averageRating")
                        .and(Sort.by(Sort.Direction.DESC, "ratingCount")));

        Page<CareGiver> careGiversPage = careGiverRepository.findAll(pageable);
        Page<ProfileResponse> results = careGiversPage.map(this::createLiteProfileResponse);

        logger.debug("Async top-rated search completed - {} total elements", results.getTotalElements());
        return CompletableFuture.completedFuture(results);
    }

    private ProfileResponse createLiteProfileResponse(CareGiver careGiver) {
        ProfileResponse response = new ProfileResponse();
        response.setId(careGiver.getId());
        response.setEmail(careGiver.getEmail());
        response.setName(careGiver.getName());
        response.setNik(null);
        response.setAddress(null);
        response.setPhoneNumber(careGiver.getPhoneNumber());
        response.setUserType("CAREGIVER");
        response.setSpeciality(careGiver.getSpeciality());
        response.setWorkAddress(careGiver.getWorkAddress());
        response.setAverageRating(careGiver.getAverageRating());
        return response;
    }
}