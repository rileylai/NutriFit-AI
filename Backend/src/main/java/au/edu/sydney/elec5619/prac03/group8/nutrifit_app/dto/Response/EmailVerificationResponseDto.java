package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationResponseDto {

    private boolean success;

    private String message;

    private Boolean emailVerified;
}
