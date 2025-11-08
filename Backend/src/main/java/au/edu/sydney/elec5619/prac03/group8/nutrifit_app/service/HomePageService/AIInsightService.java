package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.HomePageService;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.AIInsight;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.Suggestion;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.User;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Common.AIInsightDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.CreateInsightRequestDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.SuggestionRequestDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.SuggestionResponseDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.AIInsightRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.SuggestionRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.client.GeminiClient;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.QuickStatsResponseDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.security.SecurityUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
@Service
@Transactional
public class AIInsightService {
    private static final int MAX_RECOMMENDATION_COUNT = 3;
    private static final int MAX_WORDS_PER_RECOMMENDATION = 100;
    @Autowired
    private AIInsightRepository aiInsightRepository;
    
    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private GeminiClient geminiClient;
    
    @Autowired
    private SuggestionRepository suggestionRepository;
    
    @Autowired
    private SecurityUtil securityUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Get latest active insights for user
     */
    public List<AIInsightDTO> getLatestInsights() {
        try {
            // First, mark expired insights as inactive
            markExpiredInsights();

            User currentUser = securityUtil.getCurrentUserOrThrow();
            
            List<AIInsight> insights = aiInsightRepository.findLatestActiveInsights(currentUser.getUserId());
            
            return insights.stream()
                .limit(10) // Limit to 10 most recent
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            throw new RuntimeException("Error fetching latest insights: " + e.getMessage());
        }
    }
    
    /**
     * Generate new AI insights for user
     */
    public AIInsightDTO generateInsight(CreateInsightRequestDTO request) {
        try {
            User user = securityUtil.getCurrentUserOrThrow();

            Long resolvedUserId = user.getUserId();
            if (request.getUserId() != null && !request.getUserId().equals(resolvedUserId)) {
                // Frontend may send stale or anonymous IDs; respect authenticated user instead of failing
                request.setUserId(resolvedUserId);
            }
            request.setUserId(resolvedUserId);

            String resolvedAnalysisType = request.getResolvedAnalysisType();
            String normalizedAnalysisType = normalizeAnalysisType(resolvedAnalysisType);
            request.setAnalysisType(normalizedAnalysisType);
            String promptAnalysisType = hasText(resolvedAnalysisType) ? resolvedAnalysisType : normalizedAnalysisType;
            if (!hasText(promptAnalysisType)) {
                promptAnalysisType = "overall";
            }

            // Check if recent insight exists (unless force regenerate)
            if (!Boolean.TRUE.equals(request.getForceRegenerate())) {
                LocalDateTime recentThreshold = LocalDateTime.now().minusHours(2);
                List<AIInsight> recentInsights = aiInsightRepository
                    .findRecentInsights(user.getUserId(), recentThreshold);

                if (!recentInsights.isEmpty()) {
                    return convertToDTO(recentInsights.get(0));
                }
            }
            
            // Generate new insight based on analysis type
            String insightContent = generateInsightContent(user, promptAnalysisType);
            String suggestionFormat = determineSuggestionFormat(insightContent, normalizedAnalysisType);
            
            // Create new insight
            AIInsight newInsight = new AIInsight();
            newInsight.setUser(user);
            newInsight.setContent(insightContent);
            newInsight.setSuggestionFormat(suggestionFormat);
            newInsight.setIsActive(true);
            newInsight.setExpiresAt(LocalDateTime.now().plusDays(7)); // Expire in 7 days
            
            AIInsight savedInsight = aiInsightRepository.save(newInsight);
            
            return convertToDTO(savedInsight);
            
        } catch (Exception e) {
            throw new RuntimeException("Error generating insight: " + e.getMessage());
        }
    }
    
    /**
     * Get detailed insight by ID
     */
    public AIInsightDTO getInsightDetails(Long insightId) {
        try {
            AIInsight insight = aiInsightRepository.findById(insightId)
                .orElseThrow(() -> new RuntimeException("Insight not found"));

            User currentUser = securityUtil.getCurrentUserOrThrow();
            
            // Verify insight belongs to user
            if (!insight.getUser().getUserId().equals(currentUser.getUserId())) {
                throw new RuntimeException("Unauthorized access to insight");
            }
            
            return convertToDTO(insight);
            
        } catch (Exception e) {
            throw new RuntimeException("Error fetching insight details: " + e.getMessage());
        }
    }
    
