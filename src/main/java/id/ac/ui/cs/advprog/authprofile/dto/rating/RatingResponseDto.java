package id.ac.ui.cs.advprog.authprofile.dto.rating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingResponseDto {
    private Long id;
    private Long consultationId;
    private Long doctorId;
    private Integer score;
    private String comment;
    private LocalDateTime createdAt;
}