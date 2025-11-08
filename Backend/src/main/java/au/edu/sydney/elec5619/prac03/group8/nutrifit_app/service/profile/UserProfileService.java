package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.profile;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.Gender;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.User;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.UserProfile;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.UserProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Service to manage user profile creation and updates.
 */
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    @Transactional
    public UserProfile upsertProfile(User user, LocalDate birthDate, Gender gender) {
        UserProfile profile = userProfileRepository
                .findByUserUserId(user.getUserId())
                .orElseGet(() -> {
                    UserProfile p = new UserProfile();
                    p.setUser(user);
                    return p;
                });
        profile.setBirthDate(birthDate);
        profile.setGender(gender);
        return userProfileRepository.save(profile);
    }

    public UserProfile getProfile(Long userId) {
        return userProfileRepository.findByUserUserId(userId).orElse(null);
    }
}

