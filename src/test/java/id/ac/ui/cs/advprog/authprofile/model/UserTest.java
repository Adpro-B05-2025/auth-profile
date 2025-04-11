package id.ac.ui.cs.advprog.authprofile.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;
    private final String email = "test@example.com";
    private final String password = "password123";
    private final String name = "Test User";
    private final String nik = "1234567890123456";
    private final String address = "Test Address";
    private final String phoneNumber = "081234567890";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setPassword(password);
        user.setName(name);
        user.setNik(nik);
        user.setAddress(address);
        user.setPhoneNumber(phoneNumber);

        Set<Role> roles = new HashSet<>();
        Role role = new Role(Role.ERole.ROLE_PACILLIAN);
        roles.add(role);
        user.setRoles(roles);
    }

    @Test
    void testUserCreation() {
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(name, user.getName());
        assertEquals(nik, user.getNik());
        assertEquals(address, user.getAddress());
        assertEquals(phoneNumber, user.getPhoneNumber());
        assertNotNull(user.getRoles());
        assertEquals(1, user.getRoles().size());
    }

    @Test
    void testUserParameterizedConstructor() {
        User newUser = new User(email, password, name, nik, address, phoneNumber);

        assertNotNull(newUser);
        assertEquals(email, newUser.getEmail());
        assertEquals(password, newUser.getPassword());
        assertEquals(name, newUser.getName());
        assertEquals(nik, newUser.getNik());
        assertEquals(address, newUser.getAddress());
        assertEquals(phoneNumber, newUser.getPhoneNumber());
    }

    @Test
    void testFullParameterizedConstructor() {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        Set<Role> roles = new HashSet<>();
        roles.add(new Role(Role.ERole.ROLE_CAREGIVER));

        User fullUser = new User(1L, email, password, name, nik, address, phoneNumber, roles, createdAt, updatedAt);

        assertNotNull(fullUser);
        assertEquals(1L, fullUser.getId());
        assertEquals(email, fullUser.getEmail());
        assertEquals(password, fullUser.getPassword());
        assertEquals(name, fullUser.getName());
        assertEquals(nik, fullUser.getNik());
        assertEquals(address, fullUser.getAddress());
        assertEquals(phoneNumber, fullUser.getPhoneNumber());
        assertEquals(roles, fullUser.getRoles());
        assertEquals(createdAt, fullUser.getCreatedAt());
        assertEquals(updatedAt, fullUser.getUpdatedAt());
    }

    @Test
    void testPrePersist() {
        User newUser = new User();

        // Call the @PrePersist method manually
        newUser.onCreate();

        assertNotNull(newUser.getCreatedAt());
        assertNotNull(newUser.getUpdatedAt());

        // More reliable test - checks that timestamps are within 100ms of each other
        long diffInMillis = ChronoUnit.MILLIS.between(
                newUser.getCreatedAt(),
                newUser.getUpdatedAt()
        );

        assertTrue(Math.abs(diffInMillis) < 100, "Timestamps should be very close together");
    }

    @Test
    void testPreUpdate() {
        User newUser = new User();
        newUser.onCreate(); // Set initial timestamps

        LocalDateTime createdAt = newUser.getCreatedAt();

        // Force a small delay to ensure timestamps are different
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Call the @PreUpdate method manually
        newUser.onUpdate();

        assertNotNull(newUser.getUpdatedAt());
        assertEquals(createdAt, newUser.getCreatedAt()); // Created time shouldn't change
        assertTrue(newUser.getUpdatedAt().isAfter(newUser.getCreatedAt())); // Updated time should be later
    }

    @Test
    void testEqualsAndHashCode() {
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(1L);

        User user3 = new User();
        user3.setId(2L);

        // Test equality
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);

        // Test hash code
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());
    }

    @Test
    void testToString() {
        String userString = user.toString();

        // Verify that toString contains important fields
        assertTrue(userString.contains(email));
        assertTrue(userString.contains(name));
        assertTrue(userString.contains(nik));
        assertTrue(userString.contains(address));
        assertTrue(userString.contains(phoneNumber));
    }
}