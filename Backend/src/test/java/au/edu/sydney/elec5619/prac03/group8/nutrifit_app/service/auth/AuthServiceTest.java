package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.auth;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.LoginRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.RegisterRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.AuthResponseDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.EmailVerificationResponseDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.User;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.VerificationToken;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.UserRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.VerificationTokenRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.email.EmailService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JWTService jwtService;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private BCryptPasswordEncoder encoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "expiration", 3_600L);
    }

    @Test
    void register_returnsFailureWhenEmailAlreadyExists() {
        RegisterRequestDto request = new RegisterRequestDto("taken@example.com", "taken", "password123");
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        AuthResponseDto response = authService.register(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Email already exists");
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendVerificationEmail(any(), any(), any());
    }

    @Test
    void register_persistsUserAndTokenAndReturnsJwt() {
        RegisterRequestDto request = new RegisterRequestDto("test@example.com", "tester", "password123");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(encoder.encode("password123")).thenReturn("encoded");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0, User.class);
            saved.setUserId(42L);
            return saved;
        });
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        AuthResponseDto response = authService.register(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("encoded");
        assertThat(userCaptor.getValue().getUuid()).isNotNull();

        ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
        verify(verificationTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getUser()).isSameAs(userCaptor.getValue());
        assertThat(tokenCaptor.getValue().getToken()).isNotBlank();

        verify(emailService).sendVerificationEmail("test@example.com", "tester", tokenCaptor.getValue().getToken());
    }

    @Test
    void register_swallowsEmailFailureButStillSucceeds() {
        RegisterRequestDto request = new RegisterRequestDto("fail@example.com", "tester", "password123");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(encoder.encode(request.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");
        doThrow(new RuntimeException("mail down"))
            .when(emailService).sendVerificationEmail(any(), any(), any());

        AuthResponseDto response = authService.register(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getToken()).isEqualTo("jwt-token");
    }

    @Test
    void login_returnsTokenWhenAuthenticationSucceeds() {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("user@example.com");
        request.setPassword("secret");

        User user = new User();
        user.setUuid(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setUserName("user");
        user.setEmailVerified(true);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateToken(user.getUuid().toString())).thenReturn("jwt-token");

        AuthResponseDto response = authService.login(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUser().isEmailVerified()).isTrue();
    }

    @Test
    void login_returnsFailureWhenAuthenticationManagerRejectsCredentials() {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("user@example.com");
        request.setPassword("wrong");

        User user = new User();
        user.setUuid(UUID.randomUUID());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("bad"));

        AuthResponseDto response = authService.login(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Invalid email or password");
    }

    @Test
    void login_returnsFailureWhenAuthenticationNotAuthenticated() {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("user@example.com");
        request.setPassword("secret");

        User user = new User();
        user.setUuid(UUID.randomUUID());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        AuthResponseDto response = authService.login(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Authentication failed");
    }

    @Test
    void login_throwsWhenUserNotFound() {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("absent@example.com");

        when(userRepository.findByEmail("absent@example.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void verifyEmail_marksUserVerifiedWhenTokenValid() {
        User user = new User();
        user.setEmail("user@example.com");

        VerificationToken token = VerificationToken.builder()
            .token("token")
            .user(user)
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

        when(verificationTokenRepository.findByToken("token")).thenReturn(Optional.of(token));
        when(userRepository.save(user)).thenReturn(user);
        when(verificationTokenRepository.save(token)).thenReturn(token);

        EmailVerificationResponseDto response = authService.verifyEmail("token");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Email verified successfully");
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(token.isVerified()).isTrue();
    }

    @Test
    void verifyEmail_returnsAlreadyVerifiedWhenTokenUsed() {
        User user = new User();
        user.setEmail("user@example.com");

        VerificationToken token = VerificationToken.builder()
            .token("token")
            .user(user)
            .expiresAt(Instant.now().plusSeconds(3600))
            .verifiedAt(Instant.now())
            .build();

        when(verificationTokenRepository.findByToken("token")).thenReturn(Optional.of(token));

        EmailVerificationResponseDto response = authService.verifyEmail("token");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Email already verified");
    }

    @Test
    void verifyEmail_returnsExpiredMessageWhenTokenExpired() {
        User user = new User();
        VerificationToken token = VerificationToken.builder()
            .token("token")
            .user(user)
            .expiresAt(Instant.now().minusSeconds(10))
            .build();

        when(verificationTokenRepository.findByToken("token")).thenReturn(Optional.of(token));

        EmailVerificationResponseDto response = authService.verifyEmail("token");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("Verification token has expired");
    }

    @Test
    void verifyEmail_returnsInvalidWhenTokenMissing() {
        when(verificationTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        EmailVerificationResponseDto response = authService.verifyEmail("missing");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Invalid verification token");
    }

    @Test
    void verifyEmail_returnsFailureWhenPersistenceFails() {
        User user = new User();
        VerificationToken token = VerificationToken.builder()
            .token("token")
            .user(user)
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

        when(verificationTokenRepository.findByToken("token")).thenReturn(Optional.of(token));
        when(userRepository.save(user)).thenThrow(new RuntimeException("db down"));

        EmailVerificationResponseDto response = authService.verifyEmail("token");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).startsWith("Verification failed");
    }

    @Test
    void resendVerificationEmail_returnsFailureWhenUserMissing() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        EmailVerificationResponseDto response = authService.resendVerificationEmail("missing@example.com");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("User not found");
    }

    @Test
    void resendVerificationEmail_returnsAlreadyVerifiedWhenFlagTrue() {
        User user = new User();
        user.setEmailVerified(true);
        when(userRepository.findByEmail("verified@example.com")).thenReturn(Optional.of(user));

        EmailVerificationResponseDto response = authService.resendVerificationEmail("verified@example.com");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Email already verified");
    }

    @Test
    void resendVerificationEmail_deletesOldTokenAndSendsNewEmail() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setUserName("user");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        VerificationToken existing = VerificationToken.builder()
            .token("old")
            .user(user)
            .expiresAt(Instant.now().plusSeconds(10))
            .build();
        when(verificationTokenRepository.findByUserAndVerifiedAtIsNull(user)).thenReturn(Optional.of(existing));
        when(emailService.sendVerificationEmail(eq("user@example.com"), eq("user"), any())).thenReturn(true);

        EmailVerificationResponseDto response = authService.resendVerificationEmail("user@example.com");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Verification email sent successfully");

        verify(verificationTokenRepository).delete(existing);
        verify(verificationTokenRepository).save(any(VerificationToken.class));
        verify(emailService).sendVerificationEmail(eq("user@example.com"), eq("user"), any());
    }

    @Test
    void resendVerificationEmail_returnsFailureWhenEmailFails() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setUserName("user");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(emailService.sendVerificationEmail(any(), any(), any())).thenReturn(false);

        EmailVerificationResponseDto response = authService.resendVerificationEmail("user@example.com");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Failed to send verification email. Please try again later.");
    }

    @Test
    void resendVerificationEmail_returnsFailureWhenUnexpectedError() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setUserName("user");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("smtp down"))
            .when(emailService).sendVerificationEmail(any(), any(), any());

        EmailVerificationResponseDto response = authService.resendVerificationEmail("user@example.com");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).startsWith("Failed to resend verification email");
    }
}
