package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutResponseDto {
    private boolean success;
    private String message;
}
