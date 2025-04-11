package id.ac.ui.cs.advprog.authprofile.service;

import id.ac.ui.cs.advprog.authprofile.dto.request.UpdateProfileRequest;
import id.ac.ui.cs.advprog.authprofile.dto.response.ProfileResponse;
import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
import id.ac.ui.cs.advprog.authprofile.model.Role;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.repository.CareGiverRepository;
import id.ac.ui.cs.advprog.authprofile.repository.PacillianRepository;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PacillianRepository pacillianRepository;

    @Autowired
    private CareGiverRepository careGiverRepository;

    /**
     * Get the current logged-in user's profile
     */
    public ProfileResponse getCurrentUserProfile() {
        String email = getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return ProfileResponse.fromUser(user);
    }

    /**
     * Get a specific user's profile by ID
     */
    public ProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return ProfileResponse.fromUser(user);
    }

    /**
     * Get all CareGivers
     */
    public List<ProfileResponse> getAllCareGivers() {
        List<CareGiver> careGivers = careGiverRepository.findAll();
        return careGivers.stream()
                .map(ProfileResponse::fromUser)
                .collect(Collectors.toList());
    }

    /**
     * Search CareGivers by name and speciality
     */
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
}