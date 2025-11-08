package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.auth;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.PasswordResetResponseDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.PasswordResetToken;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.User;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.PasswordResetTokenRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.UserRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.password-reset.token-validity-hours:24}")
    private int tokenValidityHours;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Transactional
    public PasswordResetResponseDto requestPasswordReset(String email) {
        log.info("Password reset requested for email: {}", email);

        Optional<User> userOptional = userRepository.findByEmail(email);

        // For security reasons, always return a success message even if the user does not exist, to avoid leaking user information
        if (userOptional.isEmpty()) {
            log.warn("Password reset requested for non-existent email: {}", email);
            return PasswordResetResponseDto.builder()
                    .success(true)
                    .message("If an account exists with this email, a password reset link has been sent.")
                    .build();
        }

        User user = userOptional.get();

        // Delete all previous password reset tokens for this user
        passwordResetTokenRepository.deleteByUser(user);

        // Generate a new reset token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(Instant.now().plus(tokenValidityHours, ChronoUnit.HOURS))
                .build();

        passwordResetTokenRepository.save(resetToken);

        // Send password reset email
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), user.getUserName(), resetLink);
            log.info("Password reset email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", email, e.getMessage(), e);
            return PasswordResetResponseDto.builder()
                    .success(false)
                    .message("Failed to send password reset email. Please try again later.")
                    .build();
        }

        return PasswordResetResponseDto.builder()
                .success(true)
                .message("If an account exists with this email, a password reset link has been sent.")
                .build();
    }

    @Transactional
    public PasswordResetResponseDto resetPassword(String token, String newPassword) {
        log.info("Password reset attempt with token");

        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(token);

        if (tokenOptional.isEmpty()) {
            log.warn("Invalid password reset token provided");
            return PasswordResetResponseDto.builder()
                    .success(false)
                    .message("Invalid or expired password reset token.")
                    .build();
        }

        PasswordResetToken resetToken = tokenOptional.get();

        // Check if the token is expired
        if (resetToken.isExpired()) {
            log.warn("Expired password reset token used");
            return PasswordResetResponseDto.builder()
                    .success(false)
                    .message("Password reset token has expired. Please request a new one.")
                    .build();
        }

        // Check if the token has already been used
        if (resetToken.isUsed()) {
            log.warn("Already used password reset token");
            return PasswordResetResponseDto.builder()
                    .success(false)
                    .message("This password reset token has already been used.")
                    .build();
        }

        // Update the user's password
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark the token as used
        resetToken.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset successfully for user: {}", user.getEmail());

        return PasswordResetResponseDto.builder()
                .success(true)
                .message("Password has been reset successfully. You can now login with your new password.")
                .build();
    }

    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Cleaning up expired password reset tokens");
        passwordResetTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }
}
