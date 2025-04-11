package id.ac.ui.cs.advprog.authprofile.dto.response;

import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.model.WorkingSchedule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {
    private Long id;
    private String email;
    private String name;
    private String nik;
    private String address;
    private String phoneNumber;
    private List<String> roles;
    private String userType;

    // Pacillian specific field
    private String medicalHistory;

    // CareGiver specific fields
    private String speciality;
    private String workAddress;
    private Double averageRating;
    private List<WorkingScheduleDto> workingSchedules;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WorkingScheduleDto {
        private DayOfWeek dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private boolean isAvailable;
    }

    public static ProfileResponse fromUser(User user) {
        ProfileResponse response = new ProfileResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setNik(user.getNik());
        response.setAddress(user.getAddress());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()));

        if (user instanceof Pacillian) {
            response.setUserType("PACILLIAN");
            response.setMedicalHistory(((Pacillian) user).getMedicalHistory());
        } else if (user instanceof CareGiver) {
            response.setUserType("CAREGIVER");
            CareGiver careGiver = (CareGiver) user;
            response.setSpeciality(careGiver.getSpeciality());
            response.setWorkAddress(careGiver.getWorkAddress());
            response.setAverageRating(careGiver.getAverageRating());

            List<WorkingScheduleDto> schedules = careGiver.getWorkingSchedules().stream()
                    .map(schedule -> new WorkingScheduleDto(
                            schedule.getDayOfWeek(),
                            schedule.getStartTime(),
                            schedule.getEndTime(),
                            schedule.isAvailable()
                    ))
                    .collect(Collectors.toList());
            response.setWorkingSchedules(schedules);
        }

        return response;
    }
}
