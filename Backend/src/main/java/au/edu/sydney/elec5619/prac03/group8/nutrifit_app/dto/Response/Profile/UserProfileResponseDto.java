package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Profile;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.UserProfile;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Response DTO for user profile resource.
 */
@Data
@Builder
public class UserProfileResponseDto {
    private Long userId;
    private LocalDate birthDate;
    private String gender;

    public static UserProfileResponseDto fromEntity(UserProfile entity) {
        return UserProfileResponseDto.builder()
                .userId(entity.getUser().getUserId())
                .birthDate(entity.getBirthDate())
                .gender(entity.getGender() != null ? entity.getGender().name() : null)
                .build();
    }
}