    /**
     * Generate specific exercise/diet suggestions
     */
    public SuggestionResponseDTO generateSuggestions(SuggestionRequestDTO request) {
        try {
            User user = securityUtil.getCurrentUserOrThrow();

            Long resolvedUserId = user.getUserId();
            if (request.getUserId() != null && !request.getUserId().equals(resolvedUserId)) {
                request.setUserId(resolvedUserId);
            }
            request.setUserId(resolvedUserId);

            if (!hasText(request.getTimeFrame())) {
                request.setTimeFrame("week");
            }
            
            SuggestionResponseDTO response;

            if ("exercise".equalsIgnoreCase(request.getSuggestionType())) {
                response = generateExerciseSuggestions(user, request);
            } else if ("diet".equalsIgnoreCase(request.getSuggestionType())) {
                response = generateDietSuggestions(user, request);
            } else {
                response = new SuggestionResponseDTO();
                response.setRecommendations(Collections.emptyList());
                response.setSpecificMetrics(Collections.emptyMap());
                response.setRationale("Unsupported suggestion type: " + request.getSuggestionType());
                response.setConfidenceScore(0);
            }

            response.setSuggestionType(request.getSuggestionType());
            response.setUserGoal(request.getUserGoal());

            saveSuggestion(user, request, response);

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error generating suggestions: " + e.getMessage(), e);
        }
    }
    
    /**
     * Soft delete an insight by marking it inactive for the current user.
     */
    public void deactivateInsight(Long insightId) {
        User currentUser = securityUtil.getCurrentUserOrThrow();

        AIInsight insight = aiInsightRepository.findById(insightId)
            .orElseThrow(() -> new RuntimeException("Insight not found"));

        if (!insight.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("Unauthorized access to insight");
        }

        if (Boolean.TRUE.equals(insight.getIsActive())) {
            insight.setIsActive(false);
            aiInsightRepository.save(insight);
        }
    }

    /**
     * Soft delete a suggestion belonging to the current user.
     */
    public void deleteSuggestion(Long suggestionId) {
        User currentUser = securityUtil.getCurrentUserOrThrow();

        Suggestion suggestion = suggestionRepository.findById(suggestionId)
            .orElseThrow(() -> new RuntimeException("Suggestion not found"));

        if (!suggestion.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("Unauthorized access to suggestion");
        }

        if (Boolean.TRUE.equals(suggestion.getIsActive())) {
            suggestion.setIsActive(false);
            suggestionRepository.save(suggestion);
        }
    }

    /**
     * Fetch the latest active suggestion for the current user.
     */
    public Map<String, Object> getLatestSuggestionSummary() {
        User currentUser = securityUtil.getCurrentUserOrThrow();

        return suggestionRepository
            .findTopByUserUserIdAndIsActiveTrueOrderByCreatedAtDesc(currentUser.getUserId())
            .map(this::convertSuggestionToSummary)
            .orElse(null);
    }
    
    /**
     * Mark expired insights as inactive
     */
    public void markExpiredInsights() {
        List<AIInsight> expiredInsights = aiInsightRepository.findExpiredInsights(LocalDateTime.now());
        
        for (AIInsight insight : expiredInsights) {
            insight.setIsActive(false);
        }
        
        if (!expiredInsights.isEmpty()) {
            aiInsightRepository.saveAll(expiredInsights);
        }
    }
    
    // Private helper methods

