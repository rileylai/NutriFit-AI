package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.auth;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.LoginRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.RegisterRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.AuthResponseDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.EmailVerificationResponseDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.UserResponseDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.User;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.VerificationToken;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.UserRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.VerificationTokenRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final JWTService jwtService;
    private final AuthenticationManager authManager;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final BCryptPasswordEncoder encoder;
    private final EmailService emailService;

    @Value("${jwt.expiration}")
    private long expiration;

    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        log.debug("Starting registration process for email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already exists: {}", request.getEmail());
            return AuthResponseDto.builder()
                    .success(false)
                    .message("Email already exists")
                    .build();
        }

        User user = new User();
        user.setUuid(UUID.randomUUID());
        user.setEmail(request.getEmail());
        user.setUserName(request.getUserName());
        user.setPasswordHash(encoder.encode(request.getPassword()));
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);
        log.info("User created successfully with UUID: {}, email: {}", savedUser.getUuid(), savedUser.getEmail());

        // Generate verification token in separate table
        String tokenString = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(tokenString)
                .user(savedUser)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();
        verificationTokenRepository.save(verificationToken);

        // Send verification email
        try {
            emailService.sendVerificationEmail(
                savedUser.getEmail(),
                savedUser.getUserName(),
                tokenString
            );
            log.info("Verification email sent successfully to: {}", savedUser.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", savedUser.getEmail(), e.getMessage());
            // Continue with registration even if email fails
        }

        String token = jwtService.generateToken(savedUser.getUuid().toString());
        log.debug("JWT token generated for user: {}", savedUser.getEmail());

        UserResponseDto userResponse = UserResponseDto.builder()
                .uuid(savedUser.getUuid())
                .email(savedUser.getEmail())
                .userName(savedUser.getUserName())
                .emailVerified(savedUser.isEmailVerified())
                .build();

        return AuthResponseDto.builder()
                .success(true)
                .message("Registration successful")
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiration)
                .user(userResponse)
                .build();
    }

    public AuthResponseDto login(LoginRequestDto request) {
        log.debug("Login attempt for email: {}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found: {}", request.getEmail());
                    return new BadCredentialsException("Invalid email or password");
                });

        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUuid().toString(), request.getPassword())
            );

            if (!authentication.isAuthenticated()) {
                log.warn("Authentication failed for user: {}", request.getEmail());
                return AuthResponseDto.builder()
                        .success(false)
                        .message("Authentication failed")
                        .build();
            }

            String token = jwtService.generateToken(user.getUuid().toString());
            log.info("Login successful for user: {}, emailVerified: {}", request.getEmail(), user.isEmailVerified());

            UserResponseDto userResponse = UserResponseDto.builder()
                    .uuid(user.getUuid())
                    .email(user.getEmail())
                    .userName(user.getUserName())
                    .emailVerified(user.isEmailVerified())
                    .build();

            return AuthResponseDto.builder()
                    .success(true)
                    .message("Login successful")
                    .token(token)
                    .tokenType("Bearer")
                    .expiresIn(expiration)
                    .user(userResponse)
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for user: {}", request.getEmail());
            return AuthResponseDto.builder()
                    .success(false)
                    .message("Invalid email or password")
                    .build();
        }
    }

    /**
     * Verify email with token only (no email required)
     */
    @Transactional
    public EmailVerificationResponseDto verifyEmail(String token) {
        log.debug("Email verification attempt with token");
        try {
            VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                    .orElseThrow(() -> {
                        log.warn("Email verification failed - token not found");
                        return new IllegalArgumentException("Invalid verification token");
                    });

            User user = verificationToken.getUser();

            // Check if already verified
            if (verificationToken.isVerified()) {
                log.info("Email already verified for: {}", user.getEmail());
                return EmailVerificationResponseDto.builder()
                        .success(false)
                        .message("Email already verified")
                        .emailVerified(true)
                        .build();
            }

            // Check if token expired
            if (verificationToken.isExpired()) {
                log.warn("Verification token expired for user: {}", user.getEmail());
                return EmailVerificationResponseDto.builder()
                        .success(false)
                        .message("Verification token has expired. Please request a new one.")
                        .emailVerified(false)
                        .build();
            }

            // Verify email
            user.setEmailVerified(true);
            userRepository.save(user);

            verificationToken.setVerifiedAt(Instant.now());
            verificationTokenRepository.save(verificationToken);

            log.info("Email verified successfully for user: {}", user.getEmail());

            return EmailVerificationResponseDto.builder()
                    .success(true)
                    .message("Email verified successfully")
                    .emailVerified(true)
                    .build();

        } catch (IllegalArgumentException e) {
            return EmailVerificationResponseDto.builder()
                    .success(false)
                    .message(e.getMessage())
                    .emailVerified(false)
                    .build();
        } catch (Exception e) {
            log.error("Error verifying email: {}", e.getMessage(), e);
            return EmailVerificationResponseDto.builder()
                    .success(false)
                    .message("Verification failed: " + e.getMessage())
                    .emailVerified(false)
                    .build();
        }
    }

    /**
     * Resend verification email
     */
    @Transactional
    public EmailVerificationResponseDto resendVerificationEmail(String email) {
        log.debug("Resend verification email request for: {}", email);
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("Resend verification failed - user not found: {}", email);
                        return new IllegalArgumentException("User not found");
                    });

            // Check if already verified
            if (user.isEmailVerified()) {
                log.info("Resend verification skipped - email already verified: {}", email);
                return EmailVerificationResponseDto.builder()
                        .success(false)
                        .message("Email already verified")
                        .emailVerified(true)
                        .build();
            }

            // Delete any existing unverified tokens for this user
            verificationTokenRepository.findByUserAndVerifiedAtIsNull(user)
                    .ifPresent(verificationTokenRepository::delete);

            // Generate new verification token
            String tokenString = UUID.randomUUID().toString();
            VerificationToken verificationToken = VerificationToken.builder()
                    .token(tokenString)
                    .user(user)
                    .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                    .build();
            verificationTokenRepository.save(verificationToken);
            log.debug("New verification token generated for user: {}", email);

            // Send verification email
            boolean emailSent = emailService.sendVerificationEmail(
                    user.getEmail(),
                    user.getUserName(),
                    tokenString
            );

            if (!emailSent) {
                log.error("Failed to send verification email to: {}", email);
                return EmailVerificationResponseDto.builder()
                        .success(false)
                        .message("Failed to send verification email. Please try again later.")
                        .emailVerified(false)
                        .build();
            }

            log.info("Verification email resent to: {}", email);

            return EmailVerificationResponseDto.builder()
                    .success(true)
                    .message("Verification email sent successfully")
                    .emailVerified(false)
                    .build();

        } catch (IllegalArgumentException e) {
            return EmailVerificationResponseDto.builder()
                    .success(false)
                    .message(e.getMessage())
                    .emailVerified(false)
                    .build();
        } catch (Exception e) {
            log.error("Error resending verification email: {}", e.getMessage(), e);
            return EmailVerificationResponseDto.builder()
                    .success(false)
                    .message("Failed to resend verification email: " + e.getMessage())
                    .emailVerified(false)
                    .build();
        }
    }
}
