package id.ac.ui.cs.advprog.authprofile.repository;

import id.ac.ui.cs.advprog.authprofile.config.TestConfig;
import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.WorkingSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestConfig.class)
@ActiveProfiles("test")
class CareGiverRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CareGiverRepository careGiverRepository;


    @BeforeEach
    void setup() {
        // Clear the database before each test
        careGiverRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByEmail_ShouldReturnCareGiver_WhenCareGiverExists() {
        // given
        CareGiver careGiver = new CareGiver();
        careGiver.setEmail("doctor@example.com");
        careGiver.setPassword("password");
        careGiver.setName("Dr. Smith");
        careGiver.setNik("1234567890123456");
        careGiver.setAddress("Doctor Address");
        careGiver.setPhoneNumber("081234567890");
        careGiver.setSpeciality("Cardiology");
        careGiver.setWorkAddress("Heart Hospital");

        entityManager.persistAndFlush(careGiver);

        // when
        Optional<CareGiver> found = careGiverRepository.findByEmail("doctor@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("doctor@example.com");
        assertThat(found.get().getName()).isEqualTo("Dr. Smith");
        assertThat(found.get().getSpeciality()).isEqualTo("Cardiology");
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenCareGiverDoesNotExist() {
        // when
        Optional<CareGiver> found = careGiverRepository.findByEmail("nonexistent@example.com");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldReturnMatchingCareGivers() {
        // given
        CareGiver careGiver1 = new CareGiver();
        careGiver1.setEmail("smith@example.com");
        careGiver1.setPassword("password");
        careGiver1.setName("Dr. Smith");
        careGiver1.setNik("1111111111111111");
        careGiver1.setAddress("Address 1");
        careGiver1.setPhoneNumber("081111111111");
        careGiver1.setSpeciality("Cardiology");
        careGiver1.setWorkAddress("Hospital 1");

        CareGiver careGiver2 = new CareGiver();
        careGiver2.setEmail("johnson@example.com");
        careGiver2.setPassword("password");
        careGiver2.setName("Dr. Johnson");
        careGiver2.setNik("2222222222222222");
        careGiver2.setAddress("Address 2");
        careGiver2.setPhoneNumber("082222222222");
        careGiver2.setSpeciality("Neurology");
        careGiver2.setWorkAddress("Hospital 2");

        CareGiver careGiver3 = new CareGiver();
        careGiver3.setEmail("smithson@example.com");
        careGiver3.setPassword("password");
        careGiver3.setName("Dr. Smithson");
        careGiver3.setNik("3333333333333333");
        careGiver3.setAddress("Address 3");
        careGiver3.setPhoneNumber("083333333333");
        careGiver3.setSpeciality("Dermatology");
        careGiver3.setWorkAddress("Hospital 3");

        entityManager.persistAndFlush(careGiver1);
        entityManager.persistAndFlush(careGiver2);
        entityManager.persistAndFlush(careGiver3);

        // when
        List<CareGiver> foundBySmith = careGiverRepository.findByNameContainingIgnoreCase("smith");
        List<CareGiver> foundByDr = careGiverRepository.findByNameContainingIgnoreCase("dr");
        List<CareGiver> foundByNonexistent = careGiverRepository.findByNameContainingIgnoreCase("nonexistent");

        // then
        assertThat(foundBySmith).hasSize(2);
        assertThat(foundBySmith).extracting(CareGiver::getName)
                .containsExactlyInAnyOrder("Dr. Smith", "Dr. Smithson");

        assertThat(foundByDr).hasSize(3);
        assertThat(foundByDr).extracting(CareGiver::getName)
                .containsExactlyInAnyOrder("Dr. Smith", "Dr. Johnson", "Dr. Smithson");

        assertThat(foundByNonexistent).isEmpty();
    }

    @Test
    void findBySpecialityContainingIgnoreCase_ShouldReturnMatchingCareGivers() {
        // given
        CareGiver careGiver1 = new CareGiver();
        careGiver1.setEmail("cardio1@example.com");
        careGiver1.setPassword("password");
        careGiver1.setName("Dr. Cardio One");
        careGiver1.setNik("4444444444444444");
        careGiver1.setAddress("Address 4");
        careGiver1.setPhoneNumber("084444444444");
        careGiver1.setSpeciality("Cardiology");
        careGiver1.setWorkAddress("Hospital 4");

        CareGiver careGiver2 = new CareGiver();
        careGiver2.setEmail("neuro@example.com");
        careGiver2.setPassword("password");
        careGiver2.setName("Dr. Neuro");
        careGiver2.setNik("5555555555555555");
        careGiver2.setAddress("Address 5");
        careGiver2.setPhoneNumber("085555555555");
        careGiver2.setSpeciality("Neurology");
        careGiver2.setWorkAddress("Hospital 5");

        CareGiver careGiver3 = new CareGiver();
        careGiver3.setEmail("cardio2@example.com");
        careGiver3.setPassword("password");
        careGiver3.setName("Dr. Cardio Two");
        careGiver3.setNik("6666666666666666");
        careGiver3.setAddress("Address 6");
        careGiver3.setPhoneNumber("086666666666");
        careGiver3.setSpeciality("Cardiology");
        careGiver3.setWorkAddress("Hospital 6");

        entityManager.persistAndFlush(careGiver1);
        entityManager.persistAndFlush(careGiver2);
        entityManager.persistAndFlush(careGiver3);

        // when
        List<CareGiver> foundByCardio = careGiverRepository.findBySpecialityContainingIgnoreCase("cardio");
        List<CareGiver> foundByNeuro = careGiverRepository.findBySpecialityContainingIgnoreCase("neuro");
        List<CareGiver> foundByLogy = careGiverRepository.findBySpecialityContainingIgnoreCase("logy");
        List<CareGiver> foundByNonexistent = careGiverRepository.findBySpecialityContainingIgnoreCase("nonexistent");

        // then
        assertThat(foundByCardio).hasSize(2);
        assertThat(foundByCardio).extracting(CareGiver::getEmail)
                .containsExactlyInAnyOrder("cardio1@example.com", "cardio2@example.com");

        assertThat(foundByNeuro).hasSize(1);
        assertThat(foundByNeuro.get(0).getEmail()).isEqualTo("neuro@example.com");

        assertThat(foundByLogy).hasSize(3);
        assertThat(foundByLogy).extracting(CareGiver::getEmail)
                .containsExactlyInAnyOrder("cardio1@example.com", "neuro@example.com", "cardio2@example.com");

        assertThat(foundByNonexistent).isEmpty();
    }

    @Test
    void findByNameAndSpeciality_ShouldReturnMatchingCareGivers() {
        // given
        CareGiver careGiver1 = new CareGiver();
        careGiver1.setEmail("smith.cardio@example.com");
        careGiver1.setPassword("password");
        careGiver1.setName("Dr. Smith");
        careGiver1.setNik("7777777777777777");
        careGiver1.setAddress("Address 7");
        careGiver1.setPhoneNumber("087777777777");
        careGiver1.setSpeciality("Cardiology");
        careGiver1.setWorkAddress("Hospital 7");

        CareGiver careGiver2 = new CareGiver();
        careGiver2.setEmail("smith.neuro@example.com");
        careGiver2.setPassword("password");
        careGiver2.setName("Dr. Smith");
        careGiver2.setNik("8888888888888888");
        careGiver2.setAddress("Address 8");
        careGiver2.setPhoneNumber("088888888888");
        careGiver2.setSpeciality("Neurology");
        careGiver2.setWorkAddress("Hospital 8");

        CareGiver careGiver3 = new CareGiver();
        careGiver3.setEmail("johnson.cardio@example.com");
        careGiver3.setPassword("password");
        careGiver3.setName("Dr. Johnson");
        careGiver3.setNik("9999999999999999");
        careGiver3.setAddress("Address 9");
        careGiver3.setPhoneNumber("089999999999");
        careGiver3.setSpeciality("Cardiology");
        careGiver3.setWorkAddress("Hospital 9");

        entityManager.persistAndFlush(careGiver1);
        entityManager.persistAndFlush(careGiver2);
        entityManager.persistAndFlush(careGiver3);

        // when
        List<CareGiver> foundBySmithCardio = careGiverRepository.findByNameAndSpeciality("Smith", "Cardiology");
        List<CareGiver> foundBySmithNeuro = careGiverRepository.findByNameAndSpeciality("Smith", "Neurology");
        List<CareGiver> foundByJohnsonCardio = careGiverRepository.findByNameAndSpeciality("Johnson", "Cardiology");
        List<CareGiver> foundByNonexistent = careGiverRepository.findByNameAndSpeciality("nonexistent", "nonexistent");

        // then
        assertThat(foundBySmithCardio).hasSize(1);
        assertThat(foundBySmithCardio.get(0).getEmail()).isEqualTo("smith.cardio@example.com");

        assertThat(foundBySmithNeuro).hasSize(1);
        assertThat(foundBySmithNeuro.get(0).getEmail()).isEqualTo("smith.neuro@example.com");

        assertThat(foundByJohnsonCardio).hasSize(1);
        assertThat(foundByJohnsonCardio.get(0).getEmail()).isEqualTo("johnson.cardio@example.com");

        assertThat(foundByNonexistent).isEmpty();
    }

    @Test
    void findByAvailableDayOfWeek_ShouldReturnCareGiversWithAvailableSchedule() {
        // given
        CareGiver careGiver1 = new CareGiver();
        careGiver1.setEmail("monday.doc@example.com");
        careGiver1.setPassword("password");
        careGiver1.setName("Monday Doctor");
        careGiver1.setNik("1010101010101010");
        careGiver1.setAddress("Address 10");
        careGiver1.setPhoneNumber("0810101010");
        careGiver1.setSpeciality("General");
        careGiver1.setWorkAddress("Hospital 10");

        WorkingSchedule mondaySchedule = new WorkingSchedule();
        mondaySchedule.setDayOfWeek(DayOfWeek.MONDAY);
        mondaySchedule.setStartTime(LocalTime.of(8, 0));
        mondaySchedule.setEndTime(LocalTime.of(16, 0));
        mondaySchedule.setAvailable(true);
        careGiver1.addWorkingSchedule(mondaySchedule);

        CareGiver careGiver2 = new CareGiver();
        careGiver2.setEmail("tuesday.doc@example.com");
        careGiver2.setPassword("password");
        careGiver2.setName("Tuesday Doctor");
        careGiver2.setNik("2020202020202020");
        careGiver2.setAddress("Address 20");
        careGiver2.setPhoneNumber("0820202020");
        careGiver2.setSpeciality("General");
        careGiver2.setWorkAddress("Hospital 20");

        WorkingSchedule tuesdaySchedule = new WorkingSchedule();
        tuesdaySchedule.setDayOfWeek(DayOfWeek.TUESDAY);
        tuesdaySchedule.setStartTime(LocalTime.of(9, 0));
        tuesdaySchedule.setEndTime(LocalTime.of(17, 0));
        tuesdaySchedule.setAvailable(true);
        careGiver2.addWorkingSchedule(tuesdaySchedule);

        // Doctor with unavailable Monday schedule
        CareGiver careGiver3 = new CareGiver();
        careGiver3.setEmail("busy.monday@example.com");
        careGiver3.setPassword("password");
        careGiver3.setName("Busy Monday Doctor");
        careGiver3.setNik("3030303030303030");
        careGiver3.setAddress("Address 30");
        careGiver3.setPhoneNumber("0830303030");
        careGiver3.setSpeciality("General");
        careGiver3.setWorkAddress("Hospital 30");

        WorkingSchedule busyMondaySchedule = new WorkingSchedule();
        busyMondaySchedule.setDayOfWeek(DayOfWeek.MONDAY);
        busyMondaySchedule.setStartTime(LocalTime.of(10, 0));
        busyMondaySchedule.setEndTime(LocalTime.of(18, 0));
        busyMondaySchedule.setAvailable(false);
        careGiver3.addWorkingSchedule(busyMondaySchedule);

        entityManager.persistAndFlush(careGiver1);
        entityManager.persistAndFlush(careGiver2);
        entityManager.persistAndFlush(careGiver3);

        // when
        List<CareGiver> foundByMonday = careGiverRepository.findByAvailableDayOfWeek(DayOfWeek.MONDAY);
        List<CareGiver> foundByTuesday = careGiverRepository.findByAvailableDayOfWeek(DayOfWeek.TUESDAY);
        List<CareGiver> foundByWednesday = careGiverRepository.findByAvailableDayOfWeek(DayOfWeek.WEDNESDAY);

        // then
        assertThat(foundByMonday).hasSize(1);
        assertThat(foundByMonday.get(0).getEmail()).isEqualTo("monday.doc@example.com");

        assertThat(foundByTuesday).hasSize(1);
        assertThat(foundByTuesday.get(0).getEmail()).isEqualTo("tuesday.doc@example.com");

        assertThat(foundByWednesday).isEmpty();
    }
}