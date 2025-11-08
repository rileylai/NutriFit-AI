package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    private boolean success;
    private String message;
    private String token;
    private String tokenType;
    private Long expiresIn;
    private UserResponseDto user;
}
