package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.Gender;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.User;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.UserProfile;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.UserProfileRepository;
import java.time.LocalDate;
import java.util.Optional;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.profile.UserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserUserMetricServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(88L);
    }

    @Test
    void upsertProfile_createsNewProfileWhenAbsent() {
        when(userProfileRepository.findByUserUserId(88L)).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfile profile = userProfileService.upsertProfile(user, LocalDate.of(1990, 1, 1), Gender.MALE);

        assertThat(profile.getUser()).isSameAs(user);
        assertThat(profile.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(profile.getGender()).isEqualTo(Gender.MALE);

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(user);
    }

    @Test
    void upsertProfile_updatesExistingProfile() {
        UserProfile existing = new UserProfile();
        existing.setUser(user);
        when(userProfileRepository.findByUserUserId(88L)).thenReturn(Optional.of(existing));
        when(userProfileRepository.save(existing)).thenReturn(existing);

        UserProfile profile = userProfileService.upsertProfile(user, LocalDate.of(1985, 5, 5), Gender.FEMALE);

        assertThat(profile).isSameAs(existing);
        assertThat(profile.getBirthDate()).isEqualTo(LocalDate.of(1985, 5, 5));
        assertThat(profile.getGender()).isEqualTo(Gender.FEMALE);
    }

    @Test
    void getProfile_returnsProfileOrNull() {
        UserProfile profile = new UserProfile();
        when(userProfileRepository.findByUserUserId(88L)).thenReturn(Optional.of(profile));

        assertThat(userProfileService.getProfile(88L)).isSameAs(profile);

        when(userProfileRepository.findByUserUserId(99L)).thenReturn(Optional.empty());
        assertThat(userProfileService.getProfile(99L)).isNull();
    }
}
