package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.Profile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMetricsRequestDto {
    @NotNull(message = "Height is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Height must be greater than zero")
    private BigDecimal heightCm;
    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than zero")
    private BigDecimal weightKg;
    private Integer age;
    private String gender;
    private String userGoal;
    private LocalDateTime recordAt;
}
