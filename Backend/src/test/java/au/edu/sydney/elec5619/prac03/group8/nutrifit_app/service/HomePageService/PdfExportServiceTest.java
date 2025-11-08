package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.HomePageService;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Common.AIInsightDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.AchievementsExportDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.MacrosDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.NutritionSummaryExportDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.ProgressMetricsExportDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.StreaksDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.UserProfileExportDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.WorkoutHistoryExportDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.User;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.security.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfExportServiceTest {

    @Mock
    private DashboardService dashboardService;

    @Mock
    private AIInsightService aiInsightService;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private PdfExportService pdfExportService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setUserName("alex");
        user.setEmail("alex@example.com");
        when(securityUtil.getCurrentUserOrThrow()).thenReturn(user);
    }

    @Test
    void generatePdfReport_collectsDataAndReturnsBytes() throws IOException {
        String dateRange = "2024-01-01_to_2024-01-07";

        UserProfileExportDTO profile = new UserProfileExportDTO(
            1L,
            "alex",
            "alex@example.com",
            82.0,
            178.0,
            25.8,
            1750.0,
            32,
            "MALE",
            "maintenance",
            LocalDateTime.now()
        );

        NutritionSummaryExportDTO nutrition = new NutritionSummaryExportDTO(
            "1 Jan 2024 - 7 Jan 2024",
            14000.0,
            2000.0,
            new MacrosDTO(700.0, 900.0, 400.0),
            new MacrosDTO(100.0, 128.5, 57.0),
            21,
            3.0,
            Collections.emptyList(),
            95.0,
            "on_track"
        );

        WorkoutHistoryExportDTO workoutHistory = new WorkoutHistoryExportDTO(
            "1 Jan 2024 - 7 Jan 2024",
            6,
            5,
            1800.0,
            360,
            60.0,
            300.0,
            Map.of("cardio", 4, "strength", 2),
            Collections.emptyList(),
            "consistent",
            71.4
        );

        ProgressMetricsExportDTO progress = new ProgressMetricsExportDTO(
            "1 Jan 2024 - 7 Jan 2024",
            new ProgressMetricsExportDTO.WeightProgressDTO(85.0, 82.0, -3.0, "down", 78.0, 60.0),
            new ProgressMetricsExportDTO.FitnessProgressDTO(6, 60.0, 1800.0, "solid", 15.0, 12.0),
            new ProgressMetricsExportDTO.NutritionProgressDTO(2000.0, new MacrosDTO(110.0, 200.0, 70.0), 92.0, "balanced", 5),
            List.of(new ProgressMetricsExportDTO.WeeklyProgressDTO(LocalDate.now().minusWeeks(1), -1.2, 5, 1500.0, 2100.0, "great")),
            "strong",
            List.of("Surpassed calorie goal"),
            "on_track"
        );

        List<AIInsightDTO> insights = List.of(
            new AIInsightDTO(5L, "Stay hydrated", "general", true, LocalDateTime.now().plusDays(5),
                LocalDateTime.now().minusDays(1), LocalDateTime.now().minusHours(10), "active", "wellness", 3)
        );

        AchievementsExportDTO achievements = new AchievementsExportDTO(
            List.of(new AchievementsExportDTO.AchievementDTO("A1", "10 Workouts", "Stayed active", "fitness", 200, "completed", 100.0, LocalDateTime.now().minusDays(2), "trophy")),
            List.of(new AchievementsExportDTO.AchievementDTO("A2", "Eat Clean", "Balanced diet", "nutrition", 120, "in_progress", 60.0, null, "apple")),
            new StreaksDTO(4, 3, 3, "good", "good"),
            List.of(new AchievementsExportDTO.MilestoneDTO("M1", "Weight milestone", "Dropped 3kg", "weight", 80.0, 82.0, 75.0, null, "in_progress")),
            5,
            320,
            "Silver",
            "Keep pushing!"
        );

        when(dashboardService.getUserProfileForExport(1L)).thenReturn(profile);
        when(dashboardService.getNutritionSummaryForExport(1L, dateRange)).thenReturn(nutrition);
        when(dashboardService.getWorkoutHistoryForExport(1L, dateRange)).thenReturn(workoutHistory);
        when(dashboardService.getProgressMetricsForExport(1L, dateRange)).thenReturn(progress);
        when(dashboardService.getAIInsightsForExport(1L, dateRange, aiInsightService)).thenReturn(insights);
        when(dashboardService.getAchievementsForExport(1L)).thenReturn(achievements);

        byte[] pdfBytes = pdfExportService.generatePdfReport(dateRange);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(0);

        verify(dashboardService).getUserProfileForExport(1L);
        verify(dashboardService).getNutritionSummaryForExport(1L, dateRange);
        verify(dashboardService).getWorkoutHistoryForExport(1L, dateRange);
        verify(dashboardService).getProgressMetricsForExport(1L, dateRange);
        verify(dashboardService).getAIInsightsForExport(1L, dateRange, aiInsightService);
        verify(dashboardService).getAchievementsForExport(1L);
    }
}
