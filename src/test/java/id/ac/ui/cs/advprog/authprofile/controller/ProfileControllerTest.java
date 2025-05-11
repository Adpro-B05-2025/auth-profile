package id.ac.ui.cs.advprog.authprofile.controller;

import java.util.Map;
import id.ac.ui.cs.advprog.authprofile.config.AuthTestConfig;
import id.ac.ui.cs.advprog.authprofile.config.ProfileServiceTestConfig;
import id.ac.ui.cs.advprog.authprofile.dto.request.UpdateProfileRequest;
import id.ac.ui.cs.advprog.authprofile.dto.response.ProfileResponse;
import id.ac.ui.cs.advprog.authprofile.exception.UnauthorizedException;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import id.ac.ui.cs.advprog.authprofile.security.aspect.AuthorizationAspect;
import id.ac.ui.cs.advprog.authprofile.security.strategy.AuthorizationContext;
import id.ac.ui.cs.advprog.authprofile.service.IProfileService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = ProfileController.class)
@Import({AuthTestConfig.class, ProfileServiceTestConfig.class, ProfileControllerTest.TestConfig.class})
class ProfileControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public AuthorizationAspect authorizationAspect(AuthorizationContext authorizationContext,
                                                       UserRepository userRepository) {
            // Create the real aspect but with your mocked dependencies
            return new AuthorizationAspect(authorizationContext, userRepository);
        }

        @Bean
        @Primary
        public AuthorizationContext authorizationContext() {
            AuthorizationContext mock = mock(AuthorizationContext.class);
            // The default behavior will be to return true
            when(mock.isAuthorized(any(), any(), anyString())).thenReturn(true);
            return mock;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IProfileService profileService;

    @Autowired
    private AuthorizationContext authorizationContext;

    private ProfileResponse sampleProfileResponse;
    private UpdateProfileRequest updateProfileRequest;

    @BeforeEach
    void setUp() {
        // Reset mock and ensure authorization passes by default
        when(authorizationContext.isAuthorized(any(), any(), anyString())).thenReturn(true);

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
        // Create the request object
        UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest();
        updateProfileRequest.setName("Updated Name");
        updateProfileRequest.setEmail("test@example.com");
        updateProfileRequest.setAddress("Updated Address");
        updateProfileRequest.setPhoneNumber("0812345678");
        updateProfileRequest.setMedicalHistory("Updated Medical History");

        // Create the expected response
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

        // Mock the service call
        when(profileService.updateCurrentUserProfile(any(UpdateProfileRequest.class)))
                .thenReturn(updatedProfile);

        // Perform the test
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
        // Create lite profile responses for caregivers
        ProfileResponse caregiver1 = new ProfileResponse();
        caregiver1.setId(2L);
        caregiver1.setEmail("doctor1@example.com");
        caregiver1.setName("Doctor One");
        caregiver1.setPhoneNumber("081234567890");
        caregiver1.setUserType("CAREGIVER");
        caregiver1.setSpeciality("Cardiology");
        caregiver1.setWorkAddress("Hospital A");
        caregiver1.setAverageRating(4.5);
        // Leave sensitive fields null
        caregiver1.setNik(null);
        caregiver1.setAddress(null);

        ProfileResponse caregiver2 = new ProfileResponse();
        caregiver2.setId(3L);
        caregiver2.setEmail("doctor2@example.com");
        caregiver2.setName("Doctor Two");
        caregiver2.setPhoneNumber("082345678901");
        caregiver2.setUserType("CAREGIVER");
        caregiver2.setSpeciality("Neurology");
        caregiver2.setWorkAddress("Hospital B");
        caregiver2.setAverageRating(4.8);
        // Leave sensitive fields null
        caregiver2.setNik(null);
        caregiver2.setAddress(null);

        List<ProfileResponse> caregivers = Arrays.asList(caregiver1, caregiver2);

        when(profileService.getAllCareGiversLite()).thenReturn(caregivers);

        mockMvc.perform(get("/api/caregiver/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(caregiver1.getId()))
                .andExpect(jsonPath("$[0].email").value(caregiver1.getEmail()))
                .andExpect(jsonPath("$[0].phoneNumber").value(caregiver1.getPhoneNumber()))
                .andExpect(jsonPath("$[0].speciality").value(caregiver1.getSpeciality()))
                .andExpect(jsonPath("$[0].workAddress").value(caregiver1.getWorkAddress()))
                .andExpect(jsonPath("$[0].averageRating").value(caregiver1.getAverageRating()))
                .andExpect(jsonPath("$[0].nik").doesNotExist())
                .andExpect(jsonPath("$[1].id").value(caregiver2.getId()))
                .andExpect(jsonPath("$[1].email").value(caregiver2.getEmail()))
                .andExpect(jsonPath("$[1].speciality").value(caregiver2.getSpeciality()));
    }

    @Test
    @WithMockUser(roles = "PACILLIAN")
    void searchCareGivers() throws Exception {
        // Create lite profile response for search result
        ProfileResponse caregiver = new ProfileResponse();
        caregiver.setId(2L);
        caregiver.setEmail("doctor1@example.com");
        caregiver.setName("Doctor One");
        caregiver.setPhoneNumber("081234567890");
        caregiver.setUserType("CAREGIVER");
        caregiver.setSpeciality("Cardiology");
        caregiver.setWorkAddress("Hospital A");
        caregiver.setAverageRating(4.5);
        // Leave sensitive fields null
        caregiver.setNik(null);
        caregiver.setAddress(null);

        List<ProfileResponse> caregivers = Arrays.asList(caregiver);

        when(profileService.searchCareGiversLite(eq("Doctor"), eq("Cardiology"), eq(null), eq(null)))
                .thenReturn(caregivers);

        mockMvc.perform(get("/api/caregiver/search")
                        .param("name", "Doctor")
                        .param("speciality", "Cardiology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(caregiver.getId()))
                .andExpect(jsonPath("$[0].email").value(caregiver.getEmail()))
                .andExpect(jsonPath("$[0].phoneNumber").value(caregiver.getPhoneNumber()))
                .andExpect(jsonPath("$[0].speciality").value(caregiver.getSpeciality()))
                .andExpect(jsonPath("$[0].nik").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "PACILLIAN")
    void searchCareGiversBySchedule() throws Exception {
        // Create lite profile response for search result
        ProfileResponse caregiver = new ProfileResponse();
        caregiver.setId(2L);
        caregiver.setEmail("doctor1@example.com");
        caregiver.setName("Doctor One");
        caregiver.setPhoneNumber("081234567890");
        caregiver.setUserType("CAREGIVER");
        caregiver.setSpeciality("Cardiology");
        caregiver.setWorkAddress("Hospital A");
        caregiver.setAverageRating(4.5);
        // Leave sensitive fields null
        caregiver.setNik(null);
        caregiver.setAddress(null);

        List<ProfileResponse> caregivers = Arrays.asList(caregiver);

        // Set up day and time for search
        DayOfWeek dayOfWeek = DayOfWeek.MONDAY;
        LocalTime time = LocalTime.of(10, 0); // 10:00 AM

        // Format the time for the HTTP request
        String timeString = time.format(DateTimeFormatter.ISO_TIME); // Formats as 10:00:00

        when(profileService.searchCareGiversLite(eq("Doctor"), eq("Cardiology"), eq(dayOfWeek), eq(time)))
                .thenReturn(caregivers);

        mockMvc.perform(get("/api/caregiver/search")
                        .param("name", "Doctor")
                        .param("speciality", "Cardiology")
                        .param("dayOfWeek", dayOfWeek.toString())
                        .param("time", timeString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(caregiver.getId()))
                .andExpect(jsonPath("$[0].email").value(caregiver.getEmail()))
                .andExpect(jsonPath("$[0].phoneNumber").value(caregiver.getPhoneNumber()))
                .andExpect(jsonPath("$[0].speciality").value(caregiver.getSpeciality()))
                .andExpect(jsonPath("$[0].nik").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "PACILLIAN")
    void searchCareGiversByDayOnly() throws Exception {
        // Create lite profile response for search result
        ProfileResponse caregiver = new ProfileResponse();
        caregiver.setId(2L);
        caregiver.setEmail("doctor1@example.com");
        caregiver.setName("Doctor One");
        caregiver.setPhoneNumber("081234567890");
        caregiver.setUserType("CAREGIVER");
        caregiver.setSpeciality("Cardiology");
        caregiver.setWorkAddress("Hospital A");
        caregiver.setAverageRating(4.5);
        // Leave sensitive fields null
        caregiver.setNik(null);
        caregiver.setAddress(null);

        List<ProfileResponse> caregivers = Arrays.asList(caregiver);

        // Set up day for search
        DayOfWeek dayOfWeek = DayOfWeek.MONDAY;

        when(profileService.searchCareGiversLite(eq(null), eq(null), eq(dayOfWeek), eq(null)))
                .thenReturn(caregivers);

        mockMvc.perform(get("/api/caregiver/search")
                        .param("dayOfWeek", dayOfWeek.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(caregiver.getId()))
                .andExpect(jsonPath("$[0].email").value(caregiver.getEmail()))
                .andExpect(jsonPath("$[0].speciality").value(caregiver.getSpeciality()))
                .andExpect(jsonPath("$[0].nik").doesNotExist());
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

    @Test
    @WithMockUser(roles = "PACILLIAN")
    void whenUserNotAuthorized_thenReturnForbidden() throws Exception {
        // Mock the service to throw UnauthorizedException
        when(profileService.getCurrentUserProfile())
                .thenThrow(new UnauthorizedException("User not authorized"));

        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser // No specific role needed
    void getCareGiverProfile_ShouldReturnLiteProfile() throws Exception {
        Long caregiverId = 3L;

        // Create lite profile response for caregiver
        ProfileResponse caregiverProfile = new ProfileResponse();
        caregiverProfile.setId(caregiverId);
        caregiverProfile.setEmail("caregiver@example.com");
        caregiverProfile.setName("Dr. Test");
        caregiverProfile.setPhoneNumber("083456789012");
        caregiverProfile.setUserType("CAREGIVER");
        caregiverProfile.setSpeciality("General");
        caregiverProfile.setWorkAddress("Test Hospital");
        caregiverProfile.setAverageRating(4.5);
        // Leave sensitive fields null
        caregiverProfile.setNik(null);
        caregiverProfile.setAddress(null);

        when(profileService.getCareGiverProfileLite(eq(caregiverId))).thenReturn(caregiverProfile);

        mockMvc.perform(get("/api/caregiver/{id}", caregiverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(caregiverProfile.getId()))
                .andExpect(jsonPath("$.email").value(caregiverProfile.getEmail()))
                .andExpect(jsonPath("$.name").value(caregiverProfile.getName()))
                .andExpect(jsonPath("$.phoneNumber").value(caregiverProfile.getPhoneNumber()))
                .andExpect(jsonPath("$.userType").value(caregiverProfile.getUserType()))
                .andExpect(jsonPath("$.speciality").value(caregiverProfile.getSpeciality()))
                .andExpect(jsonPath("$.workAddress").value(caregiverProfile.getWorkAddress()))
                .andExpect(jsonPath("$.averageRating").value(caregiverProfile.getAverageRating()))
                .andExpect(jsonPath("$.nik").doesNotExist());
    }

    @Test
    @WithMockUser
    void getCareGiverProfile_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        Long nonExistentId = 999L;

        when(profileService.getCareGiverProfileLite(eq(nonExistentId)))
                .thenThrow(new EntityNotFoundException("Caregiver not found with id: " + nonExistentId));

        mockMvc.perform(get("/api/caregiver/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PACILLIAN")
    void updateCurrentUserProfile_WithEmailChange_AllBranches() throws Exception {
        // Create the request object with email change
        UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest();
        updateProfileRequest.setName("Updated Name");
        updateProfileRequest.setEmail("new-email@example.com"); // Changed email
        updateProfileRequest.setAddress("Updated Address");
        updateProfileRequest.setPhoneNumber("0812345678");
        updateProfileRequest.setMedicalHistory("Updated Medical History");

        // Create the expected response
        ProfileResponse updatedProfile = new ProfileResponse();
        updatedProfile.setId(1L);
        updatedProfile.setEmail("new-email@example.com"); // Updated email
        updatedProfile.setName(updateProfileRequest.getName());
        updatedProfile.setNik("1234567890123456");
        updatedProfile.setAddress(updateProfileRequest.getAddress());
        updatedProfile.setPhoneNumber(updateProfileRequest.getPhoneNumber());
        updatedProfile.setUserType("PACILLIAN");
        updatedProfile.setRoles(Arrays.asList("ROLE_PACILLIAN"));
        updatedProfile.setMedicalHistory(updateProfileRequest.getMedicalHistory());

        // Mock the service call
        when(profileService.updateCurrentUserProfile(any(UpdateProfileRequest.class)))
                .thenReturn(updatedProfile);

        // Test case 1: X-Email-Changed is true and Authorization has "Bearer " prefix
        ProfileController controller = new ProfileController(profileService);
        MockHttpServletResponse mockResponse1 = new MockHttpServletResponse();
        mockResponse1.addHeader("Authorization", "Bearer new-token-value");
        mockResponse1.addHeader("X-Email-Changed", "true");

        ResponseEntity<?> responseEntity1 = controller.updateCurrentUserProfile(
                updateProfileRequest, mockResponse1);

        assertTrue(responseEntity1.getStatusCode().is2xxSuccessful());
        Map<String, Object> resultMap1 = (Map<String, Object>) responseEntity1.getBody();

        assertNotNull(resultMap1);
        assertEquals(updatedProfile, resultMap1.get("profile"));
        assertTrue(resultMap1.get("message").toString().contains("email has been changed"));
        assertEquals(true, resultMap1.get("tokenUpdated"));
        assertEquals("new-token-value", resultMap1.get("token"));

        // Test case 2: X-Email-Changed is true but Authorization doesn't start with "Bearer "
        MockHttpServletResponse mockResponse2 = new MockHttpServletResponse();
        mockResponse2.addHeader("Authorization", "invalid-token-format");
        mockResponse2.addHeader("X-Email-Changed", "true");

        ResponseEntity<?> responseEntity2 = controller.updateCurrentUserProfile(
                updateProfileRequest, mockResponse2);

        assertTrue(responseEntity2.getStatusCode().is2xxSuccessful());
        Map<String, Object> resultMap2 = (Map<String, Object>) responseEntity2.getBody();

        assertNotNull(resultMap2);
        assertEquals(updatedProfile, resultMap2.get("profile"));
        assertTrue(resultMap2.get("message").toString().contains("email has been changed"));
        assertEquals(true, resultMap2.get("tokenUpdated"));
        // The token should not be in the response since Authorization doesn't start with "Bearer "
        assertNull(resultMap2.get("token"));

        // Test case 3: X-Email-Changed is true but Authorization is null
        MockHttpServletResponse mockResponse3 = new MockHttpServletResponse();
        // Do not add Authorization header
        mockResponse3.addHeader("X-Email-Changed", "true");

        ResponseEntity<?> responseEntity3 = controller.updateCurrentUserProfile(
                updateProfileRequest, mockResponse3);

        assertTrue(responseEntity3.getStatusCode().is2xxSuccessful());
        Map<String, Object> resultMap3 = (Map<String, Object>) responseEntity3.getBody();

        assertNotNull(resultMap3);
        assertEquals(updatedProfile, resultMap3.get("profile"));
        assertTrue(resultMap3.get("message").toString().contains("email has been changed"));
        assertEquals(true, resultMap3.get("tokenUpdated"));
        // The token should not be in the response since Authorization is null
        assertNull(resultMap3.get("token"));

        // Test case 4: X-Email-Changed is null (no email change)
        MockHttpServletResponse mockResponse4 = new MockHttpServletResponse();
        // Do not add X-Email-Changed header

        ResponseEntity<?> responseEntity4 = controller.updateCurrentUserProfile(
                updateProfileRequest, mockResponse4);

        assertTrue(responseEntity4.getStatusCode().is2xxSuccessful());
        // Should return just the profile response, not a map
        assertEquals(updatedProfile, responseEntity4.getBody());

        // Test case 5: X-Email-Changed is not null but not equal to "true"
        MockHttpServletResponse mockResponse5 = new MockHttpServletResponse();
        mockResponse5.addHeader("X-Email-Changed", "false");

        ResponseEntity<?> responseEntity5 = controller.updateCurrentUserProfile(
                updateProfileRequest, mockResponse5);

        assertTrue(responseEntity5.getStatusCode().is2xxSuccessful());
        // Should return just the profile response, not a map
        assertEquals(updatedProfile, responseEntity5.getBody());
    }


}