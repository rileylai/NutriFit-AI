package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final RestTemplate restTemplate;

    @Value("${mailgun.api-key}")
    private String mailgunApiKey;

    @Value("${mailgun.domain}")
    private String mailgunDomain;

    @Value("${mailgun.from-email}")
    private String fromEmail;

    @Value("${mailgun.from-name}")
    private String fromName;

    @Value("${app.frontend.url}")
    private String frontendBaseUrl;

    /**
     * Send verification email using Mailgun API
     */
    public boolean sendVerificationEmail(String toEmail, String userName, String verificationToken) {
        try {
            String verificationUrl = generateVerificationUrl(verificationToken);
            String subject = "Verify Your NutriFit Account";
            String htmlContent = buildVerificationEmailHtml(userName, verificationUrl);
            String textContent = buildVerificationEmailText(userName, verificationUrl);

            return sendEmail(toEmail, subject, textContent, htmlContent);

        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send password reset email
     */
    public boolean sendPasswordResetEmail(String toEmail, String userName, String resetToken) {
        try {
            String resetUrl = generatePasswordResetUrl(resetToken);
            String subject = "Reset Your NutriFit Password";
            String htmlContent = buildPasswordResetEmailHtml(userName, resetUrl);
            String textContent = buildPasswordResetEmailText(userName, resetUrl);

            return sendEmail(toEmail, subject, textContent, htmlContent);

        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Generic method to send email via Mailgun API using RestTemplate
     */
    private boolean sendEmail(String toEmail, String subject, String textContent, String htmlContent) {
        try {
            String url = "https://api.mailgun.net/v3/" + mailgunDomain + "/messages";

            // Create request headers with Basic Auth
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Basic Auth: "api:YOUR_API_KEY"
            String auth = "api:" + mailgunApiKey;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            headers.set("Authorization", "Basic " + encodedAuth);

            // Create form data
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("from", fromName + " <" + fromEmail + ">");
            formData.add("to", toEmail);
            formData.add("subject", subject);
            formData.add("text", textContent);
            formData.add("html", htmlContent);

            // Create HTTP entity
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

            // Send request
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Email sent successfully to {}", toEmail);
                return true;
            } else {
                log.error("Failed to send email. Status code: {}, Response: {}",
                    response.getStatusCode(), response.getBody());
                return false;
            }

        } catch (Exception e) {
            log.error("Error sending email via Mailgun: {}", e.getMessage(), e);
            return false;
        }
    }

    private String generateVerificationUrl(String token) {
        return UriComponentsBuilder.fromUriString(frontendBaseUrl)
            .path("/verify-email")
            .queryParam("token", token)
            .toUriString();
    }

    private String generatePasswordResetUrl(String token) {
        return UriComponentsBuilder.fromUriString(frontendBaseUrl)
            .path("/reset-password")
            .queryParam("token", token)
            .toUriString();
    }

    private String buildVerificationEmailHtml(String userName, String verificationUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Verify Your Email</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background-color: #f8f9fa; border-radius: 10px; padding: 30px; margin-bottom: 20px;">
                    <h1 style="color: #28a745; margin-bottom: 20px;">Welcome to NutriFit!</h1>
                    <p>Hi %s,</p>
                    <p>Thank you for registering with NutriFit. Please verify your email address to activate your account.</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s"
                           style="display: inline-block; background-color: #28a745; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: bold;">
                            Verify Email Address
                        </a>
                    </div>
                    <p>Or copy and paste this link into your browser:</p>
                    <p style="word-break: break-all; color: #666; font-size: 14px;">%s</p>
                    <p style="color: #999; font-size: 12px; margin-top: 30px;">
                        This verification link will expire in 24 hours. If you didn't create an account with NutriFit, please ignore this email.
                    </p>
                </div>
                <div style="text-align: center; color: #999; font-size: 12px;">
                    <p>&copy; 2025 NutriFit. All rights reserved.</p>
                </div>
            </body>
            </html>
            """.formatted(userName, verificationUrl, verificationUrl);
    }

    private String buildVerificationEmailText(String userName, String verificationUrl) {
        return String.format("""
            Welcome to NutriFit!

            Hi %s,

            Thank you for registering with NutriFit. Please verify your email address to activate your account.

            Click the link below to verify your email:
            %s

            This verification link will expire in 24 hours.

            If you didn't create an account with NutriFit, please ignore this email.

            Best regards,
            The NutriFit Team
            """, userName, verificationUrl);
    }

    private String buildPasswordResetEmailHtml(String userName, String resetUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Reset Your Password</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background-color: #f8f9fa; border-radius: 10px; padding: 30px; margin-bottom: 20px;">
                    <h1 style="color: #dc3545; margin-bottom: 20px;">Reset Your Password</h1>
                    <p>Hi %s,</p>
                    <p>We received a request to reset your password for your NutriFit account.</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s"
                           style="display: inline-block; background-color: #dc3545; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: bold;">
                            Reset Password
                        </a>
                    </div>
                    <p>Or copy and paste this link into your browser:</p>
                    <p style="word-break: break-all; color: #666; font-size: 14px;">%s</p>
                    <p style="color: #999; font-size: 12px; margin-top: 30px;">
                        This password reset link will expire in 1 hour. If you didn't request a password reset, please ignore this email.
                    </p>
                </div>
                <div style="text-align: center; color: #999; font-size: 12px;">
                    <p>&copy; 2025 NutriFit. All rights reserved.</p>
                </div>
            </body>
            </html>
            """.formatted(userName, resetUrl, resetUrl);
    }

    private String buildPasswordResetEmailText(String userName, String resetUrl) {
        return String.format("""
            Reset Your Password

            Hi %s,

            We received a request to reset your password for your NutriFit account.

            Click the link below to reset your password:
            %s

            This password reset link will expire in 1 hour.

            If you didn't request a password reset, please ignore this email.

            Best regards,
            The NutriFit Team
            """, userName, resetUrl);
    }
}
