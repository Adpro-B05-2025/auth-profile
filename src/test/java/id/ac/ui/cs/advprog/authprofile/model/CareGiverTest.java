package id.ac.ui.cs.advprog.authprofile.model;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class CareGiverTest {

    @Test
    void createCareGiver_ShouldSetAllFields() {
        // given
        String email = "doctor@example.com";
        String password = "password";
        String name = "Dr. Test";
        String nik = "1234567890123456";
        String address = "Doctor Address";
        String phoneNumber = "081234567890";
        String speciality = "Cardiology";
        String workAddress = "Test Hospital";

        // when
        CareGiver careGiver = new CareGiver(email, password, name, nik, address, phoneNumber, speciality, workAddress);

        // then
        assertThat(careGiver.getEmail()).isEqualTo(email);
        assertThat(careGiver.getPassword()).isEqualTo(password);
        assertThat(careGiver.getName()).isEqualTo(name);
        assertThat(careGiver.getNik()).isEqualTo(nik);
        assertThat(careGiver.getAddress()).isEqualTo(address);
        assertThat(careGiver.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(careGiver.getSpeciality()).isEqualTo(speciality);
        assertThat(careGiver.getWorkAddress()).isEqualTo(workAddress);
        assertThat(careGiver.getAverageRating()).isEqualTo(0.0);
        assertThat(careGiver.getRatingCount()).isEqualTo(0);
    }


    @Test
    void updateRating_ShouldCalculateAverageRating() {
        // given
        CareGiver careGiver = new CareGiver();
        assertThat(careGiver.getAverageRating()).isEqualTo(0.0);
        assertThat(careGiver.getRatingCount()).isEqualTo(0);

        // when - first rating
        careGiver.updateRating(4.0);

        // then
        assertThat(careGiver.getAverageRating()).isEqualTo(4.0);
        assertThat(careGiver.getRatingCount()).isEqualTo(1);

        // when - second rating
        careGiver.updateRating(5.0);

        // then - average should be (4.0 + 5.0) / 2 = 4.5
        assertThat(careGiver.getAverageRating()).isEqualTo(4.5);
        assertThat(careGiver.getRatingCount()).isEqualTo(2);
    }
}