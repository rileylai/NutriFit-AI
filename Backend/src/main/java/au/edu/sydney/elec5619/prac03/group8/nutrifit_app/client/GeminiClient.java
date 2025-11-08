package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class GeminiClient {

    private static final String DEFAULT_CHAT_COMPLETIONS_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL_NAME = "google/gemini-2.0-flash-001";
    private static final String MEAL_MODEL_NAME = "google/gemini-2.5-flash-lite-preview-09-2025";
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(?:json)?\\s*(\\{.*?})\\s*```", Pattern.DOTALL);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String apiUrl;

    public GeminiClient(RestTemplateBuilder restTemplateBuilder,
                        ObjectMapper objectMapper,
                        @Value("${gemini.api.key:}") String apiKey,
                        @Value("${gemini.api.url:" + DEFAULT_CHAT_COMPLETIONS_URL + "}") String apiUrl) {
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    public GeminiEstimation estimateWorkout(String description) {
        log.info("Estimating workout for description: {}", description);
        if (!StringUtils.hasText(apiKey)) {
            log.error("Gemini API key is not configured");
            throw new IllegalStateException("Gemini API key is not configured.");
        }
        if (!StringUtils.hasText(description)) {
            log.error("Workout description is empty");
            throw new IllegalArgumentException("Workout description must not be empty.");
        }

        String prompt = "Estimate the total duration and calories burned for the following workout: '" + description
                + "'. Return result in JSON format like {duration_minutes, exercise_type, calories_burned}.";

        OpenRouterRequest requestPayload = new OpenRouterRequest(
                MODEL_NAME,
                List.of(new OpenRouterMessage("user", prompt))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);

        HttpEntity<OpenRouterRequest> requestEntity = new HttpEntity<>(requestPayload, headers);

        log.debug("Sending workout estimation request to OpenRouter API");
        try {
            ResponseEntity<OpenRouterResponse> response = restTemplate.postForEntity(apiUrl, requestEntity, OpenRouterResponse.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("OpenRouter API returned non-success status: {}", response.getStatusCode());
                throw new IllegalStateException("OpenRouter API returned status " + response.getStatusCode());
            }

            OpenRouterResponse responseBody = response.getBody();
            if (responseBody == null) {
                log.error("OpenRouter API returned empty response body");
                throw new IllegalStateException("OpenRouter API returned no body.");
            }

            if (responseBody.getError() != null && StringUtils.hasText(responseBody.getError().getMessage())) {
                log.error("OpenRouter API error: {}", responseBody.getError().getMessage());
                throw new IllegalStateException("OpenRouter API error: " + responseBody.getError().getMessage());
            }

            String payload = responseBody.firstMessageContent()
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .orElseThrow(() -> {
                        log.error("OpenRouter API response missing text content");
                        return new IllegalStateException("OpenRouter API response missing text content.");
                    });

            log.debug("OpenRouter raw response payload: {}", payload);

            String jsonPayload = extractJsonPayload(payload);

            GeminiEstimation estimation = objectMapper.readValue(jsonPayload, GeminiEstimation.class);
            log.info("Workout estimation successful: {} minutes, {} calories, type: {}",
                    estimation.getDurationMinutes(), estimation.getCaloriesBurned(), estimation.getExerciseType());
            return estimation;
        } catch (JsonProcessingException ex) {
            log.error("Failed to parse OpenRouter response payload: {}", ex.getMessage(), ex);
            throw new IllegalStateException("Failed to parse OpenRouter response payload.", ex);
        } catch (Exception ex) {
            log.error("Error calling OpenRouter API for workout estimation: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    // AI Insight Generation for homepage components
    public String generateAIInsight(String analysisType, String userContext) {
        log.info("Generating AI insight for analysis type: {}", analysisType);
        if (!StringUtils.hasText(apiKey)) {
            log.error("Gemini API key is not configured");
            throw new IllegalStateException("Gemini API key is not configured.");
        }
        if (!StringUtils.hasText(analysisType)) {
            log.error("Analysis type is empty");
            throw new IllegalArgumentException("Analysis type must not be empty.");
        }

        String prompt = buildInsightPrompt(analysisType, userContext);
        log.debug("Generated insight prompt for type: {}", analysisType);

        OpenRouterRequest requestPayload = new OpenRouterRequest(
                MODEL_NAME,
                List.of(new OpenRouterMessage("user", prompt))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);

        HttpEntity<OpenRouterRequest> requestEntity = new HttpEntity<>(requestPayload, headers);

        log.debug("Sending AI insight request to OpenRouter API");
        try {
            ResponseEntity<OpenRouterResponse> response = restTemplate.postForEntity(apiUrl, requestEntity, OpenRouterResponse.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("OpenRouter API returned non-success status: {}", response.getStatusCode());
                throw new IllegalStateException("OpenRouter API returned status " + response.getStatusCode());
            }

            OpenRouterResponse responseBody = response.getBody();
            if (responseBody == null) {
                log.error("OpenRouter API returned empty response body");
                throw new IllegalStateException("OpenRouter API returned no body.");
            }

            if (responseBody.getError() != null && StringUtils.hasText(responseBody.getError().getMessage())) {
                log.error("OpenRouter API error: {}", responseBody.getError().getMessage());
                throw new IllegalStateException("OpenRouter API error: " + responseBody.getError().getMessage());
            }

            String insight = responseBody.firstMessageContent()
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .orElseThrow(() -> {
                        log.error("OpenRouter API response missing text content");
                        return new IllegalStateException("OpenRouter API response missing text content.");
                    });

            log.info("AI insight generated successfully for analysis type: {}, length: {}",
                    analysisType, insight.length());
            return insight;
        } catch (Exception ex) {
            log.error("Error calling OpenRouter API for AI insight: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    public List<String> generateSuggestions(String suggestionType, String userGoal, String userContext) {
        log.info("Generating suggestions for type: {}, goal: {}", suggestionType, userGoal);
        if (!StringUtils.hasText(apiKey)) {
            log.error("Gemini API key is not configured");
            throw new IllegalStateException("Gemini API key is not configured.");
        }
        if (!StringUtils.hasText(suggestionType)) {
            log.error("Suggestion type is empty");
            throw new IllegalArgumentException("Suggestion type must not be empty.");
        }

        String prompt = buildSuggestionPrompt(suggestionType, userGoal, userContext);
        log.debug("Generated suggestion prompt for type: {}", suggestionType);

        OpenRouterRequest requestPayload = new OpenRouterRequest(
                MODEL_NAME,
                List.of(new OpenRouterMessage("user", prompt))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);

        HttpEntity<OpenRouterRequest> requestEntity = new HttpEntity<>(requestPayload, headers);

        log.debug("Sending suggestions request to OpenRouter API");
        try {
            ResponseEntity<OpenRouterResponse> response = restTemplate.postForEntity(apiUrl, requestEntity, OpenRouterResponse.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("OpenRouter API returned non-success status: {}", response.getStatusCode());
                throw new IllegalStateException("OpenRouter API returned status " + response.getStatusCode());
            }

            OpenRouterResponse responseBody = response.getBody();
            if (responseBody == null) {
                log.error("OpenRouter API returned empty response body");
                throw new IllegalStateException("OpenRouter API returned no body.");
            }

            if (responseBody.getError() != null && StringUtils.hasText(responseBody.getError().getMessage())) {
                log.error("OpenRouter API error: {}", responseBody.getError().getMessage());
                throw new IllegalStateException("OpenRouter API error: " + responseBody.getError().getMessage());
            }

            String content = responseBody.firstMessageContent()
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .orElseThrow(() -> {
                        log.error("OpenRouter API response missing text content");
                        return new IllegalStateException("OpenRouter API response missing text content.");
                    });

            List<String> suggestions = parseSuggestionsList(content);
            log.info("Generated {} suggestions for type: {}", suggestions.size(), suggestionType);
            return suggestions;
        } catch (Exception ex) {
            log.error("Error calling OpenRouter API for suggestions: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    private String buildInsightPrompt(String analysisType, String userContext) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("As a fitness and nutrition expert, analyze the following user data and provide personalized insights:\n\n");
        prompt.append("Analysis Type: ").append(analysisType).append("\n");
        prompt.append("User Context: ").append(userContext != null ? userContext : "Limited data available").append("\n\n");

        prompt.append("Please provide a concise analysis that includes:\n");
        prompt.append("1. Key observations about current patterns (1-2 sentences)\n");
        prompt.append("2. Specific recommendations for improvement (up to 3 bullet points)\n");
        prompt.append("3. Actionable next steps (up to 3 bullet points)\n");
        prompt.append("4. Potential concerns or areas to monitor (optional, 1 sentence)\n\n");

        prompt.append("Format your response as markdown with clear sections and emojis for readability. ");
        prompt.append("Keep the tone encouraging but realistic, base recommendations on evidence-based fitness and nutrition principles, ");
        prompt.append("and keep the entire response under 200 words.");

        return prompt.toString();
    }

    private String buildSuggestionPrompt(String suggestionType, String userGoal, String userContext) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate specific ").append(suggestionType).append(" recommendations for a user with the goal: ").append(userGoal).append("\n\n");
        prompt.append("User Context: ").append(userContext != null ? userContext : "General recommendations").append("\n\n");

        prompt.append("Provide 4 specific, actionable recommendations. Each recommendation should be:\n");
        prompt.append("- Practical and achievable\n");
        prompt.append("- Evidence-based\n");
        prompt.append("- Tailored to the user's goal\n");
        prompt.append("- Include specific metrics or targets where appropriate\n");
        prompt.append("- Respect any stated preferences, constraints, schedule, equipment, or dietary requirements\n\n");

        prompt.append("Format your response as a numbered list with each recommendation on a separate line.");

        return prompt.toString();
    }

    private List<String> parseSuggestionsList(String content) {
        List<String> suggestions = List.of();
        try {
            String[] lines = content.split("\n");
            suggestions = List.of(lines).stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .filter(line -> line.matches("^\\d+\\..*") || line.startsWith("•") || line.startsWith("-"))
                    .map(line -> line.replaceFirst("^\\d+\\.\\s*", "").replaceFirst("^[•-]\\s*", ""))
                    .filter(StringUtils::hasText)
                    .toList();
            log.debug("Parsed {} suggestions from content", suggestions.size());
        } catch (Exception e) {
            log.warn("Failed to parse suggestions list, returning raw content as single item", e);
            suggestions = List.of(content);
        }
        return suggestions;
    }

    /**
     * Estimates meal nutrition from an image URL and/or text description.
     * Uses Gemini 2.5 Flash Lite with vision capabilities.
     *
     * @param imageUrl URL of the meal image (can be null if description provided)
     * @param description Text description of the meal (can be null if image provided)
     * @return MealEstimation containing nutrition values
     * @throws IllegalArgumentException if both imageUrl and description are empty
     * @throws IllegalStateException if API call fails
     */
    public MealEstimation estimateMeal(String imageUrl, String description) {
        log.info("Estimating meal nutrition - hasImage: {}, hasDescription: {}",
                StringUtils.hasText(imageUrl), StringUtils.hasText(description));

        if (!StringUtils.hasText(apiKey)) {
            log.error("Gemini API key is not configured");
            throw new IllegalStateException("Gemini API key is not configured.");
        }

        boolean hasImage = StringUtils.hasText(imageUrl);
        boolean hasDescription = StringUtils.hasText(description);

        if (!hasImage && !hasDescription) {
            log.error("Both image URL and description are empty");
            throw new IllegalArgumentException("At least one of image URL or description must be provided.");
        }

        // Build the prompt
        String prompt = buildMealEstimationPrompt(description);
        log.debug("Built meal estimation prompt");

        // Build message content parts
        List<ContentPart> contentParts = new java.util.ArrayList<>();
        contentParts.add(MessageContent.text(prompt));

        if (hasImage) {
            contentParts.add(MessageContent.imageUrl(imageUrl));
            log.debug("Added image URL to request: {}", imageUrl);
        }

        OpenRouterMessage message = new OpenRouterMessage("user", contentParts);
        OpenRouterRequest requestPayload = new OpenRouterRequest(MEAL_MODEL_NAME, List.of(message));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);

        HttpEntity<OpenRouterRequest> requestEntity = new HttpEntity<>(requestPayload, headers);

        log.debug("Sending meal estimation request to OpenRouter API");
        log.debug("Model: {}", MEAL_MODEL_NAME);
        log.debug("Content parts count: {}", contentParts.size());
        if (hasImage) {
            log.info("Sending image URL to Gemini AI: {}", imageUrl);
        }
        try {
            ResponseEntity<OpenRouterResponse> response = restTemplate.postForEntity(apiUrl, requestEntity, OpenRouterResponse.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("OpenRouter API returned non-success status: {}", response.getStatusCode());
                throw new IllegalStateException("OpenRouter API returned status " + response.getStatusCode());
            }

            OpenRouterResponse responseBody = response.getBody();
            if (responseBody == null) {
                log.error("OpenRouter API returned empty response body");
                throw new IllegalStateException("OpenRouter API returned no body.");
            }

            if (responseBody.getError() != null && StringUtils.hasText(responseBody.getError().getMessage())) {
                log.error("OpenRouter API error: {}", responseBody.getError().getMessage());
                throw new IllegalStateException("OpenRouter API error: " + responseBody.getError().getMessage());
            }

            String payload = responseBody.firstMessageContent()
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .orElseThrow(() -> {
                        log.error("OpenRouter API response missing text content");
                        return new IllegalStateException("OpenRouter API response missing text content.");
                    });

            log.debug("OpenRouter raw response payload: {}", payload);

            String jsonPayload = extractJsonPayload(payload);

            MealEstimation estimation = objectMapper.readValue(jsonPayload, MealEstimation.class);
            log.info("Meal estimation successful: {} calories, {}g protein, {}g carbs, {}g fat",
                    estimation.getTotalCalories(), estimation.getProteinG(),
                    estimation.getCarbsG(), estimation.getFatG());
            return estimation;
        } catch (JsonProcessingException ex) {
            log.error("Failed to parse OpenRouter response payload: {}", ex.getMessage(), ex);
            throw new IllegalStateException("Failed to parse OpenRouter response payload.", ex);
        } catch (Exception ex) {
            log.error("Error calling OpenRouter API for meal estimation: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Builds the prompt for meal estimation based on available inputs.
     */
    private String buildMealEstimationPrompt(String userDescription) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the provided meal ");

        if (StringUtils.hasText(userDescription)) {
            prompt.append("(described as: \"").append(userDescription).append("\") ");
        }

        prompt.append("and estimate its nutritional content.\n\n");

        prompt.append("Provide a detailed analysis in the following JSON format:\n");
        prompt.append("{\n");
        prompt.append("  \"total_calories\": <estimated calories as a number>,\n");
        prompt.append("  \"protein_g\": <estimated protein in grams>,\n");
        prompt.append("  \"carbs_g\": <estimated carbohydrates in grams>,\n");
        prompt.append("  \"fat_g\": <estimated fat in grams>,\n");
        prompt.append("  \"meal_description\": \"<brief description of the meal>\"\n");
        prompt.append("}\n\n");

        prompt.append("Guidelines:\n");
        prompt.append("- Be realistic with portion sizes\n");
        prompt.append("- Provide reasonable estimates based on visible food items\n");
        prompt.append("- Use standard serving sizes if portions are unclear\n");
        prompt.append("- If multiple items are present, sum up the total nutrition\n");
        prompt.append("- Keep the meal description concise (under 100 characters)\n");
        prompt.append("- Return ONLY the JSON object, no additional text\n");

        return prompt.toString();
    }

    private record OpenRouterRequest(String model, List<OpenRouterMessage> messages) {
    }

    private record OpenRouterMessage(String role, Object content) {
        // Constructor for text-only messages
        OpenRouterMessage(String role, String textContent) {
            this(role, (Object) textContent);
        }

        // Constructor for multimodal messages (text + image)
        OpenRouterMessage(String role, List<ContentPart> contentParts) {
            this(role, (Object) contentParts);
        }
    }

    // Base interface for content (Jackson will serialize properly)
    private interface ContentPart {
        String getType();
    }

    // Text content part
    private record TextContent(String type, String text) implements ContentPart {
        TextContent(String text) {
            this("text", text);
        }

        @Override
        public String getType() {
            return type;
        }
    }

    // Image URL content part
    private record ImageUrlContent(String type, ImageUrl image_url) implements ContentPart {
        ImageUrlContent(String url) {
            this("image_url", new ImageUrl(url));
        }

        @Override
        public String getType() {
            return type;
        }
    }

    private record ImageUrl(String url) {
    }

    // Helper class for building messages
    private static class MessageContent {
        static ContentPart text(String text) {
            return new TextContent(text);
        }

        static ContentPart imageUrl(String url) {
            return new ImageUrlContent(url);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeminiEstimation {

        @JsonProperty("duration_minutes")
        private Integer durationMinutes;

        @JsonProperty("exercise_type")
        private String exerciseType;

        @JsonProperty("calories_burned")
        private BigDecimal caloriesBurned;

        public Integer getDurationMinutes() {
            return durationMinutes;
        }

        public String getExerciseType() {
            return exerciseType;
        }

        public BigDecimal getCaloriesBurned() {
            return caloriesBurned;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MealEstimation {

        @JsonProperty("total_calories")
        private BigDecimal totalCalories;

        @JsonProperty("protein_g")
        private BigDecimal proteinG;

        @JsonProperty("carbs_g")
        private BigDecimal carbsG;

        @JsonProperty("fat_g")
        private BigDecimal fatG;

        @JsonProperty("meal_description")
        private String mealDescription;

        public BigDecimal getTotalCalories() {
            return totalCalories;
        }

        public void setTotalCalories(BigDecimal totalCalories) {
            this.totalCalories = totalCalories;
        }

        public BigDecimal getProteinG() {
            return proteinG;
        }

        public void setProteinG(BigDecimal proteinG) {
            this.proteinG = proteinG;
        }

        public BigDecimal getCarbsG() {
            return carbsG;
        }

        public void setCarbsG(BigDecimal carbsG) {
            this.carbsG = carbsG;
        }

        public BigDecimal getFatG() {
            return fatG;
        }

        public void setFatG(BigDecimal fatG) {
            this.fatG = fatG;
        }

        public String getMealDescription() {
            return mealDescription;
        }

        public void setMealDescription(String mealDescription) {
            this.mealDescription = mealDescription;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class OpenRouterResponse {

        private List<Choice> choices;
        private Error error;

        public List<Choice> getChoices() {
            return choices;
        }

        public void setChoices(List<Choice> choices) {
            this.choices = choices;
        }

        public Error getError() {
            return error;
        }

        public void setError(Error error) {
            this.error = error;
        }

        private Optional<String> firstMessageContent() {
            if (choices == null) {
                return Optional.empty();
            }
            return choices.stream()
                    .map(Choice::getMessage)
                    .filter(Objects::nonNull)
                    .map(ChoiceMessage::getContent)
                    .filter(StringUtils::hasText)
                    .findFirst();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Choice {

        private ChoiceMessage message;

        public ChoiceMessage getMessage() {
            return message;
        }

        public void setMessage(ChoiceMessage message) {
            this.message = message;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ChoiceMessage {

        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Error {

        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    private String extractJsonPayload(String raw) {
        String trimmed = raw.trim();

        Matcher matcher = CODE_BLOCK_PATTERN.matcher(trimmed);
        if (matcher.find()) {
            trimmed = matcher.group(1).trim();
        }

        if (!(trimmed.startsWith("{") && trimmed.endsWith("}"))) {
            int start = trimmed.indexOf('{');
            int end = trimmed.lastIndexOf('}');
            if (start >= 0 && end >= start) {
                trimmed = trimmed.substring(start, end + 1);
            }
        }

        if (!(trimmed.startsWith("{") && trimmed.endsWith("}"))) {
            throw new IllegalStateException("OpenRouter response did not include JSON payload.");
        }

        return trimmed;
    }
}
