package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.HomePageService;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.*;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Common.AIInsightDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.User;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.security.SecurityUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class PdfExportService {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private AIInsightService aiInsightService;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * Generate PDF report for user dashboard data
     * This method uses DashboardService to gather all data and formats it as PDF
     */
    public byte[] generatePdfReport(String dateRange) throws IOException {
        return generatePdfReport(null, dateRange);
    }

    public byte[] generatePdfReport(Long userId, String dateRange) throws IOException {
        Long resolvedUserId = resolveUserId(userId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Set fonts
            PdfFont boldFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
            PdfFont normalFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

            // Add title
            Paragraph title = new Paragraph("NutriFit Dashboard Export Report")
                .setFont(boldFont)
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
            document.add(title);

            // Gather data from DashboardService
            UserProfileExportDTO userProfile = dashboardService.getUserProfileForExport(resolvedUserId);
            NutritionSummaryExportDTO nutritionSummary = dashboardService.getNutritionSummaryForExport(resolvedUserId, dateRange);
            WorkoutHistoryExportDTO workoutHistory = dashboardService.getWorkoutHistoryForExport(resolvedUserId, dateRange);
            ProgressMetricsExportDTO progressMetrics = dashboardService.getProgressMetricsForExport(resolvedUserId, dateRange);
            List<AIInsightDTO> aiInsights = dashboardService.getAIInsightsForExport(resolvedUserId, dateRange, aiInsightService);
            AchievementsExportDTO achievements = dashboardService.getAchievementsForExport(resolvedUserId);

            // Add user profile section
            addUserProfileSection(document, userProfile, normalFont, boldFont);

            // Add nutrition summary section
            addNutritionSummarySection(document, nutritionSummary, normalFont, boldFont);

            // Add workout history section
            addWorkoutHistorySection(document, workoutHistory, normalFont, boldFont);

            // Add progress metrics section
            addProgressMetricsSection(document, progressMetrics, normalFont, boldFont);

            // Add AI insights section
            if (aiInsights != null && !aiInsights.isEmpty()) {
                addAIInsightsSection(document, aiInsights, normalFont, boldFont);
            }

            // Add achievements section
            addAchievementsSection(document, achievements, normalFont, boldFont);

            document.close();

        } catch (Exception e) {
            throw new IOException("Error generating PDF: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }

    private Long resolveUserId(Long requestedUserId) {
        User currentUser = securityUtil.getCurrentUserOrThrow();
        if (requestedUserId != null && !requestedUserId.equals(currentUser.getUserId())) {
            throw new IllegalArgumentException("Authenticated user does not match request payload");
        }
        return currentUser.getUserId();
    }

    private void addUserProfileSection(Document document, UserProfileExportDTO profile, PdfFont normalFont, PdfFont boldFont) {
        Paragraph sectionTitle = new Paragraph("User Profile")
            .setFont(boldFont)
            .setFontSize(14)
            .setMarginTop(10)
            .setMarginBottom(5);
        document.add(sectionTitle);

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2}));
        table.setWidth(UnitValue.createPercentValue(100));

        addTableRow(table, "Username", profile.getUsername(), normalFont, boldFont);
        addTableRow(table, "Email", profile.getEmail(), normalFont, boldFont);
        addTableRow(table, "Weight", String.format("%.1f kg", profile.getCurrentWeight()), normalFont, boldFont);
        addTableRow(table, "Height", String.format("%.1f cm", profile.getCurrentHeight()), normalFont, boldFont);
        addTableRow(table, "BMI", String.format("%.2f", profile.getCurrentBMI()), normalFont, boldFont);
        addTableRow(table, "BMR", String.format("%.0f cal/day", profile.getCurrentBMR()), normalFont, boldFont);
        if (profile.getAge() != null) {
            addTableRow(table, "Age", String.valueOf(profile.getAge()), normalFont, boldFont);
        }
        if (profile.getGender() != null) {
            addTableRow(table, "Gender", profile.getGender(), normalFont, boldFont);
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addNutritionSummarySection(Document document, NutritionSummaryExportDTO nutrition, PdfFont normalFont, PdfFont boldFont) {
        Paragraph sectionTitle = new Paragraph("Nutrition Summary")
            .setFont(boldFont)
            .setFontSize(14)
            .setMarginTop(10)
            .setMarginBottom(5);
        document.add(sectionTitle);

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2}));
        table.setWidth(UnitValue.createPercentValue(100));

        addTableRow(table, "Period", nutrition.getDateRange(), normalFont, boldFont);
        addTableRow(table, "Total Calories", String.format("%.0f cal", nutrition.getTotalCaloriesConsumed()), normalFont, boldFont);
        addTableRow(table, "Avg Daily Calories", String.format("%.0f cal", nutrition.getAvgDailyCalories()), normalFont, boldFont);
        addTableRow(table, "Total Meals", String.valueOf(nutrition.getTotalMeals()), normalFont, boldFont);
        addTableRow(table, "Avg Meals/Day", String.format("%.1f", nutrition.getAvgMealsPerDay()), normalFont, boldFont);
        addTableRow(table, "Avg Protein", String.format("%.1f g", nutrition.getAvgDailyMacros().getProtein()), normalFont, boldFont);
        addTableRow(table, "Avg Carbs", String.format("%.1f g", nutrition.getAvgDailyMacros().getCarbs()), normalFont, boldFont);
        addTableRow(table, "Avg Fats", String.format("%.1f g", nutrition.getAvgDailyMacros().getFats()), normalFont, boldFont);
        addTableRow(table, "Calorie Target Progress", String.format("%.1f%%", nutrition.getCalorieTargetProgress()), normalFont, boldFont);
        addTableRow(table, "Goal Status", nutrition.getNutritionGoalStatus(), normalFont, boldFont);

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addWorkoutHistorySection(Document document, WorkoutHistoryExportDTO workout, PdfFont normalFont, PdfFont boldFont) {
        Paragraph sectionTitle = new Paragraph("Workout History")
            .setFont(boldFont)
            .setFontSize(14)
            .setMarginTop(10)
            .setMarginBottom(5);
        document.add(sectionTitle);

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2}));
        table.setWidth(UnitValue.createPercentValue(100));

        addTableRow(table, "Period", workout.getDateRange(), normalFont, boldFont);
        addTableRow(table, "Total Workouts", String.valueOf(workout.getTotalWorkouts()), normalFont, boldFont);
        addTableRow(table, "Total Workout Days", String.valueOf(workout.getTotalWorkoutDays()), normalFont, boldFont);
        addTableRow(table, "Total Duration", String.format("%d min", workout.getTotalDurationMinutes()), normalFont, boldFont);
        addTableRow(table, "Avg Duration", String.format("%.0f min", workout.getAvgWorkoutDuration()), normalFont, boldFont);
        addTableRow(table, "Total Calories Burned", String.format("%.0f cal", workout.getTotalCaloriesBurned()), normalFont, boldFont);
        addTableRow(table, "Avg Calories/Workout", String.format("%.0f cal", workout.getAvgCaloriesBurnedPerWorkout()), normalFont, boldFont);
        addTableRow(table, "Consistency Rating", workout.getConsistencyRating(), normalFont, boldFont);
        addTableRow(table, "Weekly Frequency", String.format("%.1f%%", workout.getWeeklyFrequencyPercentage()), normalFont, boldFont);

        document.add(table);

        // Add workout type distribution
        if (workout.getWorkoutTypeDistribution() != null && !workout.getWorkoutTypeDistribution().isEmpty()) {
            document.add(new Paragraph("Workout Type Distribution:")
                .setFont(boldFont)
                .setFontSize(12)
                .setMarginTop(10));

            Table typeTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
            typeTable.setWidth(UnitValue.createPercentValue(100));

            Cell headerCell1 = new Cell().add(new Paragraph("Type").setFont(boldFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY);
            Cell headerCell2 = new Cell().add(new Paragraph("Count").setFont(boldFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY);
            typeTable.addHeaderCell(headerCell1);
            typeTable.addHeaderCell(headerCell2);

            for (Map.Entry<String, Integer> entry : workout.getWorkoutTypeDistribution().entrySet()) {
                typeTable.addCell(new Cell().add(new Paragraph(entry.getKey()).setFont(normalFont)));
                typeTable.addCell(new Cell().add(new Paragraph(String.valueOf(entry.getValue())).setFont(normalFont)));
            }

            document.add(typeTable);
        }

        document.add(new Paragraph("\n"));
    }

    private void addProgressMetricsSection(Document document, ProgressMetricsExportDTO progress, PdfFont normalFont, PdfFont boldFont) {
        Paragraph sectionTitle = new Paragraph("Progress Metrics")
            .setFont(boldFont)
            .setFontSize(14)
            .setMarginTop(10)
            .setMarginBottom(5);
        document.add(sectionTitle);

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2}));
        table.setWidth(UnitValue.createPercentValue(100));

        addTableRow(table, "Period", progress.getDateRange(), normalFont, boldFont);
        addTableRow(table, "Overall Rating", progress.getOverallProgressRating(), normalFont, boldFont);
        addTableRow(table, "Goal Status", progress.getGoalProgressStatus(), normalFont, boldFont);

        // Weight progress
        if (progress.getWeightProgress() != null) {
            ProgressMetricsExportDTO.WeightProgressDTO weight = progress.getWeightProgress();
            addTableRow(table, "Starting Weight", String.format("%.1f kg", weight.getStartWeight()), normalFont, boldFont);
            addTableRow(table, "Current Weight", String.format("%.1f kg", weight.getCurrentWeight()), normalFont, boldFont);
            addTableRow(table, "Weight Change", String.format("%.1f kg (%s)", weight.getWeightChange(), weight.getTrend()), normalFont, boldFont);
        }

        document.add(table);

        // Add milestones
        if (progress.getMilestones() != null && !progress.getMilestones().isEmpty()) {
            document.add(new Paragraph("Milestones Achieved:")
                .setFont(boldFont)
                .setFontSize(12)
                .setMarginTop(10));

            for (String milestone : progress.getMilestones()) {
                document.add(new Paragraph("• " + milestone).setFont(normalFont).setMarginLeft(20));
            }
        }

        document.add(new Paragraph("\n"));
    }

    private void addAIInsightsSection(Document document, List<AIInsightDTO> insights, PdfFont normalFont, PdfFont boldFont) {
        Paragraph sectionTitle = new Paragraph("AI Insights")
            .setFont(boldFont)
            .setFontSize(14)
            .setMarginTop(10)
            .setMarginBottom(5);
        document.add(sectionTitle);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (int i = 0; i < Math.min(insights.size(), 10); i++) {
            AIInsightDTO insight = insights.get(i);

            Paragraph insightContent = new Paragraph(insight.getContent())
                .setFont(normalFont)
                .setFontSize(10)
                .setMarginTop(5);
            document.add(insightContent);

            Paragraph insightMeta = new Paragraph(
                String.format("Category: %s | Priority: %d | Created: %s",
                    insight.getCategory(),
                    insight.getPriority(),
                    insight.getCreatedAt().format(formatter)))
                .setFont(normalFont)
                .setFontSize(9)
                .setMarginLeft(10)
                .setItalic();
            document.add(insightMeta);
        }

        document.add(new Paragraph("\n"));
    }

    private void addAchievementsSection(Document document, AchievementsExportDTO achievements, PdfFont normalFont, PdfFont boldFont) {
        Paragraph sectionTitle = new Paragraph("Achievements")
            .setFont(boldFont)
            .setFontSize(14)
            .setMarginTop(10)
            .setMarginBottom(5);
        document.add(sectionTitle);

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2}));
        table.setWidth(UnitValue.createPercentValue(100));

        addTableRow(table, "Total Achievements", String.valueOf(achievements.getTotalAchievements()), normalFont, boldFont);
        addTableRow(table, "Total Points", String.valueOf(achievements.getTotalPoints()), normalFont, boldFont);
        addTableRow(table, "Current Level", achievements.getCurrentLevel(), normalFont, boldFont);
        addTableRow(table, "Workout Streak", String.valueOf(achievements.getCurrentStreaks().getWorkoutStreak()) + " days", normalFont, boldFont);
        addTableRow(table, "Nutrition Streak", String.valueOf(achievements.getCurrentStreaks().getNutritionStreak()) + " days", normalFont, boldFont);
        addTableRow(table, "Consistency Streak", String.valueOf(achievements.getCurrentStreaks().getConsistencyStreak()) + " days", normalFont, boldFont);

        document.add(table);

        if (achievements.getMotivationalMessage() != null) {
            document.add(new Paragraph(achievements.getMotivationalMessage())
                .setFont(boldFont)
                .setFontSize(11)
                .setItalic()
                .setMarginTop(10)
                .setTextAlignment(TextAlignment.CENTER));
        }

        document.add(new Paragraph("\n"));
    }

    private void addTableRow(Table table, String label, String value, PdfFont normalFont, PdfFont boldFont) {
        Cell labelCell = new Cell().add(new Paragraph(label).setFont(boldFont));
        Cell valueCell = new Cell().add(new Paragraph(value).setFont(normalFont));
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
