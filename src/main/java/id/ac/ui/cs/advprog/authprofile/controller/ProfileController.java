package id.ac.ui.cs.advprog.authprofile.controller;

import id.ac.ui.cs.advprog.authprofile.dto.request.UpdateProfileRequest;
import id.ac.ui.cs.advprog.authprofile.dto.response.MessageResponse;
import id.ac.ui.cs.advprog.authprofile.dto.response.ProfileResponse;
import id.ac.ui.cs.advprog.authprofile.security.annotation.RequiresAuthorization;
import id.ac.ui.cs.advprog.authprofile.service.IProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ProfileResponse> updateCurrentUserProfile(@Valid @RequestBody UpdateProfileRequest updateRequest) {
        ProfileResponse updatedProfile = profileService.updateCurrentUserProfile(updateRequest);
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
}