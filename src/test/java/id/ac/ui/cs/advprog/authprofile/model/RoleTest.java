package id.ac.ui.cs.advprog.authprofile.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1);
        role.setName(Role.ERole.ROLE_PACILLIAN);
    }

    @Test
    void testRoleCreation() {
        assertNotNull(role);
        assertEquals(1, role.getId());
        assertEquals(Role.ERole.ROLE_PACILLIAN, role.getName());
    }

    @Test
    void testParameterizedConstructorWithRoleOnly() {
        Role newRole = new Role(Role.ERole.ROLE_CAREGIVER);

        assertNotNull(newRole);
        assertNull(newRole.getId());  // ID is not set in this constructor
        assertEquals(Role.ERole.ROLE_CAREGIVER, newRole.getName());
    }

    @Test
    void testFullParameterizedConstructor() {
        Integer id = 2;
        Role.ERole name = Role.ERole.ROLE_CAREGIVER;

        Role fullRole = new Role(id, name);

        assertNotNull(fullRole);
        assertEquals(id, fullRole.getId());
        assertEquals(name, fullRole.getName());
    }

    @Test
    void testERoleValues() {
        // Verify that the enum has the expected values
        assertEquals(2, Role.ERole.values().length);
        assertEquals(Role.ERole.ROLE_PACILLIAN, Role.ERole.valueOf("ROLE_PACILLIAN"));
        assertEquals(Role.ERole.ROLE_CAREGIVER, Role.ERole.valueOf("ROLE_CAREGIVER"));
    }

    @Test
    void testSetters() {
        role.setId(3);
        role.setName(Role.ERole.ROLE_CAREGIVER);

        assertEquals(3, role.getId());
        assertEquals(Role.ERole.ROLE_CAREGIVER, role.getName());
    }

    @Test
    void testEqualsAndHashCode() {
        Role role1 = new Role();
        role1.setId(1);
        role1.setName(Role.ERole.ROLE_PACILLIAN);

        Role role2 = new Role();
        role2.setId(1);
        role2.setName(Role.ERole.ROLE_PACILLIAN);

        Role role3 = new Role();
        role3.setId(2);
        role3.setName(Role.ERole.ROLE_CAREGIVER);

        // Test equality
        assertEquals(role1, role2);
        assertNotEquals(role1, role3);

        // Test hash code
        assertEquals(role1.hashCode(), role2.hashCode());
        assertNotEquals(role1.hashCode(), role3.hashCode());
    }

    @Test
    void testToString() {
        String roleString = role.toString();

        // Verify that toString contains important fields
        assertTrue(roleString.contains(String.valueOf(role.getId())));
        assertTrue(roleString.contains(role.getName().toString()));
    }
}
