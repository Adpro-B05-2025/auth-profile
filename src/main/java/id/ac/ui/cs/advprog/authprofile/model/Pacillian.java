package id.ac.ui.cs.advprog.authprofile.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pacillians")
@PrimaryKeyJoinColumn(name = "user_id")
public class Pacillian extends User {

    @Column(columnDefinition = "TEXT")
    private String medicalHistory;

    public Pacillian(String email, String password, String name, String nik,
                     String address, String phoneNumber, String medicalHistory) {
        super(email, password, name, nik, address, phoneNumber);
        this.medicalHistory = medicalHistory;
    }
}