package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.email;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(restTemplate);
        ReflectionTestUtils.setField(emailService, "mailgunApiKey", "test-api-key");
        ReflectionTestUtils.setField(emailService, "mailgunDomain", "mg.example.com");
        ReflectionTestUtils.setField(emailService, "fromEmail", "no-reply@example.com");
        ReflectionTestUtils.setField(emailService, "fromName", "NutriFit");
        ReflectionTestUtils.setField(emailService, "frontendBaseUrl", "https://front.app");
    }

    @Test
    void sendVerificationEmail_returnsTrueWhenMailgunSucceeds() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn(ResponseEntity.ok("ok"));

        boolean result = emailService.sendVerificationEmail("user@example.com", "User", "token123");

        assertThat(result).isTrue();

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(urlCaptor.capture(), entityCaptor.capture(), eq(String.class));

        assertThat(urlCaptor.getValue()).isEqualTo("https://api.mailgun.net/v3/mg.example.com/messages");

        HttpEntity<MultiValueMap<String, String>> entity = entityCaptor.getValue();
        assertThat(entity.getHeaders().getFirst("Authorization"))
            .isEqualTo("Basic " + Base64.getEncoder()
                .encodeToString("api:test-api-key".getBytes(StandardCharsets.UTF_8)));
        assertThat(entity.getBody().getFirst("to")).isEqualTo("user@example.com");
        assertThat(entity.getBody().getFirst("subject")).contains("Verify");
        assertThat(entity.getBody().getFirst("html")).contains("/verify-email?token=token123");
    }

    @Test
    void sendVerificationEmail_returnsFalseWhenMailgunReturnsError() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn(ResponseEntity.status(500).body("error"));

        boolean result = emailService.sendVerificationEmail("user@example.com", "User", "token123");

        assertThat(result).isFalse();
    }

    @Test
    void sendVerificationEmail_returnsFalseWhenExceptionThrown() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenThrow(new RuntimeException("network down"));

        boolean result = emailService.sendVerificationEmail("user@example.com", "User", "token123");

        assertThat(result).isFalse();
    }

    @Test
    void sendPasswordResetEmail_buildsResetLink() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn(ResponseEntity.ok("ok"));

        boolean result = emailService.sendPasswordResetEmail("user@example.com", "User", "resetToken");

        assertThat(result).isTrue();

        ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(anyString(), entityCaptor.capture(), eq(String.class));

        String html = entityCaptor.getValue().getBody().getFirst("html");
        assertThat(html).contains("/reset-password?token=resetToken");
    }
}