    private String normalizeAnalysisType(String analysisType) {
        if (!hasText(analysisType)) {
            return "overall";
        }

        String value = analysisType.trim().toLowerCase();
        switch (value) {
            case "fitness":
            case "exercise":
            case "workout":
            case "training":
                return "exercise";
            case "nutrition":
            case "diet":
            case "dietary":
            case "food":
                return "nutrition";
            case "overall":
            case "general":
            case "wellness":
            case "health":
                return "overall";
            default:
                return value;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String generateInsightContent(User user, String analysisType) {
        try {
            // Get user's recent data
            QuickStatsResponseDTO quickStats = dashboardService.getQuickStats(user.getUserId());
            String userContext = buildUserContext(user, quickStats);

            // Use Gemini API to generate personalized insight
            return geminiClient.generateAIInsight(analysisType, userContext);

        } catch (Exception e) {
            // Fallback to default message if API fails
            return "Unable to generate detailed analysis at this time. Please ensure your profile and recent activity data are up to date. Error: " + e.getMessage();
        }
    }

    private String determineSuggestionFormat(String content, String analysisType) {
        String normalizedType = normalizeAnalysisType(analysisType);
        if ("nutrition".equals(normalizedType) || "exercise".equals(normalizedType)) {
            return normalizedType;
        }

        String lowerContent = content != null ? content.toLowerCase() : "";
        if (lowerContent.contains("protein") || lowerContent.contains("calories") || lowerContent.contains("nutrition")) {
            return "nutrition";
        } else if (lowerContent.contains("workout") || lowerContent.contains("exercise") || lowerContent.contains("streak")) {
            return "exercise";
        } else {
            return "general";
        }
    }
    
    private SuggestionResponseDTO generateExerciseSuggestions(User user, SuggestionRequestDTO request) {
        SuggestionResponseDTO response = new SuggestionResponseDTO();
        Map<String, Object> metrics = new HashMap<>();

        try {
            // Get user's recent workout data
            QuickStatsResponseDTO quickStats = dashboardService.getQuickStats(user.getUserId());
            String userContext = buildUserContext(user, quickStats);

            String preferenceContext = buildPreferenceContext(request);
            String promptContext = mergeContexts(userContext, preferenceContext);

            // Use Gemini API to generate personalized exercise suggestions
            List<String> recommendations = geminiClient.generateSuggestions("exercise", request.getUserGoal(), promptContext);
            recommendations = limitRecommendationLength(recommendations);

            // Set default metrics based on goal
            switch (request.getUserGoal().toLowerCase()) {
                case "weight_loss":
                    metrics.put("targetCaloriesBurn", "300-500 per session");
                    metrics.put("recommendedDuration", "30-45 minutes");
                    metrics.put("weeklyFrequency", "4-5 times");
                    break;
                case "muscle_gain":
                    metrics.put("targetSets", "3-4 per exercise");
                    metrics.put("targetReps", "8-12 for hypertrophy");
                    metrics.put("weeklyFrequency", "4-5 times");
                    break;
                case "maintenance":
                default:
                    metrics.put("weeklyMinutes", "150-300");
                    metrics.put("weeklyFrequency", "3-4 times");
                    break;
            }

            response.setRecommendations(recommendations);
            response.setSpecificMetrics(metrics);
            response.setRationale("AI-generated recommendations based on your goal of " + request.getUserGoal() + " and current activity level");
            response.setConfidenceScore(90);

        } catch (Exception e) {
            // Fallback to basic recommendations if API fails
            List<String> fallbackRecommendations = new ArrayList<>();
            fallbackRecommendations.add("Consult with a fitness professional for personalized recommendations");
            fallbackRecommendations.add("Start with moderate exercise 3-4 times per week");
            fallbackRecommendations.add("Include both cardio and strength training");

            response.setRecommendations(limitRecommendationLength(fallbackRecommendations));
            response.setSpecificMetrics(metrics);
            response.setRationale("Basic recommendations due to API unavailability: " + e.getMessage());
            response.setConfidenceScore(50);
        }

        return response;
    }
    
    private SuggestionResponseDTO generateDietSuggestions(User user, SuggestionRequestDTO request) {
        SuggestionResponseDTO response = new SuggestionResponseDTO();
        Map<String, Object> metrics = new HashMap<>();

        try {
            // Get user's body metrics for calculations
            QuickStatsResponseDTO quickStats = dashboardService.getQuickStats(user.getUserId());
            double weight = quickStats.getBodyMetrics() != null ? quickStats.getBodyMetrics().getWeight() : 70.0;
            double bmr = quickStats.getBodyMetrics() != null ? quickStats.getBodyMetrics().getBmr() : 1500.0;

            String userContext = buildUserContext(user, quickStats);
            String preferenceContext = buildPreferenceContext(request);
            String promptContext = mergeContexts(userContext, preferenceContext);

            // Use Gemini API to generate personalized diet suggestions
            List<String> recommendations = geminiClient.generateSuggestions("diet", request.getUserGoal(), promptContext);
            recommendations = limitRecommendationLength(recommendations);

            // Calculate metrics based on goal and BMR
            switch (request.getUserGoal().toLowerCase()) {
                case "weight_loss":
                    double deficitCalories = bmr * 1.4 - 300; // 300 calorie deficit
                    metrics.put("targetCalories", Math.round(deficitCalories));
                    metrics.put("proteinTarget", Math.round(weight * 1.6) + "g");
                    metrics.put("calorieDeficit", "300-500 calories");
                    break;
                case "muscle_gain":
                    double surplusCalories = bmr * 1.6 + 200; // 200 calorie surplus
                    metrics.put("targetCalories", Math.round(surplusCalories));
                    metrics.put("proteinTarget", Math.round(weight * 2.0) + "g");
                    metrics.put("calorieSurplus", "200-300 calories");
                    break;
                case "maintenance":
                default:
                    double maintenanceCalories = bmr * 1.4;
                    metrics.put("targetCalories", Math.round(maintenanceCalories));
                    metrics.put("proteinTarget", Math.round(weight * 1.4) + "g");
                    metrics.put("hydration", "8-10 glasses daily");
                    break;
            }

            response.setRecommendations(recommendations);
            response.setSpecificMetrics(metrics);
            response.setRationale("AI-generated recommendations based on your BMR of " + Math.round(bmr) + " calories and " + request.getUserGoal() + " goal");
            response.setConfidenceScore(95);

        } catch (Exception e) {
            // Fallback to basic recommendations if API fails
            List<String> fallbackRecommendations = new ArrayList<>();
            fallbackRecommendations.add("Consult with a nutritionist for personalized meal planning");
            fallbackRecommendations.add("Focus on whole, unprocessed foods");
            fallbackRecommendations.add("Stay hydrated throughout the day");
            fallbackRecommendations.add("Practice portion control");

            response.setRecommendations(limitRecommendationLength(fallbackRecommendations));
            response.setSpecificMetrics(metrics);
            response.setRationale("Basic recommendations due to API unavailability: " + e.getMessage());
            response.setConfidenceScore(50);
        }

        return response;
    }

    private void saveSuggestion(User user, SuggestionRequestDTO request, SuggestionResponseDTO response) {
        Suggestion suggestion = new Suggestion();
        suggestion.setUser(user);
        suggestion.setSuggestionType(response.getSuggestionType());
        suggestion.setUserGoal(response.getUserGoal());
        suggestion.setTimeFrame(request.getTimeFrame());
        suggestion.setRecommendations(joinRecommendations(response.getRecommendations()));
        suggestion.setSpecificMetrics(toJson(response.getSpecificMetrics()));
        suggestion.setRationale(response.getRationale());
        suggestion.setConfidenceScore(response.getConfidenceScore());
        suggestion.setRequestMetadata(toJson(buildRequestMetadata(request)));
        suggestion.setIsActive(true);

        suggestionRepository.save(suggestion);
    }

    private String joinRecommendations(List<String> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) {
            return "";
        }
        return String.join("\n", recommendations);
    }

    private Map<String, Object> convertSuggestionToSummary(Suggestion suggestion) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("suggestionId", suggestion.getSuggestionId());
        summary.put("suggestionType", suggestion.getSuggestionType());
        summary.put("userGoal", suggestion.getUserGoal());
        summary.put("timeFrame", suggestion.getTimeFrame());
        summary.put("recommendations", splitRecommendations(suggestion.getRecommendations()));
        summary.put("specificMetrics", parseSpecificMetrics(suggestion.getSpecificMetrics()));
        summary.put("rationale", suggestion.getRationale());
        summary.put("confidenceScore", suggestion.getConfidenceScore());
        summary.put("requestMetadata", parseRequestMetadata(suggestion.getRequestMetadata()));
        summary.put("createdAt", suggestion.getCreatedAt());
        return summary;
    }

