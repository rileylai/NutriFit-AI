package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.HomePageService;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.client.GeminiClient;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Common.AIInsightDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.CreateInsightRequestDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.SuggestionRequestDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.BodyMetricsDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.DailyAveragesDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.MacrosDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.QuickStatsResponseDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.StreaksDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.WorkoutFrequencyDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.SuggestionResponseDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.AIInsight;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.Suggestion;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.User;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.AIInsightRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.SuggestionRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.security.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIInsightServiceTest {

    @Mock
    private AIInsightRepository aiInsightRepository;

    @Mock
    private DashboardService dashboardService;

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private SuggestionRepository suggestionRepository;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private AIInsightService aiInsightService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(42L);
    }

    @Test
    void getLatestInsights_marksExpiredAndReturnsDtos() {
        when(securityUtil.getCurrentUserOrThrow()).thenReturn(user);

        AIInsight expired = new AIInsight();
        expired.setUser(user);
        expired.setIsActive(true);
        expired.setExpiresAt(LocalDateTime.now().minusDays(1));

        when(aiInsightRepository.findExpiredInsights(any())).thenReturn(List.of(expired));

        AIInsight insight = new AIInsight();
        insight.setInsightId(7L);
        insight.setUser(user);
        insight.setContent("Keep hydrated");
        insight.setSuggestionFormat("nutrition");
        insight.setIsActive(true);
        insight.setCreatedAt(LocalDateTime.now().minusHours(2));
        insight.setUpdatedAt(LocalDateTime.now().minusHours(1));
        insight.setExpiresAt(LocalDateTime.now().plusDays(5));

        when(aiInsightRepository.findLatestActiveInsights(42L)).thenReturn(List.of(insight));

        List<AIInsightDTO> result = aiInsightService.getLatestInsights();

        assertEquals(1, result.size());
        assertEquals("Keep hydrated", result.get(0).getContent());
        assertFalse(expired.getIsActive());
        verify(aiInsightRepository).saveAll(any());
    }

    @Test
    void generateInsight_returnsRecentWhenAvailable() {
        when(securityUtil.getCurrentUserOrThrow()).thenReturn(user);

        AIInsight recent = new AIInsight();
        recent.setInsightId(10L);
        recent.setUser(user);
        recent.setContent("Recent insight");
        recent.setSuggestionFormat("exercise");
        recent.setIsActive(true);
        recent.setCreatedAt(LocalDateTime.now());
        recent.setUpdatedAt(LocalDateTime.now());

        when(aiInsightRepository.findRecentInsights(eq(42L), any())).thenReturn(List.of(recent));

        CreateInsightRequestDTO request = new CreateInsightRequestDTO();
        request.setAnalysisType("fitness");

        AIInsightDTO dto = aiInsightService.generateInsight(request);

        assertEquals("Recent insight", dto.getContent());
        verify(aiInsightRepository, never()).save(any());
    }

    @Test
    void generateInsight_createsNewInsightWhenNoneRecent() {
        when(securityUtil.getCurrentUserOrThrow()).thenReturn(user);
        when(dashboardService.getQuickStats(42L)).thenReturn(new QuickStatsResponseDTO());
        when(geminiClient.generateAIInsight(eq("nutrition"), any())).thenReturn("Eat more greens");

        when(aiInsightRepository.save(any(AIInsight.class))).thenAnswer(invocation -> {
            AIInsight toSave = invocation.getArgument(0);
            toSave.setInsightId(55L);
            toSave.setCreatedAt(LocalDateTime.now());
            toSave.setUpdatedAt(LocalDateTime.now());
            return toSave;
        });

        CreateInsightRequestDTO request = new CreateInsightRequestDTO();
        request.setAnalysisType("nutrition");
        request.setForceRegenerate(true);

        AIInsightDTO dto = aiInsightService.generateInsight(request);

        assertEquals(55L, dto.getInsightId());
        assertEquals("nutrition", dto.getSuggestionFormat());
        verify(aiInsightRepository).save(argThat(saved ->
            saved.getUser().getUserId().equals(42L)
                && "Eat more greens".equals(saved.getContent())
                && Boolean.TRUE.equals(saved.getIsActive())
        ));
    }

    @Test
    void generateSuggestions_exercisePathPersistsSuggestion() {
        when(securityUtil.getCurrentUserOrThrow()).thenReturn(user);

        QuickStatsResponseDTO quickStats = new QuickStatsResponseDTO(
            new BodyMetricsDTO(80.0, 180.0, 24.7, 1700.0, 0.0, "stable", LocalDateTime.now()),
            new DailyAveragesDTO(2000, new MacrosDTO(120.0, 250.0, 70.0), 45, 300, 7, 14000, 315, 2100, 5),
            new DailyAveragesDTO(),
            new WorkoutFrequencyDTO(4L, 7, 57.0, Collections.singletonMap("cardio", 3L), 5),
            new StreaksDTO(3, 2, 2, "building", "building")
        );

        when(dashboardService.getQuickStats(42L)).thenReturn(quickStats);
        when(geminiClient.generateSuggestions(eq("exercise"), eq("weight_loss"), any()))
            .thenReturn(List.of("Recommendation one", "Recommendation two"));

        SuggestionRequestDTO request = new SuggestionRequestDTO();
        request.setSuggestionType("exercise");
        request.setUserGoal("weight_loss");
        request.setPreferredIntensity("moderate");

        SuggestionResponseDTO response = aiInsightService.generateSuggestions(request);

        assertEquals("exercise", response.getSuggestionType());
        assertEquals(90, response.getConfidenceScore());
        verify(suggestionRepository).save(argThat(saved ->
            saved.getUser().getUserId().equals(42L)
                && saved.getRecommendations().contains("Recommendation one")
                && Boolean.TRUE.equals(saved.getIsActive())
        ));
    }

    @Test
    void deactivateInsight_marksInsightInactiveForOwner() {
        when(securityUtil.getCurrentUserOrThrow()).thenReturn(user);

        AIInsight insight = new AIInsight();
        insight.setInsightId(3L);
        insight.setUser(user);
        insight.setIsActive(true);

        when(aiInsightRepository.findById(3L)).thenReturn(java.util.Optional.of(insight));

        aiInsightService.deactivateInsight(3L);

        verify(aiInsightRepository).save(argThat(saved -> Boolean.FALSE.equals(saved.getIsActive())));
    }

    @Test
    void deleteSuggestion_marksSuggestionInactive() {
        when(securityUtil.getCurrentUserOrThrow()).thenReturn(user);

        Suggestion suggestion = new Suggestion();
        suggestion.setSuggestionId(8L);
        suggestion.setUser(user);
        suggestion.setIsActive(true);

        when(suggestionRepository.findById(8L)).thenReturn(java.util.Optional.of(suggestion));

        aiInsightService.deleteSuggestion(8L);

        verify(suggestionRepository).save(argThat(saved -> Boolean.FALSE.equals(saved.getIsActive())));
    }

    @Test
    void getLatestSuggestionSummary_returnsStructuredData() {
        when(securityUtil.getCurrentUserOrThrow()).thenReturn(user);

        Suggestion suggestion = new Suggestion();
        suggestion.setSuggestionId(9L);
        suggestion.setUser(user);
        suggestion.setSuggestionType("diet");
        suggestion.setUserGoal("maintenance");
        suggestion.setTimeFrame("week");
        suggestion.setRecommendations("One\nTwo\n");
        suggestion.setSpecificMetrics("{\"protein\":\"120g\"}");
        suggestion.setRationale("Because data");
        suggestion.setConfidenceScore(75);
        suggestion.setRequestMetadata("{\"preferredIntensity\":\"low\"}");
        suggestion.setIsActive(true);

        when(suggestionRepository.findTopByUserUserIdAndIsActiveTrueOrderByCreatedAtDesc(42L))
            .thenReturn(java.util.Optional.of(suggestion));

        Map<String, Object> summary = aiInsightService.getLatestSuggestionSummary();

        assertThat(summary).isNotNull();
        assertThat(summary.get("suggestionId")).isEqualTo(9L);
        assertThat(summary.get("recommendations")).asList().containsExactly("One", "Two");
        assertThat(summary.get("specificMetrics")).asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.MAP)
            .containsEntry("protein", "120g");
    }

    @Test
    void deleteSuggestion_throwsWhenUnauthorized() {
        when(securityUtil.getCurrentUserOrThrow()).thenReturn(user);

        User otherUser = new User();
        otherUser.setUserId(99L);

        Suggestion suggestion = new Suggestion();
        suggestion.setSuggestionId(10L);
        suggestion.setUser(otherUser);

        when(suggestionRepository.findById(10L)).thenReturn(java.util.Optional.of(suggestion));

        assertThrows(RuntimeException.class, () -> aiInsightService.deleteSuggestion(10L));
        verify(suggestionRepository, never()).save(any());
    }

    @Test
    void getInsightDetails_returnsDtoForOwner() {
        when(securityUtil.getCurrentUserOrThrow()).thenReturn(user);

        AIInsight insight = new AIInsight();
        insight.setInsightId(12L);
        insight.setUser(user);
        insight.setContent("Detail content");
        insight.setIsActive(true);
        insight.setCreatedAt(LocalDateTime.now());
        insight.setUpdatedAt(LocalDateTime.now());

        when(aiInsightRepository.findById(12L)).thenReturn(java.util.Optional.of(insight));

        AIInsightDTO dto = aiInsightService.getInsightDetails(12L);

        assertEquals(12L, dto.getInsightId());
        assertEquals("Detail content", dto.getContent());
    }

    @Test
    void getInsightDetails_throwsForUnauthorizedAccess() {
        when(securityUtil.getCurrentUserOrThrow()).thenReturn(user);

        User other = new User();
        other.setUserId(77L);

        AIInsight insight = new AIInsight();
        insight.setInsightId(13L);
        insight.setUser(other);

        when(aiInsightRepository.findById(13L)).thenReturn(java.util.Optional.of(insight));

        assertThrows(RuntimeException.class, () -> aiInsightService.getInsightDetails(13L));
    }


    @Test
    void generateInsight_returnsFallbackWhenDashboardsFails() {
        when(securityUtil.getCurrentUserOrThrow()).thenReturn(user);
        when(aiInsightRepository.findRecentInsights(eq(42L), any())).thenReturn(Collections.emptyList());
        when(dashboardService.getQuickStats(42L)).thenThrow(new RuntimeException("dashboard down"));
        when(aiInsightRepository.save(any(AIInsight.class))).thenAnswer(invocation -> {
            AIInsight saved = invocation.getArgument(0);
            saved.setInsightId(99L);
            return saved;
        });

        CreateInsightRequestDTO request = new CreateInsightRequestDTO();
        request.setAnalysisType("custom");

        AIInsightDTO dto = aiInsightService.generateInsight(request);

        assertThat(dto.getContent()).contains("Unable to generate detailed analysis");
        assertThat(dto.getSuggestionFormat()).isEqualTo("general");
    }

    @Test
    void generateInsight_setsSuggestionFormatFromContent() {
        when(securityUtil.getCurrentUserOrThrow()).thenReturn(user);
        when(aiInsightRepository.findRecentInsights(eq(42L), any())).thenReturn(Collections.emptyList());
        when(dashboardService.getQuickStats(42L)).thenReturn(sampleQuickStats(70.0, 1600.0));
        when(geminiClient.generateAIInsight(eq("custom"), any())).thenReturn("Workout intensity should increase");
        when(aiInsightRepository.save(any(AIInsight.class))).thenAnswer(invocation -> {
            AIInsight saved = invocation.getArgument(0);
            saved.setInsightId(88L);
            return saved;
        });

        CreateInsightRequestDTO request = new CreateInsightRequestDTO();
        request.setAnalysisType("custom");

        AIInsightDTO dto = aiInsightService.generateInsight(request);

        assertThat(dto.getSuggestionFormat()).isEqualTo("exercise");
        verify(geminiClient).generateAIInsight(eq("custom"), any());
    }

    @Test
    void generateSuggestions_dietPathCalculatesMetrics() {
        when(securityUtil.getCurrentUserOrThrow()).thenReturn(user);
        when(dashboardService.getQuickStats(42L)).thenReturn(sampleQuickStats(68.0, 1550.0));
        when(geminiClient.generateSuggestions(eq("diet"), eq("weight_loss"), any()))
            .thenReturn(List.of("Eat more vegetables"));
        when(suggestionRepository.save(any(Suggestion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SuggestionRequestDTO request = new SuggestionRequestDTO();
        request.setSuggestionType("diet");
        request.setUserGoal("weight_loss");

        SuggestionResponseDTO response = aiInsightService.generateSuggestions(request);

        assertThat(response.getRecommendations()).isNotEmpty();
        assertThat(response.getSpecificMetrics()).containsKeys("targetCalories", "proteinTarget");
        verify(geminiClient).generateSuggestions(eq("diet"), eq("weight_loss"), any());
    }

    @Test
    void generateSuggestions_exerciseUsesFallbackWhenGeminiFails() {
        when(securityUtil.getCurrentUserOrThrow()).thenReturn(user);
        when(dashboardService.getQuickStats(42L)).thenReturn(sampleQuickStats(70.0, 1600.0));
        when(geminiClient.generateSuggestions(eq("exercise"), any(), any()))
            .thenThrow(new RuntimeException("api down"));
        when(suggestionRepository.save(any(Suggestion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SuggestionRequestDTO request = new SuggestionRequestDTO();
        request.setSuggestionType("exercise");
        request.setUserGoal("maintenance");

        SuggestionResponseDTO response = aiInsightService.generateSuggestions(request);

        assertThat(response.getRecommendations()).hasSize(3);
        assertThat(response.getConfidenceScore()).isEqualTo(50);
        verify(geminiClient).generateSuggestions(eq("exercise"), any(), any());
    }
    @Test
    void generateSuggestions_handlesUnsupportedType() {
        when(securityUtil.getCurrentUserOrThrow()).thenReturn(user);
        when(suggestionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SuggestionRequestDTO request = new SuggestionRequestDTO();
        request.setSuggestionType("sleep");
        request.setUserGoal("rest");

        SuggestionResponseDTO response = aiInsightService.generateSuggestions(request);

        assertEquals("sleep", response.getSuggestionType());
        assertThat(response.getRecommendations()).isEmpty();
        assertEquals(0, response.getConfidenceScore());
    }

    @Test
    void getLatestSuggestionSummary_returnsNullWhenNoData() {
        when(securityUtil.getCurrentUserOrThrow()).thenReturn(user);
        when(suggestionRepository.findTopByUserUserIdAndIsActiveTrueOrderByCreatedAtDesc(42L))
            .thenReturn(java.util.Optional.empty());

        Map<String, Object> summary = aiInsightService.getLatestSuggestionSummary();

        assertNull(summary);
    }

    private QuickStatsResponseDTO sampleQuickStats(double weight, double bmr) {
        BodyMetricsDTO bodyMetrics = new BodyMetricsDTO(weight, 170.0, 24.0, bmr, 0.5, "stable", LocalDateTime.now());
        DailyAveragesDTO averages = new DailyAveragesDTO(2000, new MacrosDTO(120.0, 250.0, 70.0),
            40, 300, 7, 14000, 280, 2100, 5);
        WorkoutFrequencyDTO frequency = new WorkoutFrequencyDTO(4L, 7, 57.0, Map.of("cardio", 3L), 5);
        StreaksDTO streaks = new StreaksDTO(3, 4, 3, "good", "great");
        return new QuickStatsResponseDTO(bodyMetrics, averages, averages, frequency, streaks);
    }

}