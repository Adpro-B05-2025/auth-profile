package id.ac.ui.cs.advprog.authprofile.security;

import id.ac.ui.cs.advprog.authprofile.config.MonitoringConfig;
import id.ac.ui.cs.advprog.authprofile.controller.ProfileController;
import id.ac.ui.cs.advprog.authprofile.dto.response.ProfileResponse;
import id.ac.ui.cs.advprog.authprofile.exception.UnauthorizedException;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import id.ac.ui.cs.advprog.authprofile.security.aspect.AuthorizationAspect;
import id.ac.ui.cs.advprog.authprofile.security.strategy.AuthorizationContext;
import id.ac.ui.cs.advprog.authprofile.service.IProfileService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthorizationIntegrationTest {

    @Mock
    private AuthorizationContext authorizationContext;

    @Mock
    private IProfileService profileService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MonitoringConfig monitoringConfig;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    private AuthorizationAspect authorizationAspect;
    private ProfileController profileController;
    private ProfileController profileControllerProxy;

    private User testUser;
    private ProfileResponse profileResponse;
    private ProfileResponse otherProfile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up monitoring config mocks
        monitoringConfig.meterRegistry = meterRegistry;
        when(meterRegistry.counter(anyString(), any(Tags.class))).thenReturn(counter);

        // Create the authorization aspect with all required dependencies
        authorizationAspect = new AuthorizationAspect(authorizationContext, userRepository, monitoringConfig);

        // Create the controller with mocked service
        profileController = new ProfileController(profileService);

        // Create AOP proxy with the authorization aspect
        AspectJProxyFactory factory = new AspectJProxyFactory(profileController);
        factory.addAspect(authorizationAspect);
        profileControllerProxy = factory.getProxy();

        // Clear security context before each test
        SecurityContextHolder.clearContext();

        // Set up common test data
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        profileResponse = new ProfileResponse();
        profileResponse.setId(1L);
        profileResponse.setEmail("test@example.com");

        otherProfile = new ProfileResponse();
        otherProfile.setId(2L);

        // Set up common stubs - now using ID-based lookups
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        // Ensure we mock non-existent IDs for testing
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        when(profileService.getCurrentUserProfile()).thenReturn(profileResponse);
        when(profileService.getUserProfile(2L)).thenReturn(otherProfile);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void whenUserAuthorized_thenSucceed() {
        // Set up authentication - use user ID as username instead of email
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("1") // User ID instead of email
                .password("password")
                .authorities("ROLE_PACILLIAN")
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Configure authorization to succeed
        when(authorizationContext.isAuthorized(eq(testUser), any(), eq("VIEW_OWN_PROFILE"))).thenReturn(true);

        // Execute and verify
        ResponseEntity<ProfileResponse> response = profileControllerProxy.getCurrentUserProfile();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(profileResponse.getId(), response.getBody().getId());
    }

    @Test
    void whenUserNotAuthorized_thenThrowUnauthorizedException() {
        // Set up authentication with user ID instead of email
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("1") // User ID instead of email
                .password("password")
                .authorities("ROLE_PACILLIAN")
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Configure authorization to fail
        when(authorizationContext.isAuthorized(eq(testUser), any(), eq("VIEW_OWN_PROFILE"))).thenReturn(false);

        // Execute and verify
        assertThrows(UnauthorizedException.class, () -> {
            profileControllerProxy.getCurrentUserProfile();
        });
    }

    @Test
    void whenUserNotFound_thenThrowUnauthorizedException() {
        // Set up authentication with non-existent user ID
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("999") // Non-existent user ID
                .password("password")
                .authorities("ROLE_PACILLIAN")
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Execute and verify - should throw exception because user doesn't exist
        assertThrows(UnauthorizedException.class, () -> {
            profileControllerProxy.getCurrentUserProfile();
        });
    }

    @Test
    void whenNotAuthenticated_thenThrowUnauthorizedException() {
        // Don't set up any authentication (SecurityContext is empty)

        // Execute and verify
        assertThrows(UnauthorizedException.class, () -> {
            profileControllerProxy.getCurrentUserProfile();
        });
    }

    @Test
    void getSpecificUserProfile_whenAuthorized() {
        // Set up authentication with user ID
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("1") // User ID instead of email
                .password("password")
                .authorities("ROLE_PACILLIAN")
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Configure authorization to succeed for viewing another user's profile
        when(authorizationContext.isAuthorized(eq(testUser), eq(2L), eq("VIEW_PROFILE"))).thenReturn(true);

        // Execute and verify
        ResponseEntity<ProfileResponse> response = profileControllerProxy.getUserProfile(2L);
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(otherProfile.getId(), response.getBody().getId());
    }

    @Test
    void getSpecificUserProfile_whenNotAuthorized() {
        // Set up authentication with user ID
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("1") // User ID instead of email
                .password("password")
                .authorities("ROLE_PACILLIAN")
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Configure authorization to fail for viewing another user's profile
        when(authorizationContext.isAuthorized(eq(testUser), eq(2L), eq("VIEW_PROFILE"))).thenReturn(false);

        // Execute and verify
        assertThrows(UnauthorizedException.class, () -> {
            profileControllerProxy.getUserProfile(2L);
        });
    }

    @Test
    void whenInvalidUserIdFormat_thenThrowUnauthorizedException() {
        // Set up authentication with invalid user ID format
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("invalid-id") // Invalid user ID format
                .password("password")
                .authorities("ROLE_PACILLIAN")
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Execute and verify
        assertThrows(UnauthorizedException.class, () -> {
            profileControllerProxy.getCurrentUserProfile();
        });
    }

    @Test
    void integrationTest_FullWorkflow() {
        // Test a complete workflow: authentication -> user lookup -> authorization -> service call

        // Set up authentication
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("1")
                .password("password")
                .authorities("ROLE_PACILLIAN")
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Configure authorization to succeed
        when(authorizationContext.isAuthorized(eq(testUser), any(), eq("VIEW_OWN_PROFILE"))).thenReturn(true);

        // Execute the full workflow
        ResponseEntity<ProfileResponse> response = profileControllerProxy.getCurrentUserProfile();

        // Verify all aspects worked together
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(profileResponse, response.getBody());
    }
}