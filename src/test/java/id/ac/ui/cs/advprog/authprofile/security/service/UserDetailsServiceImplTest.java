package id.ac.ui.cs.advprog.authprofile.security.service;

import id.ac.ui.cs.advprog.authprofile.model.Role;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import id.ac.ui.cs.advprog.authprofile.security.services.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;
    private final String testEmail = "test@example.com";
    private final String testPassword = "password123";
    private final Long testUserId = 1L;

    @BeforeEach
    public void setup() {
        // Create a test user with roles
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail(testEmail);
        testUser.setPassword(testPassword);
        testUser.setName("Test User");
        testUser.setNik("1234567890123456");
        testUser.setAddress("Test Address");
        testUser.setPhoneNumber("081234567890");

        // Create and add roles
        Set<Role> roles = new HashSet<>();
        Role pacillianRole = new Role();
        pacillianRole.setId(1);
        pacillianRole.setName(Role.ERole.ROLE_PACILLIAN);

        Role caregiverRole = new Role();
        caregiverRole.setId(2);
        caregiverRole.setName(Role.ERole.ROLE_CAREGIVER);

        roles.add(pacillianRole);
        roles.add(caregiverRole);

        testUser.setRoles(roles);
    }

    @Test
    public void loadUserByUsername_WithEmail_UserExists_ReturnsUserDetails() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(testEmail);

        // Assert
        assertNotNull(userDetails);
        // Now username should be the user's ID as a string
        assertEquals(testUserId.toString(), userDetails.getUsername());
        assertEquals(testPassword, userDetails.getPassword());
        assertEquals(2, userDetails.getAuthorities().size());

        // Verify authorities contain expected roles
        assertTrue(userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_PACILLIAN")));

        assertTrue(userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_CAREGIVER")));

        // Verify repository was called once with the correct email
        verify(userRepository, times(1)).findByEmail(testEmail);
    }

    @Test
    public void loadUserByUsername_WithUserId_UserExists_ReturnsUserDetails() {
        // Arrange
        String userIdString = testUserId.toString();
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(userIdString);

        // Assert
        assertNotNull(userDetails);
        assertEquals(userIdString, userDetails.getUsername());
        assertEquals(testPassword, userDetails.getPassword());
        assertEquals(2, userDetails.getAuthorities().size());

        // Verify repository was called once with the correct ID
        verify(userRepository, times(1)).findById(testUserId);
        // Verify findByEmail was not called
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    public void loadUserByUsername_WithEmail_UserNotFound_ThrowsException() {
        // Arrange
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(nonExistentEmail)
        );

        assertEquals("User Not Found with email: " + nonExistentEmail, exception.getMessage());
        verify(userRepository, times(1)).findByEmail(nonExistentEmail);
    }

    @Test
    public void loadUserByUsername_WithUserId_UserNotFound_ThrowsException() {
        // Arrange
        Long nonExistentId = 999L;
        String nonExistentIdString = nonExistentId.toString();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(nonExistentIdString)
        );

        assertEquals("User Not Found with id: " + nonExistentId, exception.getMessage());
        verify(userRepository, times(1)).findById(nonExistentId);
    }

    @Test
    public void loadUserByUsername_WithInvalidUserId_ThrowsException() {
        // Arrange
        String invalidUserId = "not-a-number";

        // Should fall back to email-based lookup - set up BEFORE the action
        when(userRepository.findByEmail(invalidUserId)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(invalidUserId)
        );

        // Verify the exception message
        assertEquals("User Not Found with email: " + invalidUserId, exception.getMessage());

        // Verify the repository was called with the correct email
        verify(userRepository, times(1)).findByEmail(invalidUserId);
    }

    @Test
    public void loadUserById_UserExists_ReturnsUserDetails() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserById(testUserId);

        // Assert
        assertNotNull(userDetails);
        assertEquals(testUserId.toString(), userDetails.getUsername());
        assertEquals(testPassword, userDetails.getPassword());
        assertEquals(2, userDetails.getAuthorities().size());

        // Verify authorities contain expected roles
        assertTrue(userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_PACILLIAN")));

        assertTrue(userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_CAREGIVER")));

        // Verify repository was called once with the correct ID
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    public void loadUserById_UserNotFound_ThrowsException() {
        // Arrange
        Long nonExistentId = 999L;
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserById(nonExistentId)
        );

        assertEquals("User Not Found with id: " + nonExistentId, exception.getMessage());
        verify(userRepository, times(1)).findById(nonExistentId);
    }

    @Test
    public void loadUserByUsername_UserWithNoRoles_ReturnsUserDetailsWithNoAuthorities() {
        // Arrange
        User userWithNoRoles = new User();
        userWithNoRoles.setId(testUserId);
        userWithNoRoles.setEmail(testEmail);
        userWithNoRoles.setPassword(testPassword);
        userWithNoRoles.setRoles(new HashSet<>());  // Empty roles set

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(userWithNoRoles));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUserId.toString());

        // Assert
        assertNotNull(userDetails);
        assertEquals(testUserId.toString(), userDetails.getUsername());
        assertEquals(testPassword, userDetails.getPassword());
        assertEquals(0, userDetails.getAuthorities().size());

        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    public void loadUserByUsername_UserWithSingleRole_ReturnsUserDetailsWithSingleAuthority() {
        // Arrange
        User userWithSingleRole = new User();
        userWithSingleRole.setId(testUserId);
        userWithSingleRole.setEmail(testEmail);
        userWithSingleRole.setPassword(testPassword);

        Set<Role> singleRole = new HashSet<>();
        Role pacillianRole = new Role();
        pacillianRole.setId(1);
        pacillianRole.setName(Role.ERole.ROLE_PACILLIAN);
        singleRole.add(pacillianRole);

        userWithSingleRole.setRoles(singleRole);

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(userWithSingleRole));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUserId.toString());

        // Assert
        assertNotNull(userDetails);
        assertEquals(testUserId.toString(), userDetails.getUsername());
        assertEquals(testPassword, userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());

        SimpleGrantedAuthority expectedAuthority = new SimpleGrantedAuthority("ROLE_PACILLIAN");
        assertTrue(userDetails.getAuthorities().contains(expectedAuthority));

        verify(userRepository, times(1)).findById(testUserId);
    }
}