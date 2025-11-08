package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutRequestDto {
    private Long userId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workoutDate;

    private String workoutType;

    private Integer durationMinutes;

    private BigDecimal caloriesBurned;

    private String notes;
}
