package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

/**
 * Client for uploading images to ImgBB image hosting service.
 * ImgBB is a free, stable image hosting service with reliable API.
 */
@Component
public class ImgBBClient {

    private static final Logger logger = LoggerFactory.getLogger(ImgBBClient.class);

    @Value("${imgbb.api.url:https://api.imgbb.com/1/upload}")
    private String apiUrl;

    @Value("${imgbb.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public ImgBBClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Uploads an image file to ImgBB and returns the image URL.
     *
     * @param file the image file to upload
     * @return the URL of the uploaded image
     * @throws IllegalStateException if upload fails
     */
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file cannot be null or empty");
        }

        logger.info("Uploading image to ImgBB: filename={}, size={} bytes",
                file.getOriginalFilename(), file.getSize());
        logger.debug("Using API URL: {}", apiUrl);
        logger.debug("API key configured: {}", apiKey != null && !apiKey.isEmpty() ? "Yes" : "No");

        try {
            // Convert image to Base64
            byte[] imageBytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            logger.debug("Image converted to Base64, length: {}", base64Image.length());

            // Prepare form data
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("key", apiKey);
            body.add("image", base64Image);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            // Send request
            logger.debug("Sending POST request to ImgBB...");
            ResponseEntity<ImgBBResponse> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    ImgBBResponse.class
            );
            logger.debug("Received response with status: {}", response.getStatusCode());

            // Validate response
            if (!response.getStatusCode().is2xxSuccessful()) {
                logger.error("Image upload failed with status: {}", response.getStatusCode());
                throw new IllegalStateException("Image upload failed with status: " + response.getStatusCode());
            }

            ImgBBResponse responseBody = response.getBody();
            if (responseBody == null) {
                logger.error("Image upload response body is null");
                throw new IllegalStateException("Image upload response body is null");
            }

            // Check for API errors
            if (!responseBody.isSuccess()) {
                String errorMsg = responseBody.getError() != null ? responseBody.getError().getMessage() : "Unknown error";
                logger.error("Image upload failed: {}", errorMsg);
                throw new IllegalStateException("Image upload failed: " + errorMsg);
            }

            // Extract image URL
            if (responseBody.getData() == null) {
                logger.error("Image upload response missing data");
                throw new IllegalStateException("Image upload response missing data");
            }

            // Use display_url (optimized for display) or fall back to url
            String imageUrl = responseBody.getData().getDisplayUrl();
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = responseBody.getData().getUrl();
            }

            if (imageUrl == null || imageUrl.isEmpty()) {
                logger.error("Image upload response missing image URL");
                throw new IllegalStateException("Image upload response missing image URL");
            }

            logger.info("Image uploaded successfully: url={}", imageUrl);
            logger.debug("Image ID: {}", responseBody.getData().getId());
            return imageUrl;

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
     * Response structure from ImgBB API
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ImgBBResponse {
        @JsonProperty("success")
        private boolean success;

        @JsonProperty("data")
        private ImgBBData data;

        @JsonProperty("error")
        private ImgBBError error;

        public boolean isSuccess() {
            return success;
        }
    }

    /**
     * Data structure containing image information
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ImgBBData {
        @JsonProperty("id")
        private String id;

        @JsonProperty("title")
        private String title;

        @JsonProperty("url_viewer")
        private String urlViewer;

        @JsonProperty("url")
        private String url;

        @JsonProperty("display_url")
        private String displayUrl;

        @JsonProperty("width")
        private String width;

        @JsonProperty("height")
        private String height;

        @JsonProperty("size")
        private String size;

        @JsonProperty("time")
        private String time;

        @JsonProperty("expiration")
        private String expiration;

        @JsonProperty("image")
        private ImgBBImageInfo image;

        @JsonProperty("thumb")
        private ImgBBThumb thumb;

        @JsonProperty("medium")
        private ImgBBMedium medium;

        @JsonProperty("delete_url")
        private String deleteUrl;
    }

    /**
     * Image information
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ImgBBImageInfo {
        @JsonProperty("filename")
        private String filename;

        @JsonProperty("name")
        private String name;

        @JsonProperty("mime")
        private String mime;

        @JsonProperty("extension")
        private String extension;

        @JsonProperty("url")
        private String url;
    }

    /**
     * Thumbnail information
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ImgBBThumb {
        @JsonProperty("filename")
        private String filename;

        @JsonProperty("name")
        private String name;

        @JsonProperty("mime")
        private String mime;

        @JsonProperty("extension")
        private String extension;

        @JsonProperty("url")
        private String url;
    }

    /**
     * Medium size image information
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ImgBBMedium {
        @JsonProperty("filename")
        private String filename;

        @JsonProperty("name")
        private String name;

        @JsonProperty("mime")
        private String mime;

        @JsonProperty("extension")
        private String extension;

        @JsonProperty("url")
        private String url;
    }

    /**
     * Error information
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ImgBBError {
        @JsonProperty("message")
        private String message;

        @JsonProperty("code")
        private Integer code;
    }
}
