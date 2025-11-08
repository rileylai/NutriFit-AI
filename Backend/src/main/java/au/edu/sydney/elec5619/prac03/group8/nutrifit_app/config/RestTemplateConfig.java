package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(10))  // Connection timeout
                .readTimeout(Duration.ofSeconds(60))     // Read timeout for long operations (image upload + AI)
                .build();
    }
}