package id.ac.ui.cs.advprog.authprofile.controller;

import id.ac.ui.cs.advprog.authprofile.config.AuthTestConfig;
import id.ac.ui.cs.advprog.authprofile.config.ProfileServiceTestConfig;
import id.ac.ui.cs.advprog.authprofile.dto.request.UpdateProfileRequest;
import id.ac.ui.cs.advprog.authprofile.dto.response.ProfileResponse;
import id.ac.ui.cs.advprog.authprofile.service.IProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProfileController.class)
@Import({AuthTestConfig.class, ProfileServiceTestConfig.class})
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IProfileService profileService;

    private ProfileResponse sampleProfileResponse;
    private UpdateProfileRequest updateProfileRequest;

    @BeforeEach
    void setUp() {
        // Setup sample profile response
        sampleProfileResponse = new ProfileResponse();
        sampleProfileResponse.setId(1L);
        sampleProfileResponse.setEmail("test@example.com");
        sampleProfileResponse.setName("Test User");
        sampleProfileResponse.setNik("1234567890123456");
        sampleProfileResponse.setAddress("Test Address");
        sampleProfileResponse.setPhoneNumber("081234567890");
        sampleProfileResponse.setUserType("PACILLIAN");
        sampleProfileResponse.setRoles(Arrays.asList("ROLE_PACILLIAN"));
        sampleProfileResponse.setMedicalHistory("No medical history");

        // Setup update profile request
        updateProfileRequest = new UpdateProfileRequest();
        updateProfileRequest.setName("Updated Name");
        updateProfileRequest.setAddress("Updated Address");
        updateProfileRequest.setPhoneNumber("087654321098");
        updateProfileRequest.setMedicalHistory("Updated medical history");
    }

    @Test
    @WithMockUser(roles = "PACILLIAN")
    void getCurrentUserProfile() throws Exception {
        when(profileService.getCurrentUserProfile()).thenReturn(sampleProfileResponse);

        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sampleProfileResponse.getId()))
                .andExpect(jsonPath("$.email").value(sampleProfileResponse.getEmail()))
                .andExpect(jsonPath("$.name").value(sampleProfileResponse.getName()));
    }

    @Test
    @WithMockUser(roles = "PACILLIAN")
    void updateCurrentUserProfile() throws Exception {
        ProfileResponse updatedProfile = new ProfileResponse();
        updatedProfile.setId(1L);
        updatedProfile.setEmail("test@example.com");
        updatedProfile.setName(updateProfileRequest.getName());
        updatedProfile.setNik("1234567890123456");
        updatedProfile.setAddress(updateProfileRequest.getAddress());
        updatedProfile.setPhoneNumber(updateProfileRequest.getPhoneNumber());
        updatedProfile.setUserType("PACILLIAN");
        updatedProfile.setRoles(Arrays.asList("ROLE_PACILLIAN"));
        updatedProfile.setMedicalHistory(updateProfileRequest.getMedicalHistory());

        when(profileService.updateCurrentUserProfile(any(UpdateProfileRequest.class)))
                .thenReturn(updatedProfile);

        mockMvc.perform(put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProfileRequest))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updateProfileRequest.getName()))
                .andExpect(jsonPath("$.address").value(updateProfileRequest.getAddress()))
                .andExpect(jsonPath("$.phoneNumber").value(updateProfileRequest.getPhoneNumber()))
                .andExpect(jsonPath("$.medicalHistory").value(updateProfileRequest.getMedicalHistory()));
    }

    @Test
    @WithMockUser(roles = "PACILLIAN")
    void deleteCurrentUserAccount() throws Exception {
        doNothing().when(profileService).deleteCurrentUserAccount();

        mockMvc.perform(delete("/api/profile")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User account deleted successfully"));
    }

    @Test
    @WithMockUser(roles = "PACILLIAN")
    void getAllCareGivers() throws Exception {
        ProfileResponse caregiver1 = new ProfileResponse();
        caregiver1.setId(2L);
        caregiver1.setEmail("doctor1@example.com");
        caregiver1.setName("Doctor One");
        caregiver1.setUserType("CAREGIVER");
        caregiver1.setSpeciality("Cardiology");

        ProfileResponse caregiver2 = new ProfileResponse();
        caregiver2.setId(3L);
        caregiver2.setEmail("doctor2@example.com");
        caregiver2.setName("Doctor Two");
        caregiver2.setUserType("CAREGIVER");
        caregiver2.setSpeciality("Neurology");

        List<ProfileResponse> caregivers = Arrays.asList(caregiver1, caregiver2);

        when(profileService.getAllCareGivers()).thenReturn(caregivers);

        mockMvc.perform(get("/api/caregiver/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(caregiver1.getId()))
                .andExpect(jsonPath("$[0].email").value(caregiver1.getEmail()))
                .andExpect(jsonPath("$[0].speciality").value(caregiver1.getSpeciality()))
                .andExpect(jsonPath("$[1].id").value(caregiver2.getId()))
                .andExpect(jsonPath("$[1].email").value(caregiver2.getEmail()))
                .andExpect(jsonPath("$[1].speciality").value(caregiver2.getSpeciality()));
    }

    @Test
    @WithMockUser(roles = "PACILLIAN")
    void searchCareGivers() throws Exception {
        ProfileResponse caregiver = new ProfileResponse();
        caregiver.setId(2L);
        caregiver.setEmail("doctor1@example.com");
        caregiver.setName("Doctor One");
        caregiver.setUserType("CAREGIVER");
        caregiver.setSpeciality("Cardiology");

        List<ProfileResponse> caregivers = Arrays.asList(caregiver);

        when(profileService.searchCareGivers("Doctor", "Cardiology")).thenReturn(caregivers);

        mockMvc.perform(get("/api/caregiver/search")
                        .param("name", "Doctor")
                        .param("speciality", "Cardiology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(caregiver.getId()))
                .andExpect(jsonPath("$[0].email").value(caregiver.getEmail()))
                .andExpect(jsonPath("$[0].speciality").value(caregiver.getSpeciality()));
    }

    @Test
    @WithMockUser(roles = "PACILLIAN")
    void getUserProfile() throws Exception {
        Long userId = 2L;
        ProfileResponse doctorProfile = new ProfileResponse();
        doctorProfile.setId(userId);
        doctorProfile.setEmail("doctor@example.com");
        doctorProfile.setName("Doctor Test");
        doctorProfile.setUserType("CAREGIVER");
        doctorProfile.setSpeciality("General Practitioner");

        when(profileService.getUserProfile(eq(userId))).thenReturn(doctorProfile);

        mockMvc.perform(get("/api/user/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(doctorProfile.getId()))
                .andExpect(jsonPath("$.email").value(doctorProfile.getEmail()))
                .andExpect(jsonPath("$.userType").value(doctorProfile.getUserType()));
    }
}