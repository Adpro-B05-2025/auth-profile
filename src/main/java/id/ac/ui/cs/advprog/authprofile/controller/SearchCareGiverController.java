package id.ac.ui.cs.advprog.authprofile.controller;

import id.ac.ui.cs.advprog.authprofile.dto.response.ProfileResponse;
import id.ac.ui.cs.advprog.authprofile.service.SearchCareGiverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class SearchCareGiverController {

    private final SearchCareGiverService searchCareGiverService;

    @Autowired
    public SearchCareGiverController(SearchCareGiverService searchCareGiverService) {
        this.searchCareGiverService = searchCareGiverService;
    }

    /**
     * Optimized search endpoint (non-paginated)
     */
    @GetMapping("/caregiver/search-optimized")
    public ResponseEntity<List<ProfileResponse>> searchCareGiversOptimized(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String speciality) {

        List<ProfileResponse> careGivers = searchCareGiverService.searchCareGiversOptimized(name, speciality);
        return ResponseEntity.ok(careGivers);
    }

    /**
     * Name autocomplete endpoint
     */
    @GetMapping("/caregiver/suggestions/names")
    public ResponseEntity<List<String>> getNameSuggestions(
            @RequestParam String prefix) {

        List<String> suggestions = searchCareGiverService.getNameSuggestions(prefix);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * Speciality autocomplete endpoint
     */
    @GetMapping("/caregiver/suggestions/specialities")
    public ResponseEntity<List<String>> getSpecialitySuggestions(
            @RequestParam String query) {

        List<String> suggestions = searchCareGiverService.getSpecialitySuggestions(query);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * Basic paginated search endpoint
     */
    @GetMapping("/caregiver/search-paginated")
    public ResponseEntity<Page<ProfileResponse>> searchCareGiversPaginated(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String speciality,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ProfileResponse> careGivers = searchCareGiverService.searchCareGiversPaginated(
                name, speciality, page, size);
        return ResponseEntity.ok(careGivers);
    }

    /**
     * Advanced paginated search endpoint with custom sorting
     */
    @GetMapping("/caregiver/search-advanced")
    public ResponseEntity<Page<ProfileResponse>> searchCareGiversAdvanced(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String speciality,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "averageRating") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Page<ProfileResponse> careGivers = searchCareGiverService.searchCareGiversPaginatedWithSort(
                name, speciality, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(careGivers);
    }

    /**
     * Get top-rated caregivers
     */
    @GetMapping("/caregiver/top-rated")
    public ResponseEntity<Page<ProfileResponse>> getTopRatedCareGivers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ProfileResponse> careGivers = searchCareGiverService.getTopRatedCareGivers(page, size);
        return ResponseEntity.ok(careGivers);
    }


}