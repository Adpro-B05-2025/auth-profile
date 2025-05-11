package id.ac.ui.cs.advprog.authprofile.dto;

import id.ac.ui.cs.advprog.authprofile.dto.request.UpdateProfileRequest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class UpdateProfileRequestTest {

    @Test
    void testNoArgsConstructor() {
        // when
        UpdateProfileRequest request = new UpdateProfileRequest();

        // then
        assertThat(request).isNotNull();
        assertThat(request.getName()).isNull();
        assertThat(request.getEmail()).isNull();
        assertThat(request.getAddress()).isNull();
        assertThat(request.getPhoneNumber()).isNull();
        assertThat(request.getMedicalHistory()).isNull();
        assertThat(request.getSpeciality()).isNull();
        assertThat(request.getWorkAddress()).isNull();
    }

    @Test
    void testAllArgsConstructor() {
        // given
        String name = "Updated Name";
        String email = "updated@example.com";
        String address = "Updated Address";
        String phoneNumber = "0812345678";
        String medicalHistory = "Updated medical history";
        String speciality = "Updated speciality";
        String workAddress = "Updated work address";

        // when
        UpdateProfileRequest request = new UpdateProfileRequest(
                name, email, address, phoneNumber, medicalHistory, speciality, workAddress
        );

        // then
        assertThat(request).isNotNull();
        assertThat(request.getName()).isEqualTo(name);
        assertThat(request.getEmail()).isEqualTo(email);
        assertThat(request.getAddress()).isEqualTo(address);
        assertThat(request.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(request.getMedicalHistory()).isEqualTo(medicalHistory);
        assertThat(request.getSpeciality()).isEqualTo(speciality);
        assertThat(request.getWorkAddress()).isEqualTo(workAddress);
    }

    @Test
    void testSettersAndGetters() {
        // given
        UpdateProfileRequest request = new UpdateProfileRequest();
        String name = "Updated Name";
        String email = "updated@example.com";
        String address = "Updated Address";
        String phoneNumber = "0812345678";
        String medicalHistory = "Updated medical history";
        String speciality = "Updated speciality";
        String workAddress = "Updated work address";

        // when
        request.setName(name);
        request.setEmail(email);
        request.setAddress(address);
        request.setPhoneNumber(phoneNumber);
        request.setMedicalHistory(medicalHistory);
        request.setSpeciality(speciality);
        request.setWorkAddress(workAddress);

        // then
        assertThat(request.getName()).isEqualTo(name);
        assertThat(request.getEmail()).isEqualTo(email);
        assertThat(request.getAddress()).isEqualTo(address);
        assertThat(request.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(request.getMedicalHistory()).isEqualTo(medicalHistory);
        assertThat(request.getSpeciality()).isEqualTo(speciality);
        assertThat(request.getWorkAddress()).isEqualTo(workAddress);
    }

    @Test
    void testEqualsAndHashCode() {
        // given
        UpdateProfileRequest request1 = new UpdateProfileRequest(
                "Updated Name", "updated@example.com", "Updated Address", "0812345678",
                "Updated medical history", "Updated speciality", "Updated work address"
        );

        UpdateProfileRequest request2 = new UpdateProfileRequest(
                "Updated Name", "updated@example.com", "Updated Address", "0812345678",
                "Updated medical history", "Updated speciality", "Updated work address"
        );

        UpdateProfileRequest request3 = new UpdateProfileRequest(
                "Different Name", "different@example.com", "Updated Address", "0812345678",
                "Updated medical history", "Updated speciality", "Updated work address"
        );

        // then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1).isNotEqualTo(request3);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        assertThat(request1.hashCode()).isNotEqualTo(request3.hashCode());
    }

    @Test
    void testToString() {
        // given
        UpdateProfileRequest request = new UpdateProfileRequest(
                "Updated Name", "updated@example.com", "Updated Address", "0812345678",
                "Updated medical history", "Updated speciality", "Updated work address"
        );

        // when
        String toString = request.toString();

        // then
        assertThat(toString).contains("Updated Name");
        assertThat(toString).contains("updated@example.com");
        assertThat(toString).contains("Updated Address");
        assertThat(toString).contains("0812345678");
        assertThat(toString).contains("Updated medical history");
        assertThat(toString).contains("Updated speciality");
        assertThat(toString).contains("Updated work address");
    }
}