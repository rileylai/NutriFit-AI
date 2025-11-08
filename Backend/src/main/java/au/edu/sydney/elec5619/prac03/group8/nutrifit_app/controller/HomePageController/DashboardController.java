package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.controller.HomePageController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.HomePageService.AIInsightService;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.HomePageService.DashboardService;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.HomePageService.PdfExportService;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Dashboard.QuickStatsResponseDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Common.ErrorResponseDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.security.SecurityUtil;

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/homepage/dashboard")
public class DashboardController {
    
    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private AIInsightService aiInsightService;

    @Autowired
    private PdfExportService pdfExportService;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * Get quick stats data specifically
     * Serves: QuickStats component when it needs individual updates
     * Authenticated user is resolved from JWT (sub -> uuid -> userId).
     * @param period - "weekly" or "monthly" to determine the data range (defaults to "weekly")
     */
    @GetMapping("/quick-stats")
    public ResponseEntity<?> getQuickStats(
            @RequestParam(required = false, defaultValue = "weekly") String period) {
        try {
            Long userId = resolveAuthenticatedUserId();

            // Validate period parameter
            if (!period.equalsIgnoreCase("weekly") && !period.equalsIgnoreCase("monthly")) {
                ErrorResponseDTO error = new ErrorResponseDTO("INVALID_PERIOD", "Period must be either 'weekly' or 'monthly'");
                return ResponseEntity.badRequest().body(error);
            }

            QuickStatsResponseDTO quickStats = dashboardService.getQuickStats(userId, period);
            return ResponseEntity.ok(quickStats);

        } catch (IllegalStateException e) {
            ErrorResponseDTO error = new ErrorResponseDTO("UNAUTHENTICATED", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (RuntimeException e) {
            ErrorResponseDTO error = new ErrorResponseDTO("SERVICE_ERROR", "Failed to retrieve quick stats: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        } catch (Exception e) {
            ErrorResponseDTO error = new ErrorResponseDTO("UNKNOWN_ERROR", "An unexpected error occurred");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get complete dashboard data for export/reporting
     * Serves: Export report functionality
     */
    @GetMapping("/export-data-json")
    public ResponseEntity<?> getExportData(
            @RequestParam(required = false) String format,
            @RequestParam(required = false) String dateRange) {

        try {
            Long userId = resolveAuthenticatedUserId();

            Map<String, Object> exportData = new HashMap<>();

            // All comprehensive data for export
            exportData.put("userProfile", dashboardService.getUserProfileForExport(userId));
            exportData.put("nutritionSummary", dashboardService.getNutritionSummaryForExport(userId, dateRange));
            exportData.put("exerciseHistory", dashboardService.getWorkoutHistoryForExport(userId, dateRange));
            exportData.put("progressMetrics", dashboardService.getProgressMetricsForExport(userId, dateRange));
            exportData.put("aiInsights", dashboardService.getAIInsightsForExport(userId, dateRange, aiInsightService));
            exportData.put("achievements", dashboardService.getAchievementsForExport(userId));

            // Add metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("exportFormat", format != null ? format : "json");
            metadata.put("dateRange", dateRange != null ? dateRange : "30d");
            metadata.put("exportTimestamp", LocalDate.now());
            metadata.put("totalDataPoints", dashboardService.calculateTotalDataPoints(exportData));

            exportData.put("metadata", metadata);

            return ResponseEntity.ok(exportData);

        } catch (IllegalStateException e) {
            ErrorResponseDTO error = new ErrorResponseDTO("UNAUTHENTICATED", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (RuntimeException e) {
            ErrorResponseDTO error = new ErrorResponseDTO("SERVICE_ERROR", "Failed to retrieve export data: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        } catch (Exception e) {
            ErrorResponseDTO error = new ErrorResponseDTO("UNKNOWN_ERROR", "An unexpected error occurred during export");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Export dashboard data as PDF
     * Serves: Export report functionality with PDF format
     */
    @GetMapping("/export-data-pdf")
    public ResponseEntity<?> exportDataAsPdf(
            @RequestParam(required = false) String dateRange) {

        try {
            Long userId = resolveAuthenticatedUserId();

            // Generate PDF using PdfExportService
            byte[] pdfBytes = pdfExportService.generatePdfReport(userId, dateRange);

            // Set headers for PDF download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "nutrifit-dashboard-report.pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);

        } catch (IllegalArgumentException e) {
            ErrorResponseDTO error = new ErrorResponseDTO("FORBIDDEN", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (IllegalStateException e) {
            ErrorResponseDTO error = new ErrorResponseDTO("UNAUTHENTICATED", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (RuntimeException e) {
            ErrorResponseDTO error = new ErrorResponseDTO("SERVICE_ERROR", "Failed to generate PDF export: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        } catch (Exception e) {
            ErrorResponseDTO error = new ErrorResponseDTO("UNKNOWN_ERROR", "An unexpected error occurred during PDF export: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Daily progress time series for charts
     * dateRange supports: 7d, 30d, 90d, 365d, or "YYYY-MM-DD,YYYY-MM-DD". Defaults to 30d.
     */
    @GetMapping("/daily-progress")
    public ResponseEntity<?> getDailyProgress(
            @RequestParam(required = false) String dateRange
    ) {
        try {
            Long userId = resolveAuthenticatedUserId();

            var series = dashboardService.getDailyProgress(userId, dateRange);
            return ResponseEntity.ok(series);
        } catch (IllegalStateException e) {
            ErrorResponseDTO error = new ErrorResponseDTO("UNAUTHENTICATED", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (RuntimeException e) {
            ErrorResponseDTO error = new ErrorResponseDTO("SERVICE_ERROR", "Failed to retrieve daily progress: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        } catch (Exception e) {
            ErrorResponseDTO error = new ErrorResponseDTO("UNKNOWN_ERROR", "An unexpected error occurred");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    private Long resolveAuthenticatedUserId() {
        return securityUtil.getCurrentUserOrThrow().getUserId();
    }
    
}
