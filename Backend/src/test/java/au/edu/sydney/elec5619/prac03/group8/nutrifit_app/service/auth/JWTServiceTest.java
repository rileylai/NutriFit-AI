package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.auth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JWTServiceTest {

    private static final String RAW_SECRET = "0123456789ABCDEF0123456789ABCDEF";

    private JWTService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JWTService();
        String encodedSecret = Base64.getEncoder()
            .encodeToString(RAW_SECRET.getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(jwtService, "secret", encodedSecret);
        ReflectionTestUtils.setField(jwtService, "expiration", 60_000L);
    }

    @Test
    void generateTokenAndValidate_returnsTrueForMatchingUser() {
        String uuid = UUID.randomUUID().toString();
        String token = jwtService.generateToken(uuid);

        UserDetails userDetails = User.withUsername(uuid)
            .password("N/A")
            .authorities("USER")
            .build();

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUuid(token)).isEqualTo(uuid);
        assertThat(jwtService.validateToken(token, userDetails)).isTrue();
    }

    @Test
    void validateToken_returnsFalseWhenUsernameDiffers() {
        String token = jwtService.generateToken(UUID.randomUUID().toString());

        UserDetails otherUser = User.withUsername(UUID.randomUUID().toString())
            .password("N/A")
            .authorities("USER")
            .build();

        assertThat(jwtService.validateToken(token, otherUser)).isFalse();
    }

    @Test
    void validateToken_returnsFalseWhenExpired() {
        ReflectionTestUtils.setField(jwtService, "expiration", -1_000L);
        String uuid = UUID.randomUUID().toString();
        String token = jwtService.generateToken(uuid);

        UserDetails userDetails = User.withUsername(uuid)
            .password("N/A")
            .authorities("USER")
            .build();

        assertThat(jwtService.validateToken(token, userDetails)).isFalse();
    }

    @Test
    void validateToken_returnsFalseForMalformedToken() {
        UserDetails userDetails = User.withUsername("user")
            .password("N/A")
            .authorities("USER")
            .build();

        assertThat(jwtService.validateToken("not-a-token", userDetails)).isFalse();
    }

    @Test
    void extractUuid_throwsForMalformedToken() {
        assertThrows(Exception.class, () -> jwtService.extractUuid("bad-token"));
    }
}
