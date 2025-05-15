package id.ac.ui.cs.advprog.authprofile.service;

import id.ac.ui.cs.advprog.authprofile.dto.request.UpdateProfileRequest;
import id.ac.ui.cs.advprog.authprofile.dto.response.ProfileResponse;
import id.ac.ui.cs.advprog.authprofile.exception.EmailAlreadyExistsException;
import id.ac.ui.cs.advprog.authprofile.exception.ResourceNotFoundException;
import id.ac.ui.cs.advprog.authprofile.exception.UnauthorizedException;
import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.repository.CareGiverRepository;
import id.ac.ui.cs.advprog.authprofile.repository.PacillianRepository;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import id.ac.ui.cs.advprog.authprofile.security.jwt.JwtUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class  ProfileServiceImpl implements IProfileService {

    private final UserRepository userRepository;
    private final PacillianRepository pacillianRepository;
    private final CareGiverRepository careGiverRepository;
    private final JwtUtils jwtUtils;

    @Autowired
    public ProfileServiceImpl(
            UserRepository userRepository,
            PacillianRepository pacillianRepository,
            CareGiverRepository careGiverRepository,
            JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.pacillianRepository = pacillianRepository;
        this.careGiverRepository = careGiverRepository;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Get the current logged-in user's profile
     */
    @Override
    public ProfileResponse getCurrentUserProfile() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Get the current user by ID instead of by email
        Long userId = Long.parseLong(userDetails.getUsername());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return ProfileResponse.fromUser(user);
    }

    /**
     * Get a specific user's profile by ID
     */
    @Override
    public ProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return ProfileResponse.fromUser(user);
    }

    /**
     * Get all CareGivers
     */
    @Override
    public List<ProfileResponse> getAllCareGivers() {
        List<CareGiver> careGivers = careGiverRepository.findAll();
        return careGivers.stream()
                .map(ProfileResponse::fromUser)
                .collect(Collectors.toList());
    }

    /**
     * Search CareGivers by name and speciality
     */
    @Override
    public List<ProfileResponse> searchCareGivers(String name, String speciality) {
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
                .map(ProfileResponse::fromUser)
                .collect(Collectors.toList());
    }

    /**
     * Update the current user's profile
     */
    @Transactional
    public ProfileResponse updateCurrentUserProfile(UpdateProfileRequest updateRequest) {
        // Get the current user ID from SecurityContext
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(userDetails.getUsername());

        // Find the user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String currentEmail = user.getEmail();
        boolean emailChanged = false;
        String newEmail = null;

        // Check if email is being changed
        if (!currentEmail.equals(updateRequest.getEmail())) {
            // Check if new email is already in use by another user
            if (userRepository.existsByEmail(updateRequest.getEmail())) {
                throw new EmailAlreadyExistsException("Email is already in use");
            }
            newEmail = updateRequest.getEmail(); // Store the new email
            user.setEmail(updateRequest.getEmail());
            emailChanged = true;
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

        // If email was changed, we still need to update the JWT token
        // The token itself doesn't need to change since it now contains the user ID (which is unchanged)
        // But we'll still provide a new token for consistency and to update the client
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

        return ProfileResponse.fromUser(user);
    }

    /**
     * Delete the current user's account
     */
    @Override
    @Transactional
    public void deleteCurrentUserAccount() {
        // Get the current user ID from security context
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(userDetails.getUsername());

        // Find user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        userRepository.delete(user);
    }


    @Override
    public List<ProfileResponse> getAllCareGiversLite() {
        List<CareGiver> careGivers = careGiverRepository.findAll();
        return careGivers.stream()
                .map(this::createLiteProfileResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProfileResponse> searchCareGiversLite(String name, String speciality) {
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
     * @param careGiver the caregiver entity
     * @return a ProfileResponse with only essential fields populated
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

        // Map working schedules
        List<ProfileResponse.WorkingScheduleDto> schedules = new ArrayList<>();
        if (careGiver.getWorkingSchedules() != null) {
            schedules = careGiver.getWorkingSchedules().stream()
                    .map(schedule -> new ProfileResponse.WorkingScheduleDto(
                            schedule.getDayOfWeek(),
                            schedule.getStartTime(),
                            schedule.getEndTime(),
                            schedule.isAvailable()
                    ))
                    .collect(Collectors.toList());
        }
        response.setWorkingSchedules(schedules);

        return response;
    }

    @Override
    public ProfileResponse getCareGiverProfileLite(Long caregiverId) {
        CareGiver careGiver = careGiverRepository.findById(caregiverId)
                .orElseThrow(() -> new EntityNotFoundException("Caregiver not found with id: " + caregiverId));

        // No need for type check since repository is already typed to CareGiver

        return createLiteProfileResponse(careGiver);
    }

    @Override
    public List<ProfileResponse> searchCareGiversLite(String name, String speciality, DayOfWeek dayOfWeek, LocalTime time) {
        List<CareGiver> careGivers;

        // Handle all possible combinations of search parameters
        if (dayOfWeek != null && time != null) {
            // When schedule filtering is requested
            if (name != null && speciality != null) {
                // Search by name, speciality, day and time
                careGivers = careGiverRepository.findByNameAndSpecialityAndAvailableDayAndTime(name, speciality, dayOfWeek, time);
            } else if (name != null) {
                // Search by name, day and time
                careGivers = careGiverRepository.findByNameAndAvailableDayAndTime(name, dayOfWeek, time);
            } else if (speciality != null) {
                // Search by speciality, day and time
                careGivers = careGiverRepository.findBySpecialityAndAvailableDayAndTime(speciality, dayOfWeek, time);
            } else {
                // Search by day and time only
                careGivers = careGiverRepository.findByAvailableDayAndTime(dayOfWeek, time);
            }
        } else if (dayOfWeek != null) {
            // Search by day only
            careGivers = careGiverRepository.findByAvailableDayOfWeek(dayOfWeek);
        } else {
            // Fall back to existing search without schedule filtering
            return searchCareGiversLite(name, speciality);
        }

        return careGivers.stream()
                .map(this::createLiteProfileResponse)
                .collect(Collectors.toList());
    }


}