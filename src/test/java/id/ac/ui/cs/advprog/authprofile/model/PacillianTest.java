package id.ac.ui.cs.advprog.authprofile.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PacillianTest {

    private Pacillian pacillian;
    private final String email = "pacillian@example.com";
    private final String password = "password123";
    private final String name = "Test Pacillian";
    private final String nik = "1234567890123456";
    private final String address = "Pacillian Address";
    private final String phoneNumber = "081234567890";
    private final String medicalHistory = "No significant medical history";

    @BeforeEach
    void setUp() {
        pacillian = new Pacillian();
        pacillian.setEmail(email);
        pacillian.setPassword(password);
        pacillian.setName(name);
        pacillian.setNik(nik);
        pacillian.setAddress(address);
        pacillian.setPhoneNumber(phoneNumber);
        pacillian.setMedicalHistory(medicalHistory);
    }

    @Test
    void testPacillianCreation() {
        assertNotNull(pacillian);
        assertEquals(email, pacillian.getEmail());
        assertEquals(password, pacillian.getPassword());
        assertEquals(name, pacillian.getName());
        assertEquals(nik, pacillian.getNik());
        assertEquals(address, pacillian.getAddress());
        assertEquals(phoneNumber, pacillian.getPhoneNumber());
        assertEquals(medicalHistory, pacillian.getMedicalHistory());
    }

    @Test
    void testParameterizedConstructor() {
        Pacillian newPacillian = new Pacillian(email, password, name, nik, address, phoneNumber, medicalHistory);

        assertNotNull(newPacillian);
        assertEquals(email, newPacillian.getEmail());
        assertEquals(password, newPacillian.getPassword());
        assertEquals(name, newPacillian.getName());
        assertEquals(nik, newPacillian.getNik());
        assertEquals(address, newPacillian.getAddress());
        assertEquals(phoneNumber, newPacillian.getPhoneNumber());
        assertEquals(medicalHistory, newPacillian.getMedicalHistory());
    }

    @Test
    void testFullParameterizedConstructor() {
        Pacillian allArgsPacillian = new Pacillian(medicalHistory);

        assertNotNull(allArgsPacillian);
        assertEquals(medicalHistory, allArgsPacillian.getMedicalHistory());
    }

    @Test
    void testMedicalHistorySetter() {
        String newMedicalHistory = "Updated medical history with some conditions";
        pacillian.setMedicalHistory(newMedicalHistory);

        assertEquals(newMedicalHistory, pacillian.getMedicalHistory());
    }

    @Test
    void testEqualsAndHashCode() {
        Pacillian pacillian1 = new Pacillian();
        pacillian1.setId(1L);
        pacillian1.setMedicalHistory(medicalHistory);

        Pacillian pacillian2 = new Pacillian();
        pacillian2.setId(1L);
        pacillian2.setMedicalHistory(medicalHistory);

        Pacillian pacillian3 = new Pacillian();
        pacillian3.setId(2L);
        pacillian3.setMedicalHistory("Different medical history");

        // Test equality
        assertEquals(pacillian1, pacillian2);
        assertNotEquals(pacillian1, pacillian3);

        // Test hash code
        assertEquals(pacillian1.hashCode(), pacillian2.hashCode());
        assertNotEquals(pacillian1.hashCode(), pacillian3.hashCode());
    }

    @Test
    void testInheritance() {
        // Verify that Pacillian is a subclass of User
        assertInstanceOf(User.class, pacillian);
    }

    @Test
    void testToString() {
        String pacillianString = pacillian.toString();

        // With Lombok, we just verify that toString method exists and returns non-null
        assertNotNull(pacillianString);

        // Verify that the medicalHistory field is included in toString
        // This is the field specific to Pacillian class
        assertTrue(pacillianString.contains("medicalHistory=" + medicalHistory));
    }
}