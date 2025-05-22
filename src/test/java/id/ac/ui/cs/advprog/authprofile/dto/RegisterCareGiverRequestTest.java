package id.ac.ui.cs.advprog.authprofile.dto;

import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterCareGiverRequest;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterCareGiverRequestTest {

    @Test
    void testNoArgsConstructor() {
        // when
        RegisterCareGiverRequest request = new RegisterCareGiverRequest();

        // then
        assertThat(request).isNotNull();
        assertThat(request.getEmail()).isNull();
        assertThat(request.getPassword()).isNull();
        assertThat(request.getName()).isNull();
        assertThat(request.getNik()).isNull();
        assertThat(request.getAddress()).isNull();
        assertThat(request.getPhoneNumber()).isNull();
        assertThat(request.getSpeciality()).isNull();
        assertThat(request.getWorkAddress()).isNull();
    }

    @Test
    void testAllArgsConstructor() {
        // given
        String speciality = "Cardiology";
        String workAddress = "Heart Hospital";

        // when
        RegisterCareGiverRequest request = new RegisterCareGiverRequest(speciality, workAddress);
        request.setEmail("doctor@example.com");
        request.setPassword("password123");
        request.setName("Dr. Smith");
        request.setNik("1234567890123456");
        request.setAddress("123 Main St");
        request.setPhoneNumber("0812345678");

        // then
        assertThat(request).isNotNull();
        assertThat(request.getEmail()).isEqualTo("doctor@example.com");
        assertThat(request.getPassword()).isEqualTo("password123");
        assertThat(request.getName()).isEqualTo("Dr. Smith");
        assertThat(request.getNik()).isEqualTo("1234567890123456");
        assertThat(request.getAddress()).isEqualTo("123 Main St");
        assertThat(request.getPhoneNumber()).isEqualTo("0812345678");
        assertThat(request.getSpeciality()).isEqualTo(speciality);
        assertThat(request.getWorkAddress()).isEqualTo(workAddress);

    }

    @Test
    void testSettersAndGetters() {
        // given
        RegisterCareGiverRequest request = new RegisterCareGiverRequest();
        String email = "doctor@example.com";
        String password = "password123";
        String name = "Dr. Smith";
        String nik = "1234567890123456";
        String address = "123 Main St";
        String phoneNumber = "0812345678";
        String speciality = "Cardiology";
        String workAddress = "Heart Hospital";



        // when
        request.setEmail(email);
        request.setPassword(password);
        request.setName(name);
        request.setNik(nik);
        request.setAddress(address);
        request.setPhoneNumber(phoneNumber);
        request.setSpeciality(speciality);
        request.setWorkAddress(workAddress);

        // then
        assertThat(request.getEmail()).isEqualTo(email);
        assertThat(request.getPassword()).isEqualTo(password);
        assertThat(request.getName()).isEqualTo(name);
        assertThat(request.getNik()).isEqualTo(nik);
        assertThat(request.getAddress()).isEqualTo(address);
        assertThat(request.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(request.getSpeciality()).isEqualTo(speciality);
        assertThat(request.getWorkAddress()).isEqualTo(workAddress);
    }

    @Test
    void testEqualsAndHashCode() {
        // given
        RegisterCareGiverRequest request1 = RegisterCareGiverRequest.builder()
                .email("doctor@example.com")
                .password("password123")
                .name("Dr. Smith")
                .nik("1234567890123456")
                .address("123 Main St")
                .phoneNumber("0812345678")
                .speciality("Cardiology")
                .workAddress("Heart Hospital")
                .build();

        RegisterCareGiverRequest request2 = RegisterCareGiverRequest.builder()
                .email("doctor@example.com")
                .password("password123")
                .name("Dr. Smith")
                .nik("1234567890123456")
                .address("123 Main St")
                .phoneNumber("0812345678")
                .speciality("Cardiology")
                .workAddress("Heart Hospital")
                .build();

        RegisterCareGiverRequest request3 = RegisterCareGiverRequest.builder()
                .email("other@example.com")
                .password("password123")
                .name("Dr. Smith")
                .nik("1234567890123456")
                .address("123 Main St")
                .phoneNumber("0812345678")
                .speciality("Cardiology")
                .workAddress("Heart Hospital")
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
        RegisterCareGiverRequest request = RegisterCareGiverRequest.builder()
                .email("doctor@example.com")
                .password("password123")
                .name("Dr. Smith")
                .nik("1234567890123456")
                .address("123 Main St")
                .phoneNumber("0812345678")
                .speciality("Cardiology")
                .workAddress("Heart Hospital")
                .build();

        // when
        String toString = request.toString();

        // then
        assertThat(toString).contains("doctor@example.com");
        assertThat(toString).contains("password123");
        assertThat(toString).contains("Dr. Smith");
        assertThat(toString).contains("1234567890123456");
        assertThat(toString).contains("123 Main St");
        assertThat(toString).contains("0812345678");
        assertThat(toString).contains("Cardiology");
        assertThat(toString).contains("Heart Hospital");
    }

    @Test
    void testSuperBuilder() {
        // given
        String email = "doctor@example.com";
        String password = "password123";
        String name = "Dr. Smith";
        String nik = "1234567890123456";
        String address = "123 Main St";
        String phoneNumber = "0812345678";
        String speciality = "Cardiology";
        String workAddress = "Heart Hospital";



        // when
        RegisterCareGiverRequest request = RegisterCareGiverRequest.builder()
                .email(email)
                .password(password)
                .name(name)
                .nik(nik)
                .address(address)
                .phoneNumber(phoneNumber)
                .speciality(speciality)
                .workAddress(workAddress)
                .build();

        // then
        assertThat(request.getEmail()).isEqualTo(email);
        assertThat(request.getPassword()).isEqualTo(password);
        assertThat(request.getName()).isEqualTo(name);
        assertThat(request.getNik()).isEqualTo(nik);
        assertThat(request.getAddress()).isEqualTo(address);
        assertThat(request.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(request.getSpeciality()).isEqualTo(speciality);
        assertThat(request.getWorkAddress()).isEqualTo(workAddress);
    }





}