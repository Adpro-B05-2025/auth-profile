package id.ac.ui.cs.advprog.authprofile.factory;

import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterCareGiverRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterPacillianRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class UserFactoryProviderTest {

    @Mock
    private PacillianFactory pacillianFactory;

    @Mock
    private CareGiverFactory careGiverFactory;

    private UserFactoryProvider factoryProvider;
    private RegisterPacillianRequest pacillianRequest;
    private RegisterCareGiverRequest careGiverRequest;

    @BeforeEach
    void setUp() {
        factoryProvider = new UserFactoryProvider(pacillianFactory, careGiverFactory);

        // Set up pacillian request
        pacillianRequest = new RegisterPacillianRequest();
        pacillianRequest.setEmail("pacillian@example.com");
        pacillianRequest.setPassword("password");
        pacillianRequest.setName("Test Pacillian");
        pacillianRequest.setNik("1234567890123456");
        pacillianRequest.setAddress("Test Address");
        pacillianRequest.setPhoneNumber("081234567890");
        pacillianRequest.setMedicalHistory("No significant history");

        // Set up working schedule for caregiver
        List<RegisterCareGiverRequest.WorkingScheduleRequest> schedules = new ArrayList<>();
        RegisterCareGiverRequest.WorkingScheduleRequest schedule = new RegisterCareGiverRequest.WorkingScheduleRequest();
        schedule.setDayOfWeek(DayOfWeek.MONDAY);
        schedule.setStartTime(LocalTime.of(8, 0));
        schedule.setEndTime(LocalTime.of(16, 0));
        schedules.add(schedule);

        // Set up caregiver request
        careGiverRequest = new RegisterCareGiverRequest();
        careGiverRequest.setEmail("caregiver@example.com");
        careGiverRequest.setPassword("password");
        careGiverRequest.setName("Dr. Test");
        careGiverRequest.setNik("6543210987654321");
        careGiverRequest.setAddress("Doctor Address");
        careGiverRequest.setPhoneNumber("089876543210");
        careGiverRequest.setSpeciality("General");
        careGiverRequest.setWorkAddress("Test Hospital");
        careGiverRequest.setWorkingSchedules(schedules);
    }

    @Test
    void getFactory_WithPacillianRequest_ShouldReturnPacillianFactory() {
        // when
        UserFactory factory = factoryProvider.getFactory(pacillianRequest);

        // then
        assertThat(factory).isEqualTo(pacillianFactory);
    }

    @Test
    void getFactory_WithCareGiverRequest_ShouldReturnCareGiverFactory() {
        // when
        UserFactory factory = factoryProvider.getFactory(careGiverRequest);

        // then
        assertThat(factory).isEqualTo(careGiverFactory);
    }

    @Test
    void getFactory_WithNullRequest_ShouldThrowException() {
        // when/then
        assertThatThrownBy(() -> factoryProvider.getFactory(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported registration request type");
    }
}