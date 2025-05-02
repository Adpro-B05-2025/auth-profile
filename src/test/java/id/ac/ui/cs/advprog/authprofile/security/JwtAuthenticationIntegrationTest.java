package id.ac.ui.cs.advprog.authprofile.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.authprofile.dto.request.LoginRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterPacillianRequest;
import id.ac.ui.cs.advprog.authprofile.model.Role;
import id.ac.ui.cs.advprog.authprofile.repository.PacillianRepository;
import id.ac.ui.cs.advprog.authprofile.repository.RoleRepository;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import id.ac.ui.cs.advprog.authprofile.security.strategy.AuthorizationContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class JwtAuthenticationIntegrationTest {

    // Add test configuration to mock authorization context
    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public AuthorizationContext authorizationContext() {
            AuthorizationContext mockContext = mock(AuthorizationContext.class);
            // Always authorize for tests
            when(mockContext.isAuthorized(any(), any(), anyString())).thenReturn(true);
            return mockContext;
        }

        // Configure test properties
        @DynamicPropertySource
        static void configureTestProperties(DynamicPropertyRegistry registry) {
            // Override JWT configuration to ensure clean test environment
            registry.add("pandacare.app.jwtSecret", () -> "testSecretKeyForIntegrationTestingOnly");
            registry.add("pandacare.app.jwtExpirationMs", () -> "3600000");
        }
    }

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PacillianRepository pacillianRepository;

    @Autowired
    private AuthorizationContext authorizationContext;

    private RegisterPacillianRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Set up MockMvc with security for each test
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Configure mock authorization context to always return true for tests
        when(authorizationContext.isAuthorized(any(), any(), anyString())).thenReturn(true);

        // Check if roles exist, if not create them for tests
        if (!roleRepository.findByName(Role.ERole.ROLE_PACILLIAN).isPresent()) {
            Role pacillianRole = new Role();
            pacillianRole.setName(Role.ERole.ROLE_PACILLIAN);
            roleRepository.save(pacillianRole);
        }

        if (!roleRepository.findByName(Role.ERole.ROLE_CAREGIVER).isPresent()) {
            Role caregiverRole = new Role();
            caregiverRole.setName(Role.ERole.ROLE_CAREGIVER);
            roleRepository.save(caregiverRole);
        }

        // Clear existing test users if any
        userRepository.findByEmail("test@example.com").ifPresent(user -> userRepository.delete(user));

        // Using the builder pattern instead of setters
        registerRequest = RegisterPacillianRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("Test User")
                .nik("1234567890123456")
                .address("Test Address")
                .phoneNumber("081234567890")
                .medicalHistory("No significant history")
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @AfterEach
    void tearDown() {
        // Clear security context after each test
        SecurityContextHolder.clearContext();
    }

    @Test
    void fullAuthenticationFlow() throws Exception {
        // 1. Register a new pacillian
        mockMvc.perform(post("/api/auth/register/pacillian")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Pacillian registered successfully!")));

        // 2. Login with the registered account
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andReturn();

        // 3. Extract JWT token from response
        String response = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(response).get("token").asText();

        // 4. Access protected endpoint with JWT token
        mockMvc.perform(get("/api/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.name", is("Test User")));

        // Clear security context to ensure clean state
        SecurityContextHolder.clearContext();

        // 5. Access protected endpoint without token (should fail)
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerWithExistingEmail_ShouldReturnError() throws Exception {
        // First registration
        mockMvc.perform(post("/api/auth/register/pacillian")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Second registration with same email
        mockMvc.perform(post("/api/auth/register/pacillian")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void loginWithInvalidCredentials_ShouldReturnError() throws Exception {
        // Register user
        mockMvc.perform(post("/api/auth/register/pacillian")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Try to login with wrong password
        LoginRequest wrongLoginRequest = new LoginRequest();
        wrongLoginRequest.setEmail("test@example.com");
        wrongLoginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongLoginRequest)))
                .andExpect(status().isUnauthorized());
    }
}