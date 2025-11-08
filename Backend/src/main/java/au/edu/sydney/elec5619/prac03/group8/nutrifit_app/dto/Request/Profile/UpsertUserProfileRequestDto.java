package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.Profile;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.Gender;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request body for creating or updating the current user's profile.
 */
@Data
public class UpsertUserProfileRequestDto {

    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    private Gender gender;
}

