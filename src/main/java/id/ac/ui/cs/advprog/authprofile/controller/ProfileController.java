package id.ac.ui.cs.advprog.authprofile.controller;

import id.ac.ui.cs.advprog.authprofile.dto.request.UpdateProfileRequest;
import id.ac.ui.cs.advprog.authprofile.dto.response.MessageResponse;
import id.ac.ui.cs.advprog.authprofile.dto.response.ProfileResponse;
import id.ac.ui.cs.advprog.authprofile.security.annotation.RequiresAuthorization;
import id.ac.ui.cs.advprog.authprofile.service.IProfileService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class ProfileController {

    private final IProfileService profileService;

    @Autowired
    public ProfileController(IProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    @RequiresAuthorization(action = "VIEW_OWN_PROFILE")
    public ResponseEntity<ProfileResponse> getCurrentUserProfile() {
        ProfileResponse profile = profileService.getCurrentUserProfile();
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @RequiresAuthorization(action = "UPDATE_PROFILE", resourceIdExpression = "null")
    public ResponseEntity<?> updateCurrentUserProfile(
            @Valid @RequestBody UpdateProfileRequest updateRequest,
            HttpServletResponse response) {

        ProfileResponse updatedProfile = profileService.updateCurrentUserProfile(updateRequest);

        // Check if a new token was generated due to email change
        String newToken = response.getHeader("Authorization");
        String emailChanged = response.getHeader("X-Email-Changed");

        if (emailChanged != null && emailChanged.equals("true")) {
            // Return both the profile and token information
            Map<String, Object> result = new HashMap<>();
            result.put("profile", updatedProfile);
            result.put("message", "Profile updated successfully. Your email has been changed, please use the new token for future requests.");
            result.put("tokenUpdated", true);

            if (newToken != null && newToken.startsWith("Bearer ")) {
                result.put("token", newToken.substring(7));
            }

            return ResponseEntity.ok(result);
        }

        // If no email change, just return the updated profile
        return ResponseEntity.ok(updatedProfile);
    }

    @DeleteMapping("/profile")
    @RequiresAuthorization(action = "DELETE_PROFILE", resourceIdExpression = "null")
    public ResponseEntity<MessageResponse> deleteCurrentUserAccount() {
        profileService.deleteCurrentUserAccount();
        return ResponseEntity.ok(new MessageResponse("User account deleted successfully"));
    }

    @GetMapping("/caregiver/all")
    public ResponseEntity<List<ProfileResponse>> getAllCareGivers() {
        List<ProfileResponse> careGivers = profileService.getAllCareGiversLite();
        return ResponseEntity.ok(careGivers);
    }

    @GetMapping("/caregiver/search")
    public ResponseEntity<List<ProfileResponse>> searchCareGivers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String speciality,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) DayOfWeek dayOfWeek,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {

        List<ProfileResponse> careGivers = profileService.searchCareGiversLite(name, speciality, dayOfWeek, time);
        return ResponseEntity.ok(careGivers);
    }

    @GetMapping("/user/{id}")
    @RequiresAuthorization(
            action = "VIEW_PROFILE",
            resourceIdExpression = "#args[0]" // This gets the id path variable
    )
    public ResponseEntity<ProfileResponse> getUserProfile(@PathVariable Long id) {
        ProfileResponse profile = profileService.getUserProfile(id);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/caregiver/{id}")
    @RequiresAuthorization(
            action = "VIEW_CAREGIVER",
            resourceIdExpression = "#args[0]" // This gets the id path variable
    )
    public ResponseEntity<ProfileResponse> getCareGiverProfile(@PathVariable Long id) {
        ProfileResponse profile = profileService.getCareGiverProfileLite(id);
        return ResponseEntity.ok(profile);
    }
}