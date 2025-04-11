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
        assertThat(request.getWorkingSchedules()).isNull();
    }

    @Test
    void testAllArgsConstructor() {
        // given
        String email = "doctor@example.com";
        String password = "password123";
        String name = "Dr. Smith";
        String nik = "1234567890123456";
        String address = "123 Main St";
        String phoneNumber = "0812345678";
        String speciality = "Cardiology";
        String workAddress = "Heart Hospital";

        List<RegisterCareGiverRequest.WorkingScheduleRequest> schedules = new ArrayList<>();
        RegisterCareGiverRequest.WorkingScheduleRequest schedule =
                new RegisterCareGiverRequest.WorkingScheduleRequest(
                        DayOfWeek.MONDAY,
                        LocalTime.of(8, 0),
                        LocalTime.of(16, 0)
                );
        schedules.add(schedule);

        // when
        RegisterCareGiverRequest request = new RegisterCareGiverRequest(
                email, password, name, nik, address, phoneNumber, speciality,
                workAddress, schedules
        );

        // then
        assertThat(request).isNotNull();
        assertThat(request.getEmail()).isEqualTo(email);
        assertThat(request.getPassword()).isEqualTo(password);
        assertThat(request.getName()).isEqualTo(name);
        assertThat(request.getNik()).isEqualTo(nik);
        assertThat(request.getAddress()).isEqualTo(address);
        assertThat(request.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(request.getSpeciality()).isEqualTo(speciality);
        assertThat(request.getWorkAddress()).isEqualTo(workAddress);
        assertThat(request.getWorkingSchedules()).isEqualTo(schedules);
        assertThat(request.getWorkingSchedules()).hasSize(1);
        assertThat(request.getWorkingSchedules().get(0).getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(request.getWorkingSchedules().get(0).getStartTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(request.getWorkingSchedules().get(0).getEndTime()).isEqualTo(LocalTime.of(16, 0));
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

        List<RegisterCareGiverRequest.WorkingScheduleRequest> schedules = new ArrayList<>();
        RegisterCareGiverRequest.WorkingScheduleRequest schedule =
                new RegisterCareGiverRequest.WorkingScheduleRequest(
                        DayOfWeek.MONDAY,
                        LocalTime.of(8, 0),
                        LocalTime.of(16, 0)
                );
        schedules.add(schedule);

        // when
        request.setEmail(email);
        request.setPassword(password);
        request.setName(name);
        request.setNik(nik);
        request.setAddress(address);
        request.setPhoneNumber(phoneNumber);
        request.setSpeciality(speciality);
        request.setWorkAddress(workAddress);
        request.setWorkingSchedules(schedules);

        // then
        assertThat(request.getEmail()).isEqualTo(email);
        assertThat(request.getPassword()).isEqualTo(password);
        assertThat(request.getName()).isEqualTo(name);
        assertThat(request.getNik()).isEqualTo(nik);
        assertThat(request.getAddress()).isEqualTo(address);
        assertThat(request.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(request.getSpeciality()).isEqualTo(speciality);
        assertThat(request.getWorkAddress()).isEqualTo(workAddress);
        assertThat(request.getWorkingSchedules()).isEqualTo(schedules);
    }

    @Test
    void testEqualsAndHashCode() {
        // given
        RegisterCareGiverRequest request1 = new RegisterCareGiverRequest(
                "doctor@example.com", "password123", "Dr. Smith",
                "1234567890123456", "123 Main St", "0812345678",
                "Cardiology", "Heart Hospital", null
        );

        RegisterCareGiverRequest request2 = new RegisterCareGiverRequest(
                "doctor@example.com", "password123", "Dr. Smith",
                "1234567890123456", "123 Main St", "0812345678",
                "Cardiology", "Heart Hospital", null
        );

        RegisterCareGiverRequest request3 = new RegisterCareGiverRequest(
                "other@example.com", "password123", "Dr. Smith",
                "1234567890123456", "123 Main St", "0812345678",
                "Cardiology", "Heart Hospital", null
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
        RegisterCareGiverRequest request = new RegisterCareGiverRequest(
                "doctor@example.com", "password123", "Dr. Smith",
                "1234567890123456", "123 Main St", "0812345678",
                "Cardiology", "Heart Hospital", null
        );

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
    void testWorkingScheduleRequestNoArgsConstructor() {
        // when
        RegisterCareGiverRequest.WorkingScheduleRequest request =
                new RegisterCareGiverRequest.WorkingScheduleRequest();

        // then
        assertThat(request).isNotNull();
        assertThat(request.getDayOfWeek()).isNull();
        assertThat(request.getStartTime()).isNull();
        assertThat(request.getEndTime()).isNull();
    }

    @Test
    void testWorkingScheduleRequestAllArgsConstructor() {
        // given
        DayOfWeek dayOfWeek = DayOfWeek.MONDAY;
        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(16, 0);

        // when
        RegisterCareGiverRequest.WorkingScheduleRequest request =
                new RegisterCareGiverRequest.WorkingScheduleRequest(dayOfWeek, startTime, endTime);

        // then
        assertThat(request).isNotNull();
        assertThat(request.getDayOfWeek()).isEqualTo(dayOfWeek);
        assertThat(request.getStartTime()).isEqualTo(startTime);
        assertThat(request.getEndTime()).isEqualTo(endTime);
    }

    @Test
    void testWorkingScheduleRequestSettersAndGetters() {
        // given
        RegisterCareGiverRequest.WorkingScheduleRequest request =
                new RegisterCareGiverRequest.WorkingScheduleRequest();
        DayOfWeek dayOfWeek = DayOfWeek.MONDAY;
        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(16, 0);

        // when
        request.setDayOfWeek(dayOfWeek);
        request.setStartTime(startTime);
        request.setEndTime(endTime);

        // then
        assertThat(request.getDayOfWeek()).isEqualTo(dayOfWeek);
        assertThat(request.getStartTime()).isEqualTo(startTime);
        assertThat(request.getEndTime()).isEqualTo(endTime);
    }

    @Test
    void testWorkingScheduleRequestEqualsAndHashCode() {
        // given
        RegisterCareGiverRequest.WorkingScheduleRequest request1 =
                new RegisterCareGiverRequest.WorkingScheduleRequest(
                        DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(16, 0)
                );

        RegisterCareGiverRequest.WorkingScheduleRequest request2 =
                new RegisterCareGiverRequest.WorkingScheduleRequest(
                        DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(16, 0)
                );

        RegisterCareGiverRequest.WorkingScheduleRequest request3 =
                new RegisterCareGiverRequest.WorkingScheduleRequest(
                        DayOfWeek.TUESDAY, LocalTime.of(8, 0), LocalTime.of(16, 0)
                );

        // then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1).isNotEqualTo(request3);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        assertThat(request1.hashCode()).isNotEqualTo(request3.hashCode());
    }

    @Test
    void testWorkingScheduleRequestToString() {
        // given
        RegisterCareGiverRequest.WorkingScheduleRequest request =
                new RegisterCareGiverRequest.WorkingScheduleRequest(
                        DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(16, 0)
                );

        // when
        String toString = request.toString();

        // then
        assertThat(toString).contains("MONDAY");
        assertThat(toString).contains("08:00");
        assertThat(toString).contains("16:00");
    }
}