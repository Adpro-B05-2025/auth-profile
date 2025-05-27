package id.ac.ui.cs.advprog.authprofile.service;

import id.ac.ui.cs.advprog.authprofile.client.RatingClientService;
import id.ac.ui.cs.advprog.authprofile.dto.response.RatingResponseDto;
import id.ac.ui.cs.advprog.authprofile.config.MonitoringConfig;
import id.ac.ui.cs.advprog.authprofile.dto.request.UpdateProfileRequest;
import id.ac.ui.cs.advprog.authprofile.dto.response.ProfileResponse;
import id.ac.ui.cs.advprog.authprofile.dto.response.RatingSummaryResponse;
import id.ac.ui.cs.advprog.authprofile.exception.EmailAlreadyExistsException;
import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.repository.CareGiverRepository;
import id.ac.ui.cs.advprog.authprofile.repository.PacillianRepository;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import id.ac.ui.cs.advprog.authprofile.security.jwt.JwtUtils;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Service
public class ProfileServiceImpl implements IProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileServiceImpl.class);
    private static final String USER_TYPE_TAG = "userType";
    private static final String EMAIL_CHANGED_TAG = "emailChanged";
    private static final String REASON_TAG = "reason";
    private static final String USER_NOT_FOUND_MESSAGE = "User not found";

    private final UserRepository userRepository;
    private final PacillianRepository pacillianRepository;
    private final CareGiverRepository careGiverRepository;
    private final JwtUtils jwtUtils;
    private final RatingClientService ratingClientService;
    private final MonitoringConfig monitoringConfig;
    private final IRatingService ratingService;

    @Autowired
    public ProfileServiceImpl(
            UserRepository userRepository,
            PacillianRepository pacillianRepository,
            CareGiverRepository careGiverRepository,
            JwtUtils jwtUtils,
            RatingClientService ratingClientService,
            MonitoringConfig monitoringConfig, IRatingService ratingService) {

        this.userRepository = userRepository;
        this.pacillianRepository = pacillianRepository;
        this.careGiverRepository = careGiverRepository;
        this.jwtUtils = jwtUtils;
        this.ratingClientService = ratingClientService;
        this.monitoringConfig = monitoringConfig;
        this.ratingService = ratingService;
    }

    @Override
    @Timed(value = "profile_get_current_duration", description = "Time taken to get current user profile")
    public ProfileResponse getCurrentUserProfile() {
        logger.debug("Getting current user profile");

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Get the current user by ID instead of by email
        Long userId = Long.parseLong(userDetails.getUsername());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE));

        logger.debug("Retrieved profile for user: {}", user.getEmail());
        return ProfileResponse.fromUser(user);
    }

    @Override
    @Timed(value = "profile_get_by_id_duration", description = "Time taken to get user profile by ID")
    public ProfileResponse getUserProfile(Long userId) {
        logger.debug("Getting user profile for ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        ProfileResponse profile = ProfileResponse.fromUser(user);

        // Enhanced rating integration for caregivers
        if (user instanceof CareGiver caregiver) {
            try {
                RatingSummaryResponse ratingSummary = ratingService.getRatingSummary(userId);
                profile.setAverageRating(ratingSummary.getAverageRating());

                // Also update the caregiver entity if ratings have changed significantly
                if (Math.abs(caregiver.getAverageRating() - ratingSummary.getAverageRating()) > 0.1) {
                    // Async update to avoid blocking the request
                    ratingService.updateCaregiverRatingCache(userId);
                }
            } catch (Exception e) {
                logger.warn("Failed to get rating summary for caregiver {}: {}", userId, e.getMessage());
                // Fall back to cached rating from database
                profile.setAverageRating(caregiver.getAverageRating());
            }
        }

        return profile;
    }

    @Override
    @Timed(value = "profile_get_all_caregivers_duration", description = "Time taken to get all caregivers")
    public List<ProfileResponse> getAllCareGivers() {
        logger.debug("Getting all caregivers");

        List<CareGiver> careGivers = careGiverRepository.findAll();
        return careGivers.stream()
                .map(this::enhanceProfileWithRating)
                .toList();
    }

    @Override
    @Timed(value = "profile_search_caregivers_duration", description = "Time taken to search caregivers")
    public List<ProfileResponse> searchCareGivers(String name, String speciality) {
        logger.debug("Searching caregivers with name: {} and speciality: {}", name, speciality);

        // Increment search requests counter
        monitoringConfig.getSearchRequests().increment();

        // Record search with tags
        monitoringConfig.meterRegistry.counter("profile_search_caregivers",
                "hasName", String.valueOf(name != null && !name.trim().isEmpty()),
                "hasSpeciality", String.valueOf(speciality != null && !speciality.trim().isEmpty())
        ).increment();

        List<CareGiver> careGivers;

        if (name != null && speciality != null) {
            careGivers = careGiverRepository.findByNameAndSpeciality(name, speciality);
        } else if (name != null) {
            careGivers = careGiverRepository.findByNameContainingIgnoreCase(name);
        } else if (speciality != null) {
            careGivers = careGiverRepository.findBySpecialityContainingIgnoreCase(speciality);
        } else {
            careGivers = careGiverRepository.findAll();
        }

        logger.debug("Found {} caregivers matching search criteria", careGivers.size());

        // Record search results
        monitoringConfig.meterRegistry.counter("profile_search_results").increment(careGivers.size());

        return careGivers.stream()
                .map(ProfileResponse::fromUser)
                .toList();
    }

    @Transactional
    @Timed(value = "profile_update_duration", description = "Time taken to update user profile")
    public ProfileResponse updateCurrentUserProfile(UpdateProfileRequest updateRequest) {
        logger.info("Updating profile for current user");

        // Increment profile updates counter
        monitoringConfig.getProfileUpdates().increment();

        Timer.Sample sample = Timer.start(monitoringConfig.meterRegistry);
        boolean emailChanged = false;
        String userType = "unknown";

        try {
            // Get the current user ID from SecurityContext
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long userId = Long.parseLong(userDetails.getUsername());

            // Find the user by ID
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE));

            userType = user instanceof Pacillian ? "pacillian" : "caregiver";

            // Handle email change if needed
            emailChanged = handleEmailChange(user, updateRequest, userId);

            // Update basic user information
            updateBasicUserInfo(user, updateRequest);

            // Save the user to appropriate repository
            saveUserByType(user, updateRequest);

            // Force flush to ensure changes are written
            flushUserByType(user);

            // Handle JWT token regeneration if email changed
            if (emailChanged) {
                generateNewJwtTokenForEmailChange(userId);
            }

            logger.info("Profile update successful for user: {}", user.getEmail());

            // Record successful update
            monitoringConfig.meterRegistry.counter("profile_update_successful",
                    USER_TYPE_TAG, userType,
                    EMAIL_CHANGED_TAG, String.valueOf(emailChanged)
            ).increment();

            return ProfileResponse.fromUser(user);

        } catch (Exception e) {
            // Record failed update
            monitoringConfig.meterRegistry.counter("profile_update_failed",
                    USER_TYPE_TAG, userType,
                    REASON_TAG, e.getClass().getSimpleName()
            ).increment();

            logger.error("Profile update failed: {}", e.getMessage());
            throw e;
        } finally {
            sample.stop(monitoringConfig.meterRegistry.timer("profile_update_duration",
                    USER_TYPE_TAG, userType,
                    EMAIL_CHANGED_TAG, String.valueOf(emailChanged)
            ));
        }
    }

    /**
     * Handles email change validation and update
     */
    private boolean handleEmailChange(User user, UpdateProfileRequest updateRequest, Long userId) {
        String currentEmail = user.getEmail();

        // Check if email is being changed
        if (!currentEmail.equals(updateRequest.getEmail())) {
            // Check if new email is already in use by another user
            if (userRepository.existsByEmail(updateRequest.getEmail())) {
                throw new EmailAlreadyExistsException("Email is already in use");
            }
            user.setEmail(updateRequest.getEmail());
            logger.info("Email changed for user {} from {} to {}", userId, currentEmail, updateRequest.getEmail());
            return true;
        }
        return false;
    }

    /**
     * Updates basic user information
     */
    private void updateBasicUserInfo(User user, UpdateProfileRequest updateRequest) {
        user.setName(updateRequest.getName());
        user.setAddress(updateRequest.getAddress());
        user.setPhoneNumber(updateRequest.getPhoneNumber());
    }

    /**
     * Saves user to appropriate repository based on type
     */
    private void saveUserByType(User user, UpdateProfileRequest updateRequest) {
        switch (user) {
            case Pacillian pacillian -> {
                pacillian.setMedicalHistory(updateRequest.getMedicalHistory());
                pacillianRepository.save(pacillian);
            }
            case CareGiver careGiver -> {
                careGiver.setSpeciality(updateRequest.getSpeciality());
                careGiver.setWorkAddress(updateRequest.getWorkAddress());
                careGiverRepository.save(careGiver);
            }
            default -> userRepository.save(user);
        }
    }

    /**
     * Flushes appropriate repository based on user type
     */
    private void flushUserByType(User user) {
        switch (user) {
            case Pacillian ignored -> pacillianRepository.flush();
            case CareGiver ignored -> careGiverRepository.flush();
            default -> userRepository.flush();
        }
    }

    /**
     * Generates new JWT token when email is changed
     */
    private void generateNewJwtTokenForEmailChange(Long userId) {
        // Re-fetch the user to ensure we have the latest data
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found after update"));

        // Generate a new token using the user ID (remains the same)
        String jwt = jwtUtils.generateJwtTokenFromUserId(userId.toString());

        // Get the current HTTP response to add the new token as a header
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletResponse httpResponse = requestAttributes.getResponse();
            if (httpResponse != null) {
                httpResponse.setHeader("Authorization", "Bearer " + jwt);
                httpResponse.setHeader("X-Email-Changed", "true");
            }
        }
    }

    @Override
    @Transactional
    @Timed(value = "profile_delete_duration", description = "Time taken to delete user account")
    public void deleteCurrentUserAccount() {
        logger.info("Deleting current user account");

        Timer.Sample sample = Timer.start(monitoringConfig.meterRegistry);
        String userType = "unknown";

        try {
            // Get the current user ID from security context
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long userId = Long.parseLong(userDetails.getUsername());

            // Find user by ID
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE));

            userType = user instanceof Pacillian ? "pacillian" : "caregiver";
            String userEmail = user.getEmail();

            userRepository.delete(user);

            // Decrement active sessions counter
            monitoringConfig.getActiveSessions().decrementAndGet();

            // Record successful deletion
            monitoringConfig.meterRegistry.counter("profile_delete_successful",
                    USER_TYPE_TAG, userType).increment();

            logger.info("Account deletion successful for user: {}", userEmail);

        } catch (Exception e) {
            // Record failed deletion
            monitoringConfig.meterRegistry.counter("profile_delete_failed",
                    USER_TYPE_TAG, userType,
                    REASON_TAG, e.getClass().getSimpleName()
            ).increment();

            logger.error("Account deletion failed: {}", e.getMessage());
            throw e;
        } finally {
            sample.stop(monitoringConfig.meterRegistry.timer("profile_delete_duration",
                    USER_TYPE_TAG, userType));
        }
    }

    @Override
    @Timed(value = "profile_get_caregivers_lite_duration", description = "Time taken to get caregivers lite")
    public List<ProfileResponse> getAllCareGiversLite() {
        logger.debug("Getting all caregivers (lite version)");

        List<CareGiver> careGivers = careGiverRepository.findAll();
        return careGivers.stream()
                .map(this::createLiteProfileResponseWithRating)
                .toList();
    }

    @Override
    @Timed(value = "profile_search_caregivers_lite_duration", description = "Time taken to search caregivers lite")
    public List<ProfileResponse> searchCareGiversLite(String name, String speciality) {
        logger.debug("Searching caregivers (lite) with name: {} and speciality: {}", name, speciality);

        // Increment search requests counter
        monitoringConfig.getSearchRequests().increment();

        List<CareGiver> careGivers;

        if (name != null && speciality != null) {
            careGivers = careGiverRepository.findByNameAndSpeciality(name, speciality);
        } else if (name != null) {
            careGivers = careGiverRepository.findByNameContainingIgnoreCase(name);
        } else if (speciality != null) {
            careGivers = careGiverRepository.findBySpecialityContainingIgnoreCase(speciality);
        } else {
            careGivers = careGiverRepository.findAll();
        }

        return careGivers.stream()
                .map(this::createLiteProfileResponseWithRating)
                .toList();
    }

    @Override
    @Timed(value = "profile_get_caregiver_lite_duration", description = "Time taken to get caregiver profile lite")
    public ProfileResponse getCareGiverProfileLite(Long caregiverId) {
        logger.debug("Getting caregiver profile (lite) for ID: {}", caregiverId);

        CareGiver careGiver = careGiverRepository.findById(caregiverId)
                .orElseThrow(() -> new EntityNotFoundException("Caregiver not found with id: " + caregiverId));

        return createLiteProfileResponseWithRating(careGiver);
    }

    @Timed(value = "profile_get_username_duration", description = "Time taken to get username by ID")
    public String getUserName(Long userId) {
        logger.debug("Getting username for ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return user.getName();
    }

    @Override
    public RatingSummaryResponse getRatingSummaryForCurrentUser() {
        // Ambil ID user saat ini dari security context
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long currentUserId = Long.parseLong(userDetails.getUsername());

        // Panggil ratingClientService yang sudah kamu buat
        List<RatingResponseDto> ratings = ratingClientService.getRatingsByDoctorId(currentUserId);

        if (ratings == null || ratings.isEmpty()) {
            return new RatingSummaryResponse(0, 0);
        }

        int total = ratings.size();
        double sum = ratings.stream().mapToInt(RatingResponseDto::getScore).sum();
        double avg = sum / total;

        return new RatingSummaryResponse(avg, total);
    }

    @Override
    public RatingSummaryResponse getRatingSummaryForCaregiver(Long caregiverId) {
        List<RatingResponseDto> ratings = ratingClientService.getRatingsByDoctorId(caregiverId);

        if (ratings == null || ratings.isEmpty()) {
            return new RatingSummaryResponse(0, 0);
        }

        int total = ratings.size();
        double sum = ratings.stream().mapToInt(RatingResponseDto::getScore).sum();
        double avg = sum / total;

        return new RatingSummaryResponse(avg, total);
    }

    /**
     * Enhanced profile creation with real-time rating data
     */
    private ProfileResponse enhanceProfileWithRating(CareGiver careGiver) {
        ProfileResponse response = ProfileResponse.fromUser(careGiver);

        try {
            RatingSummaryResponse ratingSummary = ratingService.getRatingSummary(careGiver.getId());
            response.setAverageRating(ratingSummary.getAverageRating());
        } catch (Exception e) {
            logger.debug("Failed to get real-time rating for caregiver {}, using cached value", careGiver.getId());
            response.setAverageRating(careGiver.getAverageRating());
        }

        return response;
    }

    /**
     * Creates a lite version of ProfileResponse with essential information and rating
     */
    private ProfileResponse createLiteProfileResponseWithRating(CareGiver careGiver) {
        ProfileResponse response = new ProfileResponse();
        response.setId(careGiver.getId());
        response.setEmail(careGiver.getEmail());
        response.setName(careGiver.getName());
        // Set NIK and address to null for security
        response.setNik(null);
        response.setAddress(null);
        response.setPhoneNumber(careGiver.getPhoneNumber());
        response.setUserType("CAREGIVER");
        response.setSpeciality(careGiver.getSpeciality());
        response.setWorkAddress(careGiver.getWorkAddress());

        // Try to get real-time rating, fall back to cached if service is unavailable
        try {
            RatingSummaryResponse ratingSummary = ratingService.getRatingSummary(careGiver.getId());
            response.setAverageRating(ratingSummary.getAverageRating());
        } catch (Exception e) {
            logger.debug("Using cached rating for caregiver {} due to service unavailability", careGiver.getId());
            response.setAverageRating(careGiver.getAverageRating());
        }

        return response;
    }
}