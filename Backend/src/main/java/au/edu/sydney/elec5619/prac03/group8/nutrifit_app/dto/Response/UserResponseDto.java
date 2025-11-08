package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private UUID uuid;
    private String email;
    private String userName;
    private boolean emailVerified;
}
