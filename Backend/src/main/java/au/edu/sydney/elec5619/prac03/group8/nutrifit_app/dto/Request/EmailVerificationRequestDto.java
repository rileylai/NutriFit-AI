package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationRequestDto {

    @NotBlank(message = "Verification token is required")
    private String token;
}
