package id.ac.ui.cs.advprog.authprofile.service;

import id.ac.ui.cs.advprog.authprofile.dto.request.UpdateProfileRequest;
import id.ac.ui.cs.advprog.authprofile.dto.response.ProfileResponse;
import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.repository.CareGiverRepository;
import id.ac.ui.cs.advprog.authprofile.repository.PacillianRepository;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import id.ac.ui.cs.advprog.authprofile.service.IProfileService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileServiceImpl implements IProfileService {

    private final UserRepository userRepository;
    private final PacillianRepository pacillianRepository;
    private final CareGiverRepository careGiverRepository;

    @Autowired
    public ProfileServiceImpl(
            UserRepository userRepository,
            PacillianRepository pacillianRepository,
            CareGiverRepository careGiverRepository) {
        this.userRepository = userRepository;
        this.pacillianRepository = pacillianRepository;
        this.careGiverRepository = careGiverRepository;
    }

    /**
     * Get the current logged-in user's profile
     */
    @Override
    public ProfileResponse getCurrentUserProfile() {
        String email = getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
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
    @Override
    @Transactional
    public ProfileResponse updateCurrentUserProfile(UpdateProfileRequest updateRequest) {
        String email = getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setName(updateRequest.getName());
        user.setAddress(updateRequest.getAddress());
        user.setPhoneNumber(updateRequest.getPhoneNumber());

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

        return ProfileResponse.fromUser(user);
    }

    /**
     * Delete the current user's account
     */
    @Override
    @Transactional
    public void deleteCurrentUserAccount() {
        String email = getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        userRepository.delete(user);
    }

    /**
     * Helper method to get the current logged-in user's email
     */
    private String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }
    @Override
    public List<ProfileResponse> getAllCareGiversLite() {
        List<CareGiver> careGivers = careGiverRepository.findAll();
        return careGivers.stream()
                .map(careGiver -> createLiteProfileResponse(careGiver))
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
                .map(careGiver -> createLiteProfileResponse(careGiver))
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
                .map(careGiver -> createLiteProfileResponse(careGiver))
                .collect(Collectors.toList());
    }


}