package id.ac.ui.cs.advprog.authprofile.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "caregivers")
@PrimaryKeyJoinColumn(name = "user_id")
public class CareGiver extends User {

    @Column(name = "speciality", nullable = false)
    private String speciality;

    @Column(name = "work_address", nullable = false)
    private String workAddress;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @OneToMany(mappedBy = "careGiver", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<WorkingSchedule> workingSchedules = new ArrayList<>();

    public CareGiver(String email, String password, String name, String nik,
                     String address, String phoneNumber, String speciality, String workAddress) {
        super(email, password, name, nik, address, phoneNumber);
        this.speciality = speciality;
        this.workAddress = workAddress;
    }

    // Helper method to add a working schedule
    public void addWorkingSchedule(WorkingSchedule schedule) {
        workingSchedules.add(schedule);
        schedule.setCareGiver(this);
    }

    // Helper method to remove a working schedule
    public void removeWorkingSchedule(WorkingSchedule schedule) {
        workingSchedules.remove(schedule);
        schedule.setCareGiver(null);
    }

    // Helper method to update average rating
    public void updateRating(Double newRating) {
        double totalRating = (averageRating * ratingCount) + newRating;
        ratingCount++;
        averageRating = totalRating / ratingCount;
    }
}