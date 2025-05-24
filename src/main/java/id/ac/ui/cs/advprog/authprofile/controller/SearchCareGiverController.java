package id.ac.ui.cs.advprog.authprofile.controller;

import id.ac.ui.cs.advprog.authprofile.dto.response.ProfileResponse;
import id.ac.ui.cs.advprog.authprofile.service.SearchCareGiverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class SearchCareGiverController {

    private static final Logger logger = LoggerFactory.getLogger(SearchCareGiverController.class);
    private final SearchCareGiverService searchCareGiverService;

    @Autowired
    public SearchCareGiverController(SearchCareGiverService searchCareGiverService) {
        this.searchCareGiverService = searchCareGiverService;
    }

    /**
     * ASYNC: Optimized search endpoint (non-paginated)
     */
    @GetMapping("/caregiver/search-optimized")
    public CompletableFuture<ResponseEntity<List<ProfileResponse>>> searchCareGiversOptimized(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String speciality) {

        logger.debug("Received async search request - name: {}, speciality: {}", name, speciality);

        return searchCareGiverService.searchCareGiversOptimized(name, speciality)
                .thenApply(results -> {
                    logger.debug("Search completed with {} results", results.size());
                    return ResponseEntity.ok(results);
                })
                .exceptionally(ex -> {
                    logger.error("Error during caregiver search", ex);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                });
    }

    /**
     * SYNC: Name autocomplete endpoint (cached, so fast)
     */
    @GetMapping("/caregiver/suggestions/names")
    public ResponseEntity<List<String>> getNameSuggestions(@RequestParam String prefix) {
        try {
            // Validate input
            if (prefix == null || prefix.trim().length() < 2) {
                logger.warn("Invalid name suggestion request - prefix too short: {}", prefix);
                return ResponseEntity.badRequest().build();
            }

            logger.debug("Getting name suggestions for: {}", prefix);
            List<String> suggestions = searchCareGiverService.getNameSuggestions(prefix);

            logger.debug("Found {} name suggestions", suggestions.size());
            return ResponseEntity.ok(suggestions);

        } catch (Exception ex) {
            logger.error("Error getting name suggestions for prefix: {}", prefix, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * SYNC: Speciality autocomplete endpoint (cached, so fast)
     */
    @GetMapping("/caregiver/suggestions/specialities")
    public ResponseEntity<List<String>> getSpecialitySuggestions(@RequestParam String query) {
        try {
            // Validate input
            if (query == null || query.trim().length() < 2) {
                logger.warn("Invalid speciality suggestion request - query too short: {}", query);
                return ResponseEntity.badRequest().build();
            }

            logger.debug("Getting speciality suggestions for: {}", query);
            List<String> suggestions = searchCareGiverService.getSpecialitySuggestions(query);

            logger.debug("Found {} speciality suggestions", suggestions.size());
            return ResponseEntity.ok(suggestions);

        } catch (Exception ex) {
            logger.error("Error getting speciality suggestions for query: {}", query, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ASYNC: Basic paginated search endpoint
     */
    @GetMapping("/caregiver/search-paginated")
    public CompletableFuture<ResponseEntity<Page<ProfileResponse>>> searchCareGiversPaginated(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String speciality,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.debug("Received paginated search request - name: {}, speciality: {}, page: {}, size: {}",
                name, speciality, page, size);

        return searchCareGiverService.searchCareGiversPaginated(name, speciality, page, size)
                .thenApply(results -> {
                    logger.debug("Paginated search completed - total elements: {}, total pages: {}",
                            results.getTotalElements(), results.getTotalPages());
                    return ResponseEntity.ok(results);
                })
                .exceptionally(ex -> {
                    logger.error("Error during paginated caregiver search", ex);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                });
    }

    /**
     * ASYNC: Advanced paginated search endpoint with custom sorting
     */
    @GetMapping("/caregiver/search-advanced")
    public CompletableFuture<ResponseEntity<Page<ProfileResponse>>> searchCareGiversAdvanced(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String speciality,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "averageRating") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        // Validate sort parameters before making async call
        if (!isValidSortField(sortBy)) {
            logger.warn("Invalid sort field requested: {}", sortBy);
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().build()
            );
        }

        if (!isValidSortDirection(sortDirection)) {
            logger.warn("Invalid sort direction requested: {}", sortDirection);
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().build()
            );
        }

        logger.debug("Received advanced search request - name: {}, speciality: {}, page: {}, size: {}, sortBy: {}, sortDirection: {}",
                name, speciality, page, size, sortBy, sortDirection);

        return searchCareGiverService.searchCareGiversPaginatedWithSort(
                        name, speciality, page, size, sortBy, sortDirection)
                .thenApply(results -> {
                    logger.debug("Advanced search completed - total elements: {}", results.getTotalElements());
                    return ResponseEntity.ok(results);
                })
                .exceptionally(ex -> {
                    logger.error("Error during advanced caregiver search", ex);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                });
    }

    /**
     * ASYNC: Get top-rated caregivers
     */
    @GetMapping("/caregiver/top-rated")
    public CompletableFuture<ResponseEntity<Page<ProfileResponse>>> getTopRatedCareGivers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.debug("Received top-rated caregivers request - page: {}, size: {}", page, size);

        return searchCareGiverService.getTopRatedCareGivers(page, size)
                .thenApply(results -> {
                    logger.debug("Top-rated search completed - total elements: {}", results.getTotalElements());
                    return ResponseEntity.ok(results);
                })
                .exceptionally(ex -> {
                    logger.error("Error getting top-rated caregivers", ex);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                });
    }

    // Helper methods for validation
    private boolean isValidSortField(String sortBy) {
        return sortBy != null &&
                java.util.List.of("name", "speciality", "averageRating", "ratingCount").contains(sortBy);
    }

    private boolean isValidSortDirection(String sortDirection) {
        return sortDirection != null &&
                java.util.List.of("asc", "desc").contains(sortDirection.toLowerCase());
    }
}