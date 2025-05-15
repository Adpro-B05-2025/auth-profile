package id.ac.ui.cs.advprog.authprofile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.authprofile.config.AuthTestConfig;
import id.ac.ui.cs.advprog.authprofile.dto.request.LoginRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterCareGiverRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterPacillianRequest;
import id.ac.ui.cs.advprog.authprofile.dto.response.JwtResponse;
import id.ac.ui.cs.advprog.authprofile.dto.response.TokenValidationResponse;
import id.ac.ui.cs.advprog.authprofile.service.IAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(AuthTestConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IAuthService authService;

    private LoginRequest loginRequest;
    private RegisterPacillianRequest pacillianRequest;
    private RegisterCareGiverRequest careGiverRequest;
    private JwtResponse jwtResponse;
    private TokenValidationResponse validTokenResponse;
    private TokenValidationResponse invalidTokenResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("test@example.com", "password");

        pacillianRequest = new RegisterPacillianRequest();
        pacillianRequest.setEmail("pacillian@example.com");
        pacillianRequest.setPassword("password");
        pacillianRequest.setName("Test Pacillian");
        pacillianRequest.setNik("1234567890123456");
        pacillianRequest.setAddress("Test Address");
        pacillianRequest.setPhoneNumber("081234567890");
        pacillianRequest.setMedicalHistory("No significant history");

        careGiverRequest = new RegisterCareGiverRequest();
        careGiverRequest.setEmail("caregiver@example.com");
        careGiverRequest.setPassword("password");
        careGiverRequest.setName("Dr. Test");
        careGiverRequest.setNik("6543210987654321");
        careGiverRequest.setAddress("Doctor Address");
        careGiverRequest.setPhoneNumber("089876543210");
        careGiverRequest.setSpeciality("General");
        careGiverRequest.setWorkAddress("Test Hospital");
        careGiverRequest.setWorkingSchedules(null);

        jwtResponse = new JwtResponse("test_jwt_token", 1L, "test@example.com", "Test User", Arrays.asList("ROLE_PACILLIAN"));

        // Setup token validation responses
        validTokenResponse = new TokenValidationResponse(true, 1L, "test@example.com", Arrays.asList("ROLE_PACILLIAN"));
        invalidTokenResponse = new TokenValidationResponse(false, null, null, null);
    }

    @Test
    @WithMockUser
    void authenticateUser_ShouldReturnJwtResponse() throws Exception {
        when(authService.authenticateUser(any(LoginRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("test_jwt_token")))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.name", is("Test User")))
                .andExpect(jsonPath("$.roles[0]", is("ROLE_PACILLIAN")));
    }

    @Test
    @WithMockUser
    void registerPacillian_ShouldReturnSuccessMessage() throws Exception {
        // Given
        String successMessage = "Pacillian registered successfully!";
        when(authService.registerUser(any(RegisterPacillianRequest.class))).thenReturn(successMessage);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pacillianRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is(successMessage)))
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @WithMockUser
    void registerCareGiver_ShouldReturnSuccessMessage() throws Exception {
        // Given
        String successMessage = "CareGiver registered successfully!";
        when(authService.registerUser(any(RegisterCareGiverRequest.class))).thenReturn(successMessage);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(careGiverRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is(successMessage)))
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @WithMockUser
    void validateToken_WithValidToken_ShouldReturnValidResponse() throws Exception {
        // Given
        when(authService.validateToken(anyString())).thenReturn(validTokenResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/validate")
                        .header("Authorization", "Bearer valid_test_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", is(true)))
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.username", is("test@example.com")))
                .andExpect(jsonPath("$.roles[0]", is("ROLE_PACILLIAN")));
    }

    @Test
    @WithMockUser
    void validateToken_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        // Given
        when(authService.validateToken(anyString())).thenReturn(invalidTokenResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/validate")
                        .header("Authorization", "Bearer invalid_test_token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid", is(false)))
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andExpect(jsonPath("$.username").doesNotExist())
                .andExpect(jsonPath("$.roles").doesNotExist());
    }

    @Test
    @WithMockUser
    void validateToken_WithMissingAuthHeader_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/validate"))
                .andExpect(status().isUnauthorized());
        // We expect a 401 Unauthorized as defined in our controller
    }

    @Test
    @WithMockUser
    void validateToken_WithInvalidAuthHeader_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/validate")
                        .header("Authorization", "InvalidHeader"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid", is(false)));
    }
}