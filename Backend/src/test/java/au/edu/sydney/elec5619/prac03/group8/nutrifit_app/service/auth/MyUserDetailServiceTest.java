package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.auth;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.User;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyUserDetailServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MyUserDetailService myUserDetailService;

    @Test
    void loadUserByUsername_returnsUserPrincipal() {
        UUID uuid = UUID.randomUUID();
        User user = new User();
        user.setUuid(uuid);
        user.setPasswordHash("hash");
        user.setEmailVerified(true);

        when(userRepository.findByUuid(eq(uuid))).thenReturn(user);

        UserDetails details = myUserDetailService.loadUserByUsername(uuid.toString());

        assertThat(details.getUsername()).isEqualTo(uuid.toString());
        assertThat(details.getPassword()).isEqualTo("hash");
        assertThat(details.isEnabled()).isTrue();
    }

    @Test
    void loadUserByUsername_throwsWhenUserMissing() {
        UUID uuid = UUID.randomUUID();
        when(userRepository.findByUuid(eq(uuid))).thenReturn(null);

        assertThrows(UsernameNotFoundException.class,
            () -> myUserDetailService.loadUserByUsername(uuid.toString()));
    }
}
