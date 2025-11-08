package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Client for uploading images to picui.cn image hosting service.
 * Handles multipart file upload and returns the uploaded image URL.
 */
@Component
public class ImageHostingClient {

    private static final Logger logger = LoggerFactory.getLogger(ImageHostingClient.class);

    @Value("${picui.api.url:https://www.picui.cn/api/v1/upload}")
    private String apiUrl;

    @Value("${picui.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public ImageHostingClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Uploads an image file to picui.cn and returns the thumbnail URL.
     *
     * @param file the image file to upload
     * @return the thumbnail URL of the uploaded image
     * @throws IllegalStateException if upload fails
     */
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file cannot be null or empty");
        }

        logger.info("Uploading image to picui.cn: filename={}, size={} bytes",
                file.getOriginalFilename(), file.getSize());
        logger.debug("Using API URL: {}", apiUrl);
        logger.debug("API key configured: {}", apiKey != null && !apiKey.isEmpty() ? "Yes (length: " + apiKey.length() + ")" : "No");

        try {
            // Prepare multipart request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // Add the image file
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            // Add the API token as a form parameter (not Authorization header)
            body.add("token", apiKey);

            logger.debug("Request body prepared with file and token");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Send request
            logger.debug("Sending POST request to picui.cn...");
            ResponseEntity<PicuiResponse> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    PicuiResponse.class
            );
            logger.debug("Received response with status: {}", response.getStatusCode());

            // Validate response
            if (!response.getStatusCode().is2xxSuccessful()) {
                logger.error("Image upload failed with status: {}", response.getStatusCode());
                throw new IllegalStateException("Image upload failed with status: " + response.getStatusCode());
            }

            PicuiResponse responseBody = response.getBody();
            if (responseBody == null) {
                logger.error("Image upload response body is null");
                throw new IllegalStateException("Image upload response body is null");
            }

            // Check for API errors
            if (!responseBody.isSuccess()) {
                String errorMsg = responseBody.getMessage() != null ? responseBody.getMessage() : "Unknown error";
                logger.error("Image upload failed: {}", errorMsg);
                throw new IllegalStateException("Image upload failed: " + errorMsg);
            }

            // Extract thumbnail URL
            if (responseBody.getData() == null || responseBody.getData().getLinks() == null) {
                logger.error("Image upload response missing data or links");
                throw new IllegalStateException("Image upload response missing data or links");
            }

            String thumbnailUrl = responseBody.getData().getLinks().getThumbUrl();
            if (thumbnailUrl == null || thumbnailUrl.isEmpty()) {
                logger.error("Image upload response missing thumbnail URL");
                throw new IllegalStateException("Image upload response missing thumbnail URL");
            }

            logger.info("Image uploaded successfully: thumbnailUrl={}", thumbnailUrl);
            return thumbnailUrl;

        } catch (IOException e) {
            logger.error("Failed to read image file: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to read image file: " + e.getMessage(), e);
        } catch (RestClientException e) {
            logger.error("REST client error during image upload: {}", e.getMessage(), e);
            throw new IllegalStateException("Image upload failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during image upload: {}", e.getMessage(), e);
            throw new IllegalStateException("Image upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * Response structure from picui.cn API
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class PicuiResponse {
        @JsonProperty("status")
        private boolean status;

        @JsonProperty("message")
        private String message;

        @JsonProperty("data")
        private PicuiData data;

        public boolean isSuccess() {
            return status;
        }
    }

    /**
     * Data structure containing image links
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class PicuiData {
        @JsonProperty("links")
        private PicuiLinks links;
    }

    /**
     * Links structure containing various image URLs
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class PicuiLinks {
        @JsonProperty("url")
        private String url;

        @JsonProperty("thumb_url")
        private String thumbUrl;

        @JsonProperty("html")
        private String html;

        @JsonProperty("bbcode")
        private String bbcode;

        @JsonProperty("markdown")
        private String markdown;
    }
}
