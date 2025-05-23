package id.ac.ui.cs.advprog.authprofile.service;

import id.ac.ui.cs.advprog.authprofile.dto.response.ProfileResponse;
import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.repository.CareGiverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchCareGiverService {

    private final CareGiverRepository careGiverRepository;

    @Autowired
    public SearchCareGiverService(CareGiverRepository careGiverRepository) {
        this.careGiverRepository = careGiverRepository;
    }

    /**
     * Enhanced search with better performance
     */
    public List<ProfileResponse> searchCareGiversOptimized(String name, String speciality) {
        // Use the optimized repository method
        List<CareGiver> careGivers = careGiverRepository.findCareGiversWithFilters(
                (name != null && !name.trim().isEmpty()) ? name.trim() : null,
                (speciality != null && !speciality.trim().isEmpty()) ? speciality.trim() : null
        );

        return careGivers.stream()
                .map(this::createLiteProfileResponse)
                .collect(Collectors.toList());
    }

    public List<ProfileResponse> getAllCareGiversLite() {
        List<CareGiver> careGivers = careGiverRepository.findAll();
        return careGivers.stream()
                .map(this::createLiteProfileResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get autocomplete suggestions for names
     */
    @Cacheable(value = "nameSuggestions", key = "#prefix")
    public List<String> getNameSuggestions(String prefix) {
        if (prefix == null || prefix.trim().length() < 2) {
            return List.of(); // Don't suggest for very short inputs
        }
        return careGiverRepository.findNameSuggestions(prefix.trim());
    }

    /**
     * Get autocomplete suggestions for specialities
     */
    @Cacheable(value = "specialitySuggestions", key = "#query")
    public List<String> getSpecialitySuggestions(String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        return careGiverRepository.findSpecialitySuggestions(query.trim());
    }

    public Page<ProfileResponse> searchCareGiversPaginated(String name, String speciality, int page, int size) {
        // Validate pagination parameters
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        // Create pageable with sorting
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "averageRating")
                        .and(Sort.by(Sort.Direction.ASC, "name")));

        // Clean filter parameters
        String cleanName = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
        String cleanSpeciality = (speciality != null && !speciality.trim().isEmpty()) ? speciality.trim() : null;

        // Use appropriate repository method based on filters
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

        return careGiversPage.map(this::createLiteProfileResponse);
    }

    public Page<ProfileResponse> searchCareGiversPaginatedWithSort(String name, String speciality,
                                                                   int page, int size,
                                                                   String sortBy, String sortDirection) {
        // Validate pagination parameters
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        // Validate sorting parameters
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

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, validSortBy));

        // Clean filter parameters
        String cleanName = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
        String cleanSpeciality = (speciality != null && !speciality.trim().isEmpty()) ? speciality.trim() : null;

        // Use appropriate repository method based on filters
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

        return careGiversPage.map(this::createLiteProfileResponse);
    }

    /**
     * Get top-rated caregivers with pagination
     */
    public Page<ProfileResponse> getTopRatedCareGivers(int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 50) size = 10;

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "averageRating")
                        .and(Sort.by(Sort.Direction.DESC, "ratingCount")));

        Page<CareGiver> careGiversPage = careGiverRepository.findAll(pageable);
        return careGiversPage.map(this::createLiteProfileResponse);
    }

    private ProfileResponse createLiteProfileResponse(CareGiver careGiver) {
        ProfileResponse response = new ProfileResponse();
        response.setId(careGiver.getId());
        response.setEmail(careGiver.getEmail());
        response.setName(careGiver.getName());
        response.setNik(null); // Hidden for security
        response.setAddress(null); // Hidden for security
        response.setPhoneNumber(careGiver.getPhoneNumber());
        response.setUserType("CAREGIVER");
        response.setSpeciality(careGiver.getSpeciality());
        response.setWorkAddress(careGiver.getWorkAddress());
        response.setAverageRating(careGiver.getAverageRating());
        return response;
    }
}