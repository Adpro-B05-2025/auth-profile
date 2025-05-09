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

    @Test
    void findByAvailableDayAndTime_ShouldReturnCareGiversAvailableAtSpecificTime() {
        // given
        // Doctor available Monday morning
        CareGiver morningDoctor = new CareGiver();
        morningDoctor.setEmail("morning.doc@example.com");
        morningDoctor.setPassword("password");
        morningDoctor.setName("Morning Doctor");
        morningDoctor.setNik("1111222233334444");
        morningDoctor.setAddress("Morning Address");
        morningDoctor.setPhoneNumber("0811223344");
        morningDoctor.setSpeciality("General");
        morningDoctor.setWorkAddress("Morning Hospital");

        WorkingSchedule morningSchedule = new WorkingSchedule();
        morningSchedule.setDayOfWeek(DayOfWeek.MONDAY);
        morningSchedule.setStartTime(LocalTime.of(8, 0));
        morningSchedule.setEndTime(LocalTime.of(12, 0));
        morningSchedule.setAvailable(true);
        morningDoctor.addWorkingSchedule(morningSchedule);

        // Doctor available Monday afternoon
        CareGiver afternoonDoctor = new CareGiver();
        afternoonDoctor.setEmail("afternoon.doc@example.com");
        afternoonDoctor.setPassword("password");
        afternoonDoctor.setName("Afternoon Doctor");
        afternoonDoctor.setNik("5555666677778888");
        afternoonDoctor.setAddress("Afternoon Address");
        afternoonDoctor.setPhoneNumber("0855667788");
        afternoonDoctor.setSpeciality("General");
        afternoonDoctor.setWorkAddress("Afternoon Hospital");

        WorkingSchedule afternoonSchedule = new WorkingSchedule();
        afternoonSchedule.setDayOfWeek(DayOfWeek.MONDAY);
        afternoonSchedule.setStartTime(LocalTime.of(13, 0));
        afternoonSchedule.setEndTime(LocalTime.of(17, 0));
        afternoonSchedule.setAvailable(true);
        afternoonDoctor.addWorkingSchedule(afternoonSchedule);

        // Doctor available Tuesday all day
        CareGiver tuesdayDoctor = new CareGiver();
        tuesdayDoctor.setEmail("tuesday.all.day@example.com");
        tuesdayDoctor.setPassword("password");
        tuesdayDoctor.setName("Tuesday All Day");
        tuesdayDoctor.setNik("9999000011112222");
        tuesdayDoctor.setAddress("Tuesday Address");
        tuesdayDoctor.setPhoneNumber("0899001122");
        tuesdayDoctor.setSpeciality("General");
        tuesdayDoctor.setWorkAddress("Tuesday Hospital");

        WorkingSchedule tuesdaySchedule = new WorkingSchedule();
        tuesdaySchedule.setDayOfWeek(DayOfWeek.TUESDAY);
        tuesdaySchedule.setStartTime(LocalTime.of(8, 0));
        tuesdaySchedule.setEndTime(LocalTime.of(17, 0));
        tuesdaySchedule.setAvailable(true);
        tuesdayDoctor.addWorkingSchedule(tuesdaySchedule);

        entityManager.persistAndFlush(morningDoctor);
        entityManager.persistAndFlush(afternoonDoctor);
        entityManager.persistAndFlush(tuesdayDoctor);

        // when
        List<CareGiver> foundMondayMorning = careGiverRepository.findByAvailableDayAndTime(
                DayOfWeek.MONDAY, LocalTime.of(10, 0));
        List<CareGiver> foundMondayAfternoon = careGiverRepository.findByAvailableDayAndTime(
                DayOfWeek.MONDAY, LocalTime.of(15, 0));
        List<CareGiver> foundMondayLunchtime = careGiverRepository.findByAvailableDayAndTime(
                DayOfWeek.MONDAY, LocalTime.of(12, 30));
        List<CareGiver> foundTuesdayMorning = careGiverRepository.findByAvailableDayAndTime(
                DayOfWeek.TUESDAY, LocalTime.of(9, 0));

        // then
        assertThat(foundMondayMorning).hasSize(1);
        assertThat(foundMondayMorning.get(0).getEmail()).isEqualTo("morning.doc@example.com");

        assertThat(foundMondayAfternoon).hasSize(1);
        assertThat(foundMondayAfternoon.get(0).getEmail()).isEqualTo("afternoon.doc@example.com");

        assertThat(foundMondayLunchtime).isEmpty();

        assertThat(foundTuesdayMorning).hasSize(1);
        assertThat(foundTuesdayMorning.get(0).getEmail()).isEqualTo("tuesday.all.day@example.com");
    }

    @Test
    void findByNameAndAvailableDayAndTime_ShouldFilterByNameAndSchedule() {
        // given
        // Set up two doctors with the same name but different schedules
        CareGiver smithMorning = new CareGiver();
        smithMorning.setEmail("smith.morning@example.com");
        smithMorning.setPassword("password");
        smithMorning.setName("Dr. Smith");
        smithMorning.setNik("1212121212121212");
        smithMorning.setAddress("Smith Morning Address");
        smithMorning.setPhoneNumber("0812121212");
        smithMorning.setSpeciality("General");
        smithMorning.setWorkAddress("Smith Morning Hospital");

        WorkingSchedule smithMorningSchedule = new WorkingSchedule();
        smithMorningSchedule.setDayOfWeek(DayOfWeek.MONDAY);
        smithMorningSchedule.setStartTime(LocalTime.of(8, 0));
        smithMorningSchedule.setEndTime(LocalTime.of(12, 0));
        smithMorningSchedule.setAvailable(true);
        smithMorning.addWorkingSchedule(smithMorningSchedule);

        CareGiver smithAfternoon = new CareGiver();
        smithAfternoon.setEmail("smith.afternoon@example.com");
        smithAfternoon.setPassword("password");
        smithAfternoon.setName("Dr. Smith");
        smithAfternoon.setNik("3434343434343434");
        smithAfternoon.setAddress("Smith Afternoon Address");
        smithAfternoon.setPhoneNumber("0834343434");
        smithAfternoon.setSpeciality("General");
        smithAfternoon.setWorkAddress("Smith Afternoon Hospital");

        WorkingSchedule smithAfternoonSchedule = new WorkingSchedule();
        smithAfternoonSchedule.setDayOfWeek(DayOfWeek.MONDAY);
        smithAfternoonSchedule.setStartTime(LocalTime.of(13, 0));
        smithAfternoonSchedule.setEndTime(LocalTime.of(17, 0));
        smithAfternoonSchedule.setAvailable(true);
        smithAfternoon.addWorkingSchedule(smithAfternoonSchedule);

        // Different name doctor
        CareGiver johnsonMorning = new CareGiver();
        johnsonMorning.setEmail("johnson.morning@example.com");
        johnsonMorning.setPassword("password");
        johnsonMorning.setName("Dr. Johnson");
        johnsonMorning.setNik("5656565656565656");
        johnsonMorning.setAddress("Johnson Morning Address");
        johnsonMorning.setPhoneNumber("0856565656");
        johnsonMorning.setSpeciality("General");
        johnsonMorning.setWorkAddress("Johnson Morning Hospital");

        WorkingSchedule johnsonMorningSchedule = new WorkingSchedule();
        johnsonMorningSchedule.setDayOfWeek(DayOfWeek.MONDAY);
        johnsonMorningSchedule.setStartTime(LocalTime.of(8, 0));
        johnsonMorningSchedule.setEndTime(LocalTime.of(12, 0));
        johnsonMorningSchedule.setAvailable(true);
        johnsonMorning.addWorkingSchedule(johnsonMorningSchedule);

        entityManager.persistAndFlush(smithMorning);
        entityManager.persistAndFlush(smithAfternoon);
        entityManager.persistAndFlush(johnsonMorning);

        // when
        List<CareGiver> smithMondayMorning = careGiverRepository.findByNameAndAvailableDayAndTime(
                "Smith", DayOfWeek.MONDAY, LocalTime.of(10, 0));
        List<CareGiver> smithMondayAfternoon = careGiverRepository.findByNameAndAvailableDayAndTime(
                "Smith", DayOfWeek.MONDAY, LocalTime.of(15, 0));
        List<CareGiver> johnsonMondayMorning = careGiverRepository.findByNameAndAvailableDayAndTime(
                "Johnson", DayOfWeek.MONDAY, LocalTime.of(10, 0));
        List<CareGiver> nonexistentDoctor = careGiverRepository.findByNameAndAvailableDayAndTime(
                "Nonexistent", DayOfWeek.MONDAY, LocalTime.of(10, 0));

        // then
        assertThat(smithMondayMorning).hasSize(1);
        assertThat(smithMondayMorning.get(0).getEmail()).isEqualTo("smith.morning@example.com");

        assertThat(smithMondayAfternoon).hasSize(1);
        assertThat(smithMondayAfternoon.get(0).getEmail()).isEqualTo("smith.afternoon@example.com");

        assertThat(johnsonMondayMorning).hasSize(1);
        assertThat(johnsonMondayMorning.get(0).getEmail()).isEqualTo("johnson.morning@example.com");

        assertThat(nonexistentDoctor).isEmpty();
    }

    @Test
    void findBySpecialityAndAvailableDayAndTime_ShouldFilterBySpecialityAndSchedule() {
        // given
        // Set up two doctors with the same speciality but different schedules
        CareGiver cardioMorning = new CareGiver();
        cardioMorning.setEmail("cardio.morning@example.com");
        cardioMorning.setPassword("password");
        cardioMorning.setName("Morning Cardiologist");
        cardioMorning.setNik("7878787878787878");
        cardioMorning.setAddress("Cardio Morning Address");
        cardioMorning.setPhoneNumber("0878787878");
        cardioMorning.setSpeciality("Cardiology");
        cardioMorning.setWorkAddress("Cardio Morning Hospital");

        WorkingSchedule cardioMorningSchedule = new WorkingSchedule();
        cardioMorningSchedule.setDayOfWeek(DayOfWeek.MONDAY);
        cardioMorningSchedule.setStartTime(LocalTime.of(8, 0));
        cardioMorningSchedule.setEndTime(LocalTime.of(12, 0));
        cardioMorningSchedule.setAvailable(true);
        cardioMorning.addWorkingSchedule(cardioMorningSchedule);

        CareGiver cardioAfternoon = new CareGiver();
        cardioAfternoon.setEmail("cardio.afternoon@example.com");
        cardioAfternoon.setPassword("password");
        cardioAfternoon.setName("Afternoon Cardiologist");
        cardioAfternoon.setNik("9090909090909090");
        cardioAfternoon.setAddress("Cardio Afternoon Address");
        cardioAfternoon.setPhoneNumber("0890909090");
        cardioAfternoon.setSpeciality("Cardiology");
        cardioAfternoon.setWorkAddress("Cardio Afternoon Hospital");

        WorkingSchedule cardioAfternoonSchedule = new WorkingSchedule();
        cardioAfternoonSchedule.setDayOfWeek(DayOfWeek.MONDAY);
        cardioAfternoonSchedule.setStartTime(LocalTime.of(13, 0));
        cardioAfternoonSchedule.setEndTime(LocalTime.of(17, 0));
        cardioAfternoonSchedule.setAvailable(true);
        cardioAfternoon.addWorkingSchedule(cardioAfternoonSchedule);

        // Different speciality doctor
        CareGiver neuroMorning = new CareGiver();
        neuroMorning.setEmail("neuro.morning@example.com");
        neuroMorning.setPassword("password");
        neuroMorning.setName("Morning Neurologist");
        neuroMorning.setNik("1231231231231231");
        neuroMorning.setAddress("Neuro Morning Address");
        neuroMorning.setPhoneNumber("0812312312");
        neuroMorning.setSpeciality("Neurology");
        neuroMorning.setWorkAddress("Neuro Morning Hospital");

        WorkingSchedule neuroMorningSchedule = new WorkingSchedule();
        neuroMorningSchedule.setDayOfWeek(DayOfWeek.MONDAY);
        neuroMorningSchedule.setStartTime(LocalTime.of(8, 0));
        neuroMorningSchedule.setEndTime(LocalTime.of(12, 0));
        neuroMorningSchedule.setAvailable(true);
        neuroMorning.addWorkingSchedule(neuroMorningSchedule);

        entityManager.persistAndFlush(cardioMorning);
        entityManager.persistAndFlush(cardioAfternoon);
        entityManager.persistAndFlush(neuroMorning);

        // when
        List<CareGiver> cardioMondayMorning = careGiverRepository.findBySpecialityAndAvailableDayAndTime(
                "Cardio", DayOfWeek.MONDAY, LocalTime.of(10, 0));
        List<CareGiver> cardioMondayAfternoon = careGiverRepository.findBySpecialityAndAvailableDayAndTime(
                "Cardio", DayOfWeek.MONDAY, LocalTime.of(15, 0));
        List<CareGiver> neuroMondayMorning = careGiverRepository.findBySpecialityAndAvailableDayAndTime(
                "Neuro", DayOfWeek.MONDAY, LocalTime.of(10, 0));
        List<CareGiver> nonexistentSpeciality = careGiverRepository.findBySpecialityAndAvailableDayAndTime(
                "Nonexistent", DayOfWeek.MONDAY, LocalTime.of(10, 0));

        // then
        assertThat(cardioMondayMorning).hasSize(1);
        assertThat(cardioMondayMorning.get(0).getEmail()).isEqualTo("cardio.morning@example.com");

        assertThat(cardioMondayAfternoon).hasSize(1);
        assertThat(cardioMondayAfternoon.get(0).getEmail()).isEqualTo("cardio.afternoon@example.com");

        assertThat(neuroMondayMorning).hasSize(1);
        assertThat(neuroMondayMorning.get(0).getEmail()).isEqualTo("neuro.morning@example.com");

        assertThat(nonexistentSpeciality).isEmpty();
    }

    @Test
    void findByNameAndSpecialityAndAvailableDayAndTime_ShouldFilterByNameSpecialityAndSchedule() {
        // given
        // Set up doctors with different combinations of name and speciality
        CareGiver smithCardioMorning = new CareGiver();
        smithCardioMorning.setEmail("smith.cardio.morning@example.com");
        smithCardioMorning.setPassword("password");
        smithCardioMorning.setName("Dr. Smith");
        smithCardioMorning.setNik("4545454545454545");
        smithCardioMorning.setAddress("Smith Cardio Morning Address");
        smithCardioMorning.setPhoneNumber("0845454545");
        smithCardioMorning.setSpeciality("Cardiology");
        smithCardioMorning.setWorkAddress("Smith Cardio Morning Hospital");

        WorkingSchedule smithCardioMorningSchedule = new WorkingSchedule();
        smithCardioMorningSchedule.setDayOfWeek(DayOfWeek.MONDAY);
        smithCardioMorningSchedule.setStartTime(LocalTime.of(8, 0));
        smithCardioMorningSchedule.setEndTime(LocalTime.of(12, 0));
        smithCardioMorningSchedule.setAvailable(true);
        smithCardioMorning.addWorkingSchedule(smithCardioMorningSchedule);

        CareGiver smithNeuroMorning = new CareGiver();
        smithNeuroMorning.setEmail("smith.neuro.morning@example.com");
        smithNeuroMorning.setPassword("password");
        smithNeuroMorning.setName("Dr. Smith");
        smithNeuroMorning.setNik("6767676767676767");
        smithNeuroMorning.setAddress("Smith Neuro Morning Address");
        smithNeuroMorning.setPhoneNumber("0867676767");
        smithNeuroMorning.setSpeciality("Neurology");
        smithNeuroMorning.setWorkAddress("Smith Neuro Morning Hospital");

        WorkingSchedule smithNeuroMorningSchedule = new WorkingSchedule();
        smithNeuroMorningSchedule.setDayOfWeek(DayOfWeek.MONDAY);
        smithNeuroMorningSchedule.setStartTime(LocalTime.of(8, 0));
        smithNeuroMorningSchedule.setEndTime(LocalTime.of(12, 0));
        smithNeuroMorningSchedule.setAvailable(true);
        smithNeuroMorning.addWorkingSchedule(smithNeuroMorningSchedule);

        CareGiver johnsonCardioMorning = new CareGiver();
        johnsonCardioMorning.setEmail("johnson.cardio.morning@example.com");
        johnsonCardioMorning.setPassword("password");
        johnsonCardioMorning.setName("Dr. Johnson");
        johnsonCardioMorning.setNik("8989898989898989");
        johnsonCardioMorning.setAddress("Johnson Cardio Morning Address");
        johnsonCardioMorning.setPhoneNumber("0889898989");
        johnsonCardioMorning.setSpeciality("Cardiology");
        johnsonCardioMorning.setWorkAddress("Johnson Cardio Morning Hospital");

        WorkingSchedule johnsonCardioMorningSchedule = new WorkingSchedule();
        johnsonCardioMorningSchedule.setDayOfWeek(DayOfWeek.MONDAY);
        johnsonCardioMorningSchedule.setStartTime(LocalTime.of(8, 0));
        johnsonCardioMorningSchedule.setEndTime(LocalTime.of(12, 0));
        johnsonCardioMorningSchedule.setAvailable(true);
        johnsonCardioMorning.addWorkingSchedule(johnsonCardioMorningSchedule);

        CareGiver smithCardioAfternoon = new CareGiver();
        smithCardioAfternoon.setEmail("smith.cardio.afternoon@example.com");
        smithCardioAfternoon.setPassword("password");
        smithCardioAfternoon.setName("Dr. Smith");
        smithCardioAfternoon.setNik("0101010101010101");
        smithCardioAfternoon.setAddress("Smith Cardio Afternoon Address");
        smithCardioAfternoon.setPhoneNumber("0801010101");
        smithCardioAfternoon.setSpeciality("Cardiology");
        smithCardioAfternoon.setWorkAddress("Smith Cardio Afternoon Hospital");

        WorkingSchedule smithCardioAfternoonSchedule = new WorkingSchedule();
        smithCardioAfternoonSchedule.setDayOfWeek(DayOfWeek.MONDAY);
        smithCardioAfternoonSchedule.setStartTime(LocalTime.of(13, 0));
        smithCardioAfternoonSchedule.setEndTime(LocalTime.of(17, 0));
        smithCardioAfternoonSchedule.setAvailable(true);
        smithCardioAfternoon.addWorkingSchedule(smithCardioAfternoonSchedule);

        entityManager.persistAndFlush(smithCardioMorning);
        entityManager.persistAndFlush(smithNeuroMorning);
        entityManager.persistAndFlush(johnsonCardioMorning);
        entityManager.persistAndFlush(smithCardioAfternoon);

        // when
        List<CareGiver> smithCardioMorningResults = careGiverRepository.findByNameAndSpecialityAndAvailableDayAndTime(
                "Smith", "Cardio", DayOfWeek.MONDAY, LocalTime.of(10, 0));
        List<CareGiver> smithNeuroMorningResults = careGiverRepository.findByNameAndSpecialityAndAvailableDayAndTime(
                "Smith", "Neuro", DayOfWeek.MONDAY, LocalTime.of(10, 0));
        List<CareGiver> johnsonCardioMorningResults = careGiverRepository.findByNameAndSpecialityAndAvailableDayAndTime(
                "Johnson", "Cardio", DayOfWeek.MONDAY, LocalTime.of(10, 0));
        List<CareGiver> smithCardioAfternoonResults = careGiverRepository.findByNameAndSpecialityAndAvailableDayAndTime(
                "Smith", "Cardio", DayOfWeek.MONDAY, LocalTime.of(15, 0));
        List<CareGiver> nonexistentResults = careGiverRepository.findByNameAndSpecialityAndAvailableDayAndTime(
                "Nonexistent", "Nonexistent", DayOfWeek.MONDAY, LocalTime.of(10, 0));

        // then
        assertThat(smithCardioMorningResults).hasSize(1);
        assertThat(smithCardioMorningResults.get(0).getEmail()).isEqualTo("smith.cardio.morning@example.com");

        assertThat(smithNeuroMorningResults).hasSize(1);
        assertThat(smithNeuroMorningResults.get(0).getEmail()).isEqualTo("smith.neuro.morning@example.com");

        assertThat(johnsonCardioMorningResults).hasSize(1);
        assertThat(johnsonCardioMorningResults.get(0).getEmail()).isEqualTo("johnson.cardio.morning@example.com");

        assertThat(smithCardioAfternoonResults).hasSize(1);
        assertThat(smithCardioAfternoonResults.get(0).getEmail()).isEqualTo("smith.cardio.afternoon@example.com");

        assertThat(nonexistentResults).isEmpty();
    }
}