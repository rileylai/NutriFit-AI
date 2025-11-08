package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.auth;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.PasswordResetResponseDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.PasswordResetToken;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.User;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.PasswordResetTokenRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.UserRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.email.EmailService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "tokenValidityHours", 24);
        ReflectionTestUtils.setField(passwordResetService, "frontendUrl", "https://frontend.app");
    }

    @Test
    void requestPasswordReset_returnsGenericSuccessWhenUserMissing() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        PasswordResetResponseDto response = passwordResetService.requestPasswordReset("missing@example.com");

        assertTrue(response.isSuccess());
        assertThat(response.getMessage()).contains("If an account exists");
        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(any(), any(), any());
    }

    @Test
    void requestPasswordReset_generatesTokenAndSendsEmail() {
        User user = buildUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(emailService.sendPasswordResetEmail(eq(user.getEmail()), eq(user.getUserName()), any()))
            .thenReturn(true);

        PasswordResetResponseDto response = passwordResetService.requestPasswordReset(user.getEmail());

        assertTrue(response.isSuccess());
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).deleteByUser(user);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());

        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getUser()).isSameAs(user);
        assertThat(savedToken.getToken()).isNotBlank();
        assertThat(savedToken.getExpiresAt()).isAfter(Instant.now());

        verify(emailService).sendPasswordResetEmail(eq(user.getEmail()), eq(user.getUserName()), any());
    }

    @Test
    void requestPasswordReset_returnsFailureWhenEmailSendThrows() {
        User user = buildUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("smtp down"))
            .when(emailService).sendPasswordResetEmail(any(), any(), any());

        PasswordResetResponseDto response = passwordResetService.requestPasswordReset(user.getEmail());

        assertFalse(response.isSuccess());
        assertThat(response.getMessage()).isEqualTo("Failed to send password reset email. Please try again later.");
    }

    @Test
    void resetPassword_returnsFailureForMissingToken() {
        when(passwordResetTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        PasswordResetResponseDto response = passwordResetService.resetPassword("missing", "newPass");

        assertFalse(response.isSuccess());
        assertThat(response.getMessage()).contains("Invalid or expired password reset token.");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void resetPassword_returnsFailureForExpiredToken() {
        PasswordResetToken token = PasswordResetToken.builder()
            .token("expired")
            .user(buildUser())
            .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
            .build();
        when(passwordResetTokenRepository.findByToken("expired")).thenReturn(Optional.of(token));

        PasswordResetResponseDto response = passwordResetService.resetPassword("expired", "newPass");

        assertFalse(response.isSuccess());
        assertThat(response.getMessage()).contains("has expired");
    }

    @Test
    void resetPassword_returnsFailureForAlreadyUsedToken() {
        PasswordResetToken token = PasswordResetToken.builder()
            .token("used")
            .user(buildUser())
            .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
            .usedAt(Instant.now())
            .build();
        when(passwordResetTokenRepository.findByToken("used")).thenReturn(Optional.of(token));

        PasswordResetResponseDto response = passwordResetService.resetPassword("used", "newPass");

        assertFalse(response.isSuccess());
        assertThat(response.getMessage()).contains("already been used");
    }

    @Test
    void resetPassword_updatesPasswordAndMarksTokenUsed() {
        User user = buildUser();
        PasswordResetToken token = PasswordResetToken.builder()
            .token("valid")
            .user(user)
            .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
            .build();

        when(passwordResetTokenRepository.findByToken("valid")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPass")).thenReturn("encoded");
        when(userRepository.save(user)).thenReturn(user);

        PasswordResetResponseDto response = passwordResetService.resetPassword("valid", "newPass");

        assertTrue(response.isSuccess());
        assertThat(user.getPasswordHash()).isEqualTo("encoded");
        verify(passwordResetTokenRepository).save(token);
        assertThat(token.isUsed()).isTrue();
    }

    @Test
    void cleanupExpiredTokens_invokesRepositoryWithCurrentInstant() {
        ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);

        passwordResetService.cleanupExpiredTokens();

        verify(passwordResetTokenRepository).deleteByExpiresAtBefore(instantCaptor.capture());
        assertThat(instantCaptor.getValue()).isNotNull();
        assertThat(Instant.now().minusSeconds(5)).isBefore(instantCaptor.getValue());
    }

    private User buildUser() {
        User user = new User();
        user.setUserId(7L);
        user.setUuid(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setUserName("user");
        return user;
    }
}
