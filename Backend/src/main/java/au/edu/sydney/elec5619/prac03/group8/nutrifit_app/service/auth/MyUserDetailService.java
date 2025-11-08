package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.auth;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.User;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.user.UserPrincipal;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MyUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String uuid) throws UsernameNotFoundException {
        User user = userRepository.findByUuid(UUID.fromString(uuid));
        if (user == null) {
            throw new UsernameNotFoundException(uuid);
        }
        return new UserPrincipal(user);
    }
}
