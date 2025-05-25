package id.ac.ui.cs.advprog.authprofile.controller;

import id.ac.ui.cs.advprog.authprofile.dto.request.UpdateProfileRequest;
import id.ac.ui.cs.advprog.authprofile.dto.response.MessageResponse;
import id.ac.ui.cs.advprog.authprofile.dto.response.ProfileResponse;
import id.ac.ui.cs.advprog.authprofile.dto.response.RatingSummaryResponse;
import id.ac.ui.cs.advprog.authprofile.security.annotation.RequiresAuthorization;
import id.ac.ui.cs.advprog.authprofile.service.IProfileService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
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
            @RequestParam(required = false) String speciality) {

        List<ProfileResponse> careGivers = profileService.searchCareGiversLite(name, speciality);
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


    @GetMapping("/profile/ratings")
    public ResponseEntity<RatingSummaryResponse> getRatingsForCurrentUser() {
        RatingSummaryResponse ratingSummary = profileService.getRatingSummaryForCurrentUser();
        return ResponseEntity.ok(ratingSummary);
    }

    @GetMapping("/caregiver/{id}/summary")
    @RequiresAuthorization(action = "VIEW_CAREGIVER", resourceIdExpression = "#id")
    public ResponseEntity<RatingSummaryResponse> getCaregiverRatingSummary(@PathVariable Long id) {
        RatingSummaryResponse ratingSummary = profileService.getRatingSummaryForCaregiver(id);
        return ResponseEntity.ok(ratingSummary);


    @GetMapping("/user/{userId}/name")
    @RequiresAuthorization(
            action = "VIEW_USERNAME"
    )
    public ResponseEntity<Map<String, Object>> getUserName(@PathVariable Long userId) {

        try {
            String userName = profileService.getUserName(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("id", userId);
            response.put("name", userName);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            Map<String, Object> error = new HashMap<>();
            error.put("error", "User not found");
            error.put("id", userId);
            error.put("name", "User " + userId); // Fallback name

            return ResponseEntity.status(404).body(error);
        }

    }
}