    private List<String> splitRecommendations(String recommendations) {
        if (!hasText(recommendations)) {
            return Collections.emptyList();
        }

        return Arrays.stream(recommendations.split("\\r?\\n"))
            .map(String::trim)
            .filter(this::hasText)
            .collect(Collectors.toList());
    }

    private Map<String, Object> parseSpecificMetrics(String metricsJson) {
        return parseJsonToMap(metricsJson);
    }

    private Map<String, Object> parseRequestMetadata(String metadataJson) {
        return parseJsonToMap(metadataJson);
    }

    private Map<String, Object> parseJsonToMap(String json) {
        if (!hasText(json)) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("raw", json);
            return fallback;
        }
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize suggestion details", e);
        }
    }

    private List<String> limitRecommendationLength(List<String> recommendations) {
        if (recommendations == null) {
            return Collections.emptyList();
        }

        return recommendations.stream()
            .filter(this::hasText)
            .map(rec -> limitWords(rec, MAX_WORDS_PER_RECOMMENDATION))
            .limit(MAX_RECOMMENDATION_COUNT)
            .collect(Collectors.toList());
    }

    private String limitWords(String text, int maxWords) {
        if (!hasText(text)) {
            return text;
        }

        String[] words = text.trim().split("\\s+");
        if (words.length <= maxWords) {
            return text.trim();
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < maxWords; i++) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(words[i]);
        }
        builder.append("...");
        return builder.toString();
    }

    private String buildUserContext(User user, QuickStatsResponseDTO quickStats) {
        StringBuilder context = new StringBuilder();

        try {
            // User basic info
            context.append("User Profile:\n");

            // Body metrics
            if (quickStats.getBodyMetrics() != null) {
                var bodyMetrics = quickStats.getBodyMetrics();
                context.append("- Weight: ").append(bodyMetrics.getWeight()).append(" kg\n");
                context.append("- BMI: ").append(bodyMetrics.getBmi()).append("\n");
                context.append("- BMR: ").append(bodyMetrics.getBmr()).append(" calories\n");
                context.append("- Weight Trend: ").append(bodyMetrics.getWeightTrend()).append("\n");
            }

            // Weekly averages
            if (quickStats.getWeeklyAverages() != null) {
                var weeklyAverages = quickStats.getWeeklyAverages();
                context.append("- Average Calories Intake: ").append(weeklyAverages.getAvgCaloriesIntake()).append(" calories/day\n");
                if (weeklyAverages.getAvgMacros() != null) {
                    var avgMacros = weeklyAverages.getAvgMacros();
                    context.append("- Average Protein: ").append(avgMacros.getProtein()).append("g/day\n");
                    context.append("- Average Carbs: ").append(avgMacros.getCarbs()).append("g/day\n");
                    context.append("- Average Fats: ").append(avgMacros.getFats()).append("g/day\n");
                }
            }

            // Workout frequency and streaks
            if (quickStats.getWorkoutFrequency() != null) {
                var workoutFreq = quickStats.getWorkoutFrequency();
                context.append("- Workout Days This Week: ").append(workoutFreq.getWorkoutDays()).append("\n");
                context.append("- Workout Frequency: ").append(workoutFreq.getFrequencyPercentage()).append("%\n");
            }

            if (quickStats.getStreaks() != null) {
                var streaks = quickStats.getStreaks();
                context.append("- Current Workout Streak: ").append(streaks.getWorkoutStreak()).append(" days\n");
                context.append("- Current Nutrition Streak: ").append(streaks.getNutritionStreak()).append(" days\n");
            }

        } catch (Exception e) {
            context.append("Limited user data available for analysis.\n");
        }

        return context.toString();
    }

    private String buildPreferenceContext(SuggestionRequestDTO request) {
        List<String> details = new ArrayList<>();

        if (hasText(request.getPreferredIntensity())) {
            details.add("Preferred intensity: " + request.getPreferredIntensity());
        }
        if (hasText(request.getExperienceLevel())) {
            details.add("Experience level: " + request.getExperienceLevel());
        }
        if (request.getFocusAreas() != null && !request.getFocusAreas().isEmpty()) {
            String formatted = formatList(request.getFocusAreas());
            if (hasText(formatted)) {
                details.add("Focus areas: " + formatted);
            }
        }
        if (request.getEquipment() != null && !request.getEquipment().isEmpty()) {
            String formatted = formatList(request.getEquipment());
            if (hasText(formatted)) {
                details.add("Available equipment: " + formatted);
            }
        }
        if (request.getDietaryPreferences() != null && !request.getDietaryPreferences().isEmpty()) {
            String formatted = formatList(request.getDietaryPreferences());
            if (hasText(formatted)) {
                details.add("Dietary preferences: " + formatted);
            }
        }
        if (hasText(request.getWeeklySchedule())) {
            details.add("Weekly schedule: " + request.getWeeklySchedule());
        }
        if (request.getPreferredTimes() != null && !request.getPreferredTimes().isEmpty()) {
            String formatted = formatList(request.getPreferredTimes());
            if (hasText(formatted)) {
                details.add("Preferred times: " + formatted);
            }
        }
        if (hasText(request.getNotes())) {
            details.add("Additional notes: " + request.getNotes());
        }

        if (details.isEmpty()) {
            return "";
        }

        StringBuilder preferences = new StringBuilder();
        preferences.append("User Preferences and Constraints:\n");
        for (String detail : details) {
            preferences.append("- ").append(detail).append("\n");
        }

        return preferences.toString();
    }

    private Map<String, Object> buildRequestMetadata(SuggestionRequestDTO request) {
        Map<String, Object> metadata = new LinkedHashMap<>();

        if (hasText(request.getPreferredIntensity())) {
            metadata.put("preferredIntensity", request.getPreferredIntensity());
        }
        if (hasText(request.getExperienceLevel())) {
            metadata.put("experienceLevel", request.getExperienceLevel());
        }
        if (request.getFocusAreas() != null && !request.getFocusAreas().isEmpty()) {
            metadata.put("focusAreas", new ArrayList<>(request.getFocusAreas()));
        }
        if (request.getEquipment() != null && !request.getEquipment().isEmpty()) {
            metadata.put("equipment", new ArrayList<>(request.getEquipment()));
        }
        if (request.getDietaryPreferences() != null && !request.getDietaryPreferences().isEmpty()) {
            metadata.put("dietaryPreferences", new ArrayList<>(request.getDietaryPreferences()));
        }
        if (hasText(request.getWeeklySchedule())) {
            metadata.put("weeklySchedule", request.getWeeklySchedule());
        }
        if (request.getPreferredTimes() != null && !request.getPreferredTimes().isEmpty()) {
            metadata.put("preferredTimes", new ArrayList<>(request.getPreferredTimes()));
        }
        if (hasText(request.getNotes())) {
            metadata.put("notes", request.getNotes());
        }

        return metadata;
    }

    private String formatList(List<String> values) {
        return values.stream()
            .filter(this::hasText)
            .map(String::trim)
            .collect(Collectors.joining(", "));
    }

    private String mergeContexts(String... contexts) {
        return Arrays.stream(contexts)
            .filter(this::hasText)
            .collect(Collectors.joining("\n"));
    }

    private AIInsightDTO convertToDTO(AIInsight insight) {
        AIInsightDTO dto = new AIInsightDTO();
        dto.setInsightId(insight.getInsightId());
        dto.setContent(insight.getContent());
        dto.setSuggestionFormat(insight.getSuggestionFormat());
        dto.setIsActive(insight.getIsActive());
        LocalDateTime expiresAt = insight.getExpiresAt();
        LocalDateTime createdAt = insight.getCreatedAt();
        LocalDateTime updatedAt = insight.getUpdatedAt();
        LocalDateTime now = LocalDateTime.now();

        dto.setExpiresAt(expiresAt);
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);
        
        // Determine status while guarding against missing timestamps from older records
        if (expiresAt != null && expiresAt.isBefore(now)) {
            dto.setStatus("expired");
        } else if (createdAt != null && createdAt.isAfter(now.minusHours(1))) {
            dto.setStatus("new");
        } else {
            dto.setStatus("active");
        }
        
        // Set category and priority based on content
        dto.setCategory(insight.getSuggestionFormat());
        dto.setPriority(determinePriority(insight.getContent()));
        
        return dto;
    }
    
    private Integer determinePriority(String content) {
        if (content.contains("⚠️") || content.contains("critical") || content.contains("urgent")) {
            return 5; // High priority
        } else if (content.contains("recommendation") || content.contains("should")) {
            return 3; // Medium priority
        } else {
            return 1; // Low priority
        }
    }
}
