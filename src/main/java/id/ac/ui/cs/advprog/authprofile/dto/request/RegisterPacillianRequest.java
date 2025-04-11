package id.ac.ui.cs.advprog.authprofile.dto.request;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class RegisterPacillianRequest extends BaseRegisterRequest {
    private String medicalHistory;
}