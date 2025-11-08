package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Common;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutDto {
    private Long workoutId;
    private Long userId;
    private String workoutType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workoutDate;

    private Integer durationMinutes;
    private BigDecimal caloriesBurned;
    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
