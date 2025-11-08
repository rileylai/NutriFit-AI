package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Profile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMetricsResponseDto {
    private Long metricId;
    private Long userId;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private Integer age;
    private String gender;
    private BigDecimal bmi;
    private BigDecimal bmr;
    private String userGoal;
    private LocalDateTime recordAt;
    private LocalDateTime createdAt;
}
