package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.controller;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.EmailVerificationRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.ForgotPasswordRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.LoginRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.RegisterRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.ResendVerificationRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.ResetPasswordRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.AuthResponseDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.EmailVerificationResponseDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.PasswordResetResponseDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.auth.AuthService;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.auth.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        log.info("Registration request received for email: {}", request.getEmail());
        try {
            AuthResponseDto response = authService.register(request);
            if (response.isSuccess()) {
                log.info("User registered successfully: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                log.warn("Registration failed for email {}: {}", request.getEmail(), response.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (IllegalArgumentException e) {
            log.error("Registration validation error for email {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponseDto.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Unexpected error during registration for email {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponseDto.builder()
                            .success(false)
                            .message("Registration failed: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        log.info("Login request received for email: {}", request.getEmail());
        try {
            AuthResponseDto response = authService.login(request);
            if (response.isSuccess()) {
                log.info("User logged in successfully: {}", request.getEmail());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Login failed for email {}: {}", request.getEmail(), response.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            log.error("Unexpected error during login for email {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponseDto.builder()
                            .success(false)
                            .message("Login failed: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/email-verify")
    public ResponseEntity<EmailVerificationResponseDto> verifyEmail(
            @Valid @RequestBody EmailVerificationRequestDto request) {
        log.info("Email verification request received with token");
        try {
            EmailVerificationResponseDto response = authService.verifyEmail(request.getToken());

            if (response.isSuccess()) {
                log.info("Email verified successfully");
                return ResponseEntity.ok(response);
            } else {
                log.warn("Email verification failed: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("Unexpected error during email verification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EmailVerificationResponseDto.builder()
                            .success(false)
                            .message("Email verification failed: " + e.getMessage())
                            .emailVerified(false)
                            .build());
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<EmailVerificationResponseDto> resendVerification(
            @Valid @RequestBody ResendVerificationRequestDto request) {
        log.info("Resend verification email request received for: {}", request.getEmail());
        try {
            EmailVerificationResponseDto response = authService.resendVerificationEmail(request.getEmail());

            if (response.isSuccess()) {
                log.info("Verification email resent successfully to: {}", request.getEmail());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Failed to resend verification email to {}: {}", request.getEmail(), response.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("Unexpected error while resending verification email to {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EmailVerificationResponseDto.builder()
                            .success(false)
                            .message("Failed to resend verification email: " + e.getMessage())
                            .emailVerified(false)
                            .build());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetResponseDto> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto request) {
        log.info("Forgot password request received for email: {}", request.getEmail());
        try {
            PasswordResetResponseDto response = passwordResetService.requestPasswordReset(request.getEmail());

            if (response.isSuccess()) {
                log.info("Password reset email sent successfully for: {}", request.getEmail());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Failed to send password reset email for {}: {}", request.getEmail(), response.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("Unexpected error during forgot password for {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PasswordResetResponseDto.builder()
                            .success(false)
                            .message("Failed to process password reset request: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetResponseDto> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto request) {
        log.info("Reset password request received");
        try {
            PasswordResetResponseDto response = passwordResetService.resetPassword(
                    request.getToken(),
                    request.getNewPassword()
            );

            if (response.isSuccess()) {
                log.info("Password reset successfully");
                return ResponseEntity.ok(response);
            } else {
                log.warn("Password reset failed: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("Unexpected error during password reset: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PasswordResetResponseDto.builder()
                            .success(false)
                            .message("Failed to reset password: " + e.getMessage())
                            .build());
        }
    }
}