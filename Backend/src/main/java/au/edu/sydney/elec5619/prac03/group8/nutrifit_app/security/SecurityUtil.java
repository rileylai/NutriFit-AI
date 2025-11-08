package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.security;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.User;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecurityUtil {
    private final UserRepository userRepository;

    public Optional<UserDetails> getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(authentication)) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return Optional.of(userDetails);
        }

        return Optional.empty();
    }

    public Optional<String> getCurrentUsername() {
        return getCurrentUserDetails().map(UserDetails::getUsername)
                .or(() -> {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (!isAuthenticated(authentication)) {
                        return Optional.empty();
                    }
                    return Optional.ofNullable(authentication.getName());
                });
    }

    public Optional<UUID> getCurrentUserUuid() {
        return getCurrentUsername().map(username -> {
            try {
                return UUID.fromString(username);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Invalid authenticated user identifier", e);
            }
        });
    }

    public Optional<User> getCurrentUser() {
        return getCurrentUserUuid().map(uuid -> {
            User user = userRepository.findByUuid(uuid);
            if (user == null) {
                throw new IllegalStateException("Authenticated user not found");
            }
            return user;
        });
    }

    public User getCurrentUserOrThrow() {
        return getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("No authenticated user found"));
    }

    public boolean isAuthenticated() {
        return isAuthenticated(SecurityContextHolder.getContext().getAuthentication());
    }

    private boolean isAuthenticated(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        return principal != null && !"anonymousUser".equals(String.valueOf(principal));
    }
}
