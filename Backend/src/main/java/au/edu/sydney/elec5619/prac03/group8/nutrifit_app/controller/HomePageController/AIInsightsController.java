package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.controller.HomePageController;

import org.springframework.web.bind.annotation.RestController;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Common.AIInsightDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.CreateInsightRequestDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.SuggestionRequestDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.SuggestionResponseDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.HomePageService.AIInsightService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/homepage/ai-insights")
public class AIInsightsController {
    @Autowired
    private AIInsightService aiInsightService;
    
    /**
     * Get latest personalized recommendations
     * Serves: AIInsights component
     */
    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> getLatestInsights() {
        try {
            List<AIInsightDTO> insights = aiInsightService.getLatestInsights();
            Map<String, Object> latestSuggestion = aiInsightService.getLatestSuggestionSummary();

            Map<String, Object> response = new HashMap<>();
            response.put("insights", insights);
            response.put("totalCount", insights.size());
            response.put("hasNewInsights", insights.stream().anyMatch(i -> "new".equals(i.getStatus())));
            response.put("latestSuggestion", latestSuggestion);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to load insights: " + e.getMessage()));
        }
    }
    
    /**
     * Trigger new AI analysis
     * Serves: AIInsights component when user requests new analysis
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateInsight(@RequestBody CreateInsightRequestDTO request) {
        try {
            AIInsightDTO newInsight = aiInsightService.generateInsight(request);
            
            return ResponseEntity.ok(Map.of(
                "insight", newInsight,
                "message", "New AI insight generated successfully"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to generate insight: " + e.getMessage()));
        }
    }
    
    /**
     * Get full insight details for modal
     * Serves: AIInsightModal component
     */
    @GetMapping("/detailed/{id}")
    public ResponseEntity<Map<String, Object>> getInsightDetails(
            @PathVariable Long id) {
        try {
            AIInsightDTO insightDetails = aiInsightService.getInsightDetails(id);
            
            return ResponseEntity.ok(Map.of(
                "insight", insightDetails,
                "canEdit", true, // You can add logic here for edit permissions
                "relatedSuggestions", List.of() // Could add related suggestions
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to load insight details: " + e.getMessage()));
        }
    }
    
    /**
     * Generate specific exercise/diet suggestions
     * Serves: Both AIInsights and AIInsightModal when user wants specific suggestions
     */
    @PostMapping("/suggestions")
    public ResponseEntity<Map<String, Object>> generateSuggestions(@RequestBody SuggestionRequestDTO request) {
        try {
            SuggestionResponseDTO suggestions = aiInsightService.generateSuggestions(request);
            
            return ResponseEntity.ok(Map.of(
                "suggestions", suggestions,
                "timestamp", java.time.LocalDateTime.now(),
                "requestId", java.util.UUID.randomUUID().toString()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to generate suggestions: " + e.getMessage()));
        }
    }
    
    /**
     * Get suggestions by type (shorthand endpoint)
     */
    @GetMapping("/suggestions")
    public ResponseEntity<Map<String, Object>> getSuggestions(
            @RequestParam String type, // "exercise" or "diet"
            @RequestParam(required = false, defaultValue = "maintenance") String goal) {
        
        try {
            SuggestionRequestDTO request = new SuggestionRequestDTO();
            request.setSuggestionType(type);
            request.setUserGoal(goal);
            request.setTimeFrame("week");
            
            SuggestionResponseDTO suggestions = aiInsightService.generateSuggestions(request);
            
            return ResponseEntity.ok(Map.of(
                "suggestions", suggestions,
                "type", type,
                "goal", goal
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get suggestions: " + e.getMessage()));
        }
    }
    
    /**
     * Delete/deactivate insight
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteInsight(
            @PathVariable Long id) {
        try {
            aiInsightService.deactivateInsight(id);

            return ResponseEntity.ok(Map.of(
                "message", "Insight deleted successfully",
                "insightId", id
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to delete insight: " + e.getMessage()));
        }
    }

    /**
     * Delete a stored suggestion
     */
    @DeleteMapping("/suggestions/{id}")
    public ResponseEntity<Map<String, Object>> deleteSuggestion(
            @PathVariable Long id) {
        try {
            aiInsightService.deleteSuggestion(id);

            return ResponseEntity.ok(Map.of(
                "message", "Suggestion deleted successfully",
                "suggestionId", id
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to delete suggestion: " + e.getMessage()));
        }
    }
}
