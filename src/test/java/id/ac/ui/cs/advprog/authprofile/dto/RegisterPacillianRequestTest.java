package id.ac.ui.cs.advprog.authprofile.dto;

import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterPacillianRequest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class RegisterPacillianRequestTest {

    @Test
    void testNoArgsConstructor() {
        // when
        RegisterPacillianRequest request = new RegisterPacillianRequest();

        // then
        assertThat(request).isNotNull();
        assertThat(request.getEmail()).isNull();
        assertThat(request.getPassword()).isNull();
        assertThat(request.getName()).isNull();
        assertThat(request.getNik()).isNull();
        assertThat(request.getAddress()).isNull();
        assertThat(request.getPhoneNumber()).isNull();
        assertThat(request.getMedicalHistory()).isNull();
    }

    @Test
    void testAllArgsConstructor() {
        // given
        String email = "patient@example.com";
        String password = "password123";
        String name = "Patient Name";
        String nik = "1234567890123456";
        String address = "123 Main St";
        String phoneNumber = "0812345678";
        String medicalHistory = "No significant medical history";

        // when
        RegisterPacillianRequest request = new RegisterPacillianRequest(medicalHistory);
        request.setEmail(email);
        request.setPassword(password);
        request.setName(name);
        request.setNik(nik);
        request.setAddress(address);
        request.setPhoneNumber(phoneNumber);

        // then
        assertThat(request).isNotNull();
        assertThat(request.getEmail()).isEqualTo(email);
        assertThat(request.getPassword()).isEqualTo(password);
        assertThat(request.getName()).isEqualTo(name);
        assertThat(request.getNik()).isEqualTo(nik);
        assertThat(request.getAddress()).isEqualTo(address);
        assertThat(request.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(request.getMedicalHistory()).isEqualTo(medicalHistory);
    }

    @Test
    void testSettersAndGetters() {
        // given
        RegisterPacillianRequest request = new RegisterPacillianRequest();
        String email = "patient@example.com";
        String password = "password123";
        String name = "Patient Name";
        String nik = "1234567890123456";
        String address = "123 Main St";
        String phoneNumber = "0812345678";
        String medicalHistory = "No significant medical history";

        // when
        request.setEmail(email);
        request.setPassword(password);
        request.setName(name);
        request.setNik(nik);
        request.setAddress(address);
        request.setPhoneNumber(phoneNumber);
        request.setMedicalHistory(medicalHistory);

        // then
        assertThat(request.getEmail()).isEqualTo(email);
        assertThat(request.getPassword()).isEqualTo(password);
        assertThat(request.getName()).isEqualTo(name);
        assertThat(request.getNik()).isEqualTo(nik);
        assertThat(request.getAddress()).isEqualTo(address);
        assertThat(request.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(request.getMedicalHistory()).isEqualTo(medicalHistory);
    }

    @Test
    void testEqualsAndHashCode() {
        // given
        RegisterPacillianRequest request1 = RegisterPacillianRequest.builder()
                .email("patient@example.com")
                .password("password123")
                .name("Patient Name")
                .nik("1234567890123456")
                .address("123 Main St")
                .phoneNumber("0812345678")
                .medicalHistory("No significant medical history")
                .build();

        RegisterPacillianRequest request2 = RegisterPacillianRequest.builder()
                .email("patient@example.com")
                .password("password123")
                .name("Patient Name")
                .nik("1234567890123456")
                .address("123 Main St")
                .phoneNumber("0812345678")
                .medicalHistory("No significant medical history")
                .build();

        RegisterPacillianRequest request3 = RegisterPacillianRequest.builder()
                .email("other@example.com")
                .password("password123")
                .name("Patient Name")
                .nik("1234567890123456")
                .address("123 Main St")
                .phoneNumber("0812345678")
                .medicalHistory("No significant medical history")
                .build();

        // then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1).isNotEqualTo(request3);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        assertThat(request1.hashCode()).isNotEqualTo(request3.hashCode());
    }

    @Test
    void testToString() {
        // given
        RegisterPacillianRequest request = RegisterPacillianRequest.builder()
                .email("patient@example.com")
                .password("password123")
                .name("Patient Name")
                .nik("1234567890123456")
                .address("123 Main St")
                .phoneNumber("0812345678")
                .medicalHistory("No significant medical history")
                .build();

        // when
        String toString = request.toString();

        // then
        assertThat(toString).contains("patient@example.com");
        assertThat(toString).contains("password123");
        assertThat(toString).contains("Patient Name");
        assertThat(toString).contains("1234567890123456");
        assertThat(toString).contains("123 Main St");
        assertThat(toString).contains("0812345678");
        assertThat(toString).contains("No significant medical history");
    }

    @Test
    void testSuperBuilder() {
        // given
        String email = "patient@example.com";
        String password = "password123";
        String name = "Patient Name";
        String nik = "1234567890123456";
        String address = "123 Main St";
        String phoneNumber = "0812345678";
        String medicalHistory = "No significant medical history";

        // when
        RegisterPacillianRequest request = RegisterPacillianRequest.builder()
                .email(email)
                .password(password)
                .name(name)
                .nik(nik)
                .address(address)
                .phoneNumber(phoneNumber)
                .medicalHistory(medicalHistory)
                .build();

        // then
        assertThat(request.getEmail()).isEqualTo(email);
        assertThat(request.getPassword()).isEqualTo(password);
        assertThat(request.getName()).isEqualTo(name);
        assertThat(request.getNik()).isEqualTo(nik);
        assertThat(request.getAddress()).isEqualTo(address);
        assertThat(request.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(request.getMedicalHistory()).isEqualTo(medicalHistory);
    }
}