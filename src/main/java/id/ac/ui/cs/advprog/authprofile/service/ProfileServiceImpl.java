package id.ac.ui.cs.advprog.authprofile.service;

import id.ac.ui.cs.advprog.authprofile.client.RatingClientService;
import id.ac.ui.cs.advprog.authprofile.dto.rating.RatingResponseDto;
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
import java.util.stream.Collectors;

@Service
public class ProfileServiceImpl implements IProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileServiceImpl.class);

    private final UserRepository userRepository;
    private final PacillianRepository pacillianRepository;
    private final CareGiverRepository careGiverRepository;
    private final JwtUtils jwtUtils;
    private final RatingClientService ratingClientService;
    private final MonitoringConfig monitoringConfig;


    @Autowired
    public ProfileServiceImpl(
            UserRepository userRepository,
            PacillianRepository pacillianRepository,
            CareGiverRepository careGiverRepository,
            JwtUtils jwtUtils,
            RatingClientService ratingClientService,
            MonitoringConfig monitoringConfig) {

        this.userRepository = userRepository;
        this.pacillianRepository = pacillianRepository;
        this.careGiverRepository = careGiverRepository;
        this.jwtUtils = jwtUtils;
        this.ratingClientService = ratingClientService;
        this.monitoringConfig = monitoringConfig;
    }

    @Override
    @Timed(value = "profile_get_current_duration", description = "Time taken to get current user profile")
    public ProfileResponse getCurrentUserProfile() {
        logger.debug("Getting current user profile");

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Get the current user by ID instead of by email
        Long userId = Long.parseLong(userDetails.getUsername());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

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

        if (user instanceof CareGiver) {
            double avgRating = calculateAverageRating(user.getId());
            profile.setAverageRating(avgRating);
        }

        return profile;
    }

    @Override
    @Timed(value = "profile_get_all_caregivers_duration", description = "Time taken to get all caregivers")
    public List<ProfileResponse> getAllCareGivers() {
        logger.debug("Getting all caregivers");

        List<CareGiver> careGivers = careGiverRepository.findAll();
        return careGivers.stream()
                .map(ProfileResponse::fromUser)
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
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
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            userType = user instanceof Pacillian ? "pacillian" : "caregiver";
            String currentEmail = user.getEmail();

            // Check if email is being changed
            if (!currentEmail.equals(updateRequest.getEmail())) {
                // Check if new email is already in use by another user
                if (userRepository.existsByEmail(updateRequest.getEmail())) {
                    throw new EmailAlreadyExistsException("Email is already in use");
                }
                user.setEmail(updateRequest.getEmail());
                emailChanged = true;
                logger.info("Email changed for user {} from {} to {}", userId, currentEmail, updateRequest.getEmail());
            }

            user.setName(updateRequest.getName());
            user.setAddress(updateRequest.getAddress());
            user.setPhoneNumber(updateRequest.getPhoneNumber());

            // Save the user first
            if (user instanceof Pacillian pacillian) {
                pacillian.setMedicalHistory(updateRequest.getMedicalHistory());
                pacillianRepository.save(pacillian);
            } else if (user instanceof CareGiver careGiver) {
                careGiver.setSpeciality(updateRequest.getSpeciality());
                careGiver.setWorkAddress(updateRequest.getWorkAddress());
                careGiverRepository.save(careGiver);
            } else {
                userRepository.save(user);
            }

            // Force a flush to ensure changes are written to the database
            if (user instanceof Pacillian) {
                pacillianRepository.flush();
            } else if (user instanceof CareGiver) {
                careGiverRepository.flush();
            } else {
                userRepository.flush();
            }

            // If email was changed, generate new JWT token
            if (emailChanged) {
                // Re-fetch the user to ensure we have the latest data
                user = userRepository.findById(userId)
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

            logger.info("Profile update successful for user: {}", user.getEmail());

            // Record successful update
            monitoringConfig.meterRegistry.counter("profile_update_successful",
                    "userType", userType,
                    "emailChanged", String.valueOf(emailChanged)
            ).increment();

            return ProfileResponse.fromUser(user);

        } catch (Exception e) {
            // Record failed update
            monitoringConfig.meterRegistry.counter("profile_update_failed",
                    "userType", userType,
                    "reason", e.getClass().getSimpleName()
            ).increment();

            logger.error("Profile update failed: {}", e.getMessage());
            throw e;
        } finally {
            sample.stop(monitoringConfig.meterRegistry.timer("profile_update_duration",
                    "userType", userType,
                    "emailChanged", String.valueOf(emailChanged)
            ));
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
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            userType = user instanceof Pacillian ? "pacillian" : "caregiver";
            String userEmail = user.getEmail();

            userRepository.delete(user);

            // Decrement active sessions counter
            monitoringConfig.getActiveSessions().decrementAndGet();

            // Record successful deletion
            monitoringConfig.meterRegistry.counter("profile_delete_successful",
                    "userType", userType).increment();

            logger.info("Account deletion successful for user: {}", userEmail);

        } catch (Exception e) {
            // Record failed deletion
            monitoringConfig.meterRegistry.counter("profile_delete_failed",
                    "userType", userType,
                    "reason", e.getClass().getSimpleName()
            ).increment();

            logger.error("Account deletion failed: {}", e.getMessage());
            throw e;
        } finally {
            sample.stop(monitoringConfig.meterRegistry.timer("profile_delete_duration",
                    "userType", userType));
        }
    }

    @Override
    @Timed(value = "profile_get_caregivers_lite_duration", description = "Time taken to get caregivers lite")
    public List<ProfileResponse> getAllCareGiversLite() {
        logger.debug("Getting all caregivers (lite version)");

        List<CareGiver> careGivers = careGiverRepository.findAll();
        return careGivers.stream()
                .map(this::createLiteProfileResponse)
                .collect(Collectors.toList());
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
                .map(this::createLiteProfileResponse)
                .collect(Collectors.toList());
    }

    /**
     * Creates a lite version of ProfileResponse with only essential information
     */
    private ProfileResponse createLiteProfileResponse(CareGiver careGiver) {
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
        response.setAverageRating(careGiver.getAverageRating());

        return response;
    }

    @Override
    @Timed(value = "profile_get_caregiver_lite_duration", description = "Time taken to get caregiver profile lite")
    public ProfileResponse getCareGiverProfileLite(Long caregiverId) {
        logger.debug("Getting caregiver profile (lite) for ID: {}", caregiverId);

        CareGiver careGiver = careGiverRepository.findById(caregiverId)
                .orElseThrow(() -> new EntityNotFoundException("Caregiver not found with id: " + caregiverId));

        return createLiteProfileResponse(careGiver);
    }

    @Timed(value = "profile_get_username_duration", description = "Time taken to get username by ID")
    public String getUserName(Long userId) {
        logger.debug("Getting username for ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return user.getName();
    }

    private double calculateAverageRating(Long doctorId) {
        List<RatingResponseDto> ratings = ratingClientService.getRatingsByDoctorId(doctorId);
        if (ratings.isEmpty()) {
            return 0.0;
        }
        double sum = ratings.stream().mapToInt(RatingResponseDto::getScore).sum();
        return sum / ratings.size();
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

}