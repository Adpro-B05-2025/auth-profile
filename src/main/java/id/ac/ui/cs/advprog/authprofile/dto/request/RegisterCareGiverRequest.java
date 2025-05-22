package id.ac.ui.cs.advprog.authprofile.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class RegisterCareGiverRequest extends BaseRegisterRequest {

    @NotBlank(message = "Speciality is required")
    private String speciality;

    @NotBlank(message = "Work address is required")
    private String workAddress;
}