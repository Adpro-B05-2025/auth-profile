package id.ac.ui.cs.advprog.authprofile.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class WorkingScheduleTest {

    private WorkingSchedule workingSchedule;
    private CareGiver careGiver;
    private final DayOfWeek dayOfWeek = DayOfWeek.MONDAY;
    private final LocalTime startTime = LocalTime.of(9, 0);
    private final LocalTime endTime = LocalTime.of(17, 0);

    @BeforeEach
    void setUp() {
        careGiver = new CareGiver();
        careGiver.setId(1L);
        careGiver.setName("Dr. Test");
        careGiver.setSpeciality("General");

        workingSchedule = new WorkingSchedule();
        workingSchedule.setId(1L);
        workingSchedule.setDayOfWeek(dayOfWeek);
        workingSchedule.setStartTime(startTime);
        workingSchedule.setEndTime(endTime);
        workingSchedule.setCareGiver(careGiver);
        workingSchedule.setAvailable(true);
    }

    @Test
    void testWorkingScheduleCreation() {
        assertNotNull(workingSchedule);
        assertEquals(1L, workingSchedule.getId());
        assertEquals(dayOfWeek, workingSchedule.getDayOfWeek());
        assertEquals(startTime, workingSchedule.getStartTime());
        assertEquals(endTime, workingSchedule.getEndTime());
        assertEquals(careGiver, workingSchedule.getCareGiver());
        assertTrue(workingSchedule.isAvailable());
    }

    @Test
    void testParameterizedConstructor() {
        WorkingSchedule newSchedule = new WorkingSchedule(dayOfWeek, startTime, endTime, careGiver);

        assertNotNull(newSchedule);
        assertEquals(dayOfWeek, newSchedule.getDayOfWeek());
        assertEquals(startTime, newSchedule.getStartTime());
        assertEquals(endTime, newSchedule.getEndTime());
        assertEquals(careGiver, newSchedule.getCareGiver());
        assertTrue(newSchedule.isAvailable());  // Default value should be true
    }

    @Test
    void testFullParameterizedConstructor() {
        Long id = 2L;
        CareGiver newCareGiver = new CareGiver();
        newCareGiver.setId(2L);
        boolean isAvailable = false;

        WorkingSchedule fullSchedule = new WorkingSchedule(id, dayOfWeek, startTime, endTime, newCareGiver, isAvailable);

        assertNotNull(fullSchedule);
        assertEquals(id, fullSchedule.getId());
        assertEquals(dayOfWeek, fullSchedule.getDayOfWeek());
        assertEquals(startTime, fullSchedule.getStartTime());
        assertEquals(endTime, fullSchedule.getEndTime());
        assertEquals(newCareGiver, fullSchedule.getCareGiver());
        assertEquals(isAvailable, fullSchedule.isAvailable());
    }

    @Test
    void testSetters() {
        Long newId = 3L;
        DayOfWeek newDayOfWeek = DayOfWeek.TUESDAY;
        LocalTime newStartTime = LocalTime.of(10, 0);
        LocalTime newEndTime = LocalTime.of(18, 0);
        CareGiver newCareGiver = new CareGiver();
        newCareGiver.setId(3L);
        boolean newAvailability = false;

        workingSchedule.setId(newId);
        workingSchedule.setDayOfWeek(newDayOfWeek);
        workingSchedule.setStartTime(newStartTime);
        workingSchedule.setEndTime(newEndTime);
        workingSchedule.setCareGiver(newCareGiver);
        workingSchedule.setAvailable(newAvailability);

        assertEquals(newId, workingSchedule.getId());
        assertEquals(newDayOfWeek, workingSchedule.getDayOfWeek());
        assertEquals(newStartTime, workingSchedule.getStartTime());
        assertEquals(newEndTime, workingSchedule.getEndTime());
        assertEquals(newCareGiver, workingSchedule.getCareGiver());
        assertEquals(newAvailability, workingSchedule.isAvailable());
    }

    @Test
    void testEqualsAndHashCode() {
        WorkingSchedule schedule1 = new WorkingSchedule();
        schedule1.setId(1L);
        schedule1.setDayOfWeek(DayOfWeek.MONDAY);

        WorkingSchedule schedule2 = new WorkingSchedule();
        schedule2.setId(1L);
        schedule2.setDayOfWeek(DayOfWeek.MONDAY);

        WorkingSchedule schedule3 = new WorkingSchedule();
        schedule3.setId(2L);
        schedule3.setDayOfWeek(DayOfWeek.TUESDAY);

        // Test equality
        assertEquals(schedule1, schedule2);
        assertNotEquals(schedule1, schedule3);

        // Test hash code
        assertEquals(schedule1.hashCode(), schedule2.hashCode());
        assertNotEquals(schedule1.hashCode(), schedule3.hashCode());
    }

    @Test
    void testToString() {
        String scheduleString = workingSchedule.toString();

        // Verify that toString contains important fields
        assertTrue(scheduleString.contains(workingSchedule.getDayOfWeek().toString()));
        assertTrue(scheduleString.contains(workingSchedule.getStartTime().toString()));
        assertTrue(scheduleString.contains(workingSchedule.getEndTime().toString()));
        assertTrue(scheduleString.contains(String.valueOf(workingSchedule.isAvailable())));
    }

    @Test
    void testTimeValidation() {
        // Create a schedule with start time after end time
        LocalTime invalidStartTime = LocalTime.of(18, 0);
        LocalTime invalidEndTime = LocalTime.of(9, 0);

        WorkingSchedule invalidSchedule = new WorkingSchedule();
        invalidSchedule.setStartTime(invalidStartTime);
        invalidSchedule.setEndTime(invalidEndTime);

        // The model doesn't have built-in validation, but we can test that it accepts the values
        assertEquals(invalidStartTime, invalidSchedule.getStartTime());
        assertEquals(invalidEndTime, invalidSchedule.getEndTime());
    }

    @Test
    void testCareGiverRelationship() {
        // Test bidirectional relationship
        CareGiver newCareGiver = new CareGiver();
        newCareGiver.setId(4L);
        newCareGiver.setName("Dr. Another");

        workingSchedule.setCareGiver(newCareGiver);
        assertEquals(newCareGiver, workingSchedule.getCareGiver());

        // Add the working schedule to the care giver
        newCareGiver.addWorkingSchedule(workingSchedule);
        assertEquals(1, newCareGiver.getWorkingSchedules().size());
        assertEquals(workingSchedule, newCareGiver.getWorkingSchedules().get(0));
    }
}