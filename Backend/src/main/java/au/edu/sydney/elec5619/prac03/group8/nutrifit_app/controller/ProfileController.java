package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Common.ErrorResponseDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.Profile.GetMetricsHistoryRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.Profile.UserMetricsRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.common.PageResponse;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Profile.UserMetricsResponseDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.profile.UserMetricService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/homepage/profile")
public class ProfileController {

    private final UserMetricService userMetricService;

    @GetMapping
    public ResponseEntity<?> getLatestProfile() {
        try {
            UserMetricsResponseDto latestMetrics = userMetricService.getLatestUserMetrics();
            if (latestMetrics == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDTO("PROFILE_NOT_FOUND", "No profile metrics recorded for the user."));
            }
            return ResponseEntity.ok(latestMetrics);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO("INVALID_REQUEST", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ErrorResponseDTO("SERVICE_ERROR", "Failed to load profile metrics: " + e.getMessage()));
        }
    }

    @PostMapping("/history")
    public ResponseEntity<?> getMetricsHistory(@RequestBody(required = false) @Valid GetMetricsHistoryRequestDto request) {
        try {
            PageResponse<UserMetricsResponseDto> history = userMetricService.getUserMetricsHistory(request);
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO("INVALID_REQUEST", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ErrorResponseDTO("SERVICE_ERROR", "Failed to load profile history: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createUserMetrics(@RequestBody UserMetricsRequestDto request) {
        try {
            UserMetricsResponseDto savedMetrics = userMetricService.createUserMetrics(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedMetrics);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO("INVALID_REQUEST", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ErrorResponseDTO("SERVICE_ERROR", "Failed to create profile metrics: " + e.getMessage()));
        }
    }

    @PutMapping("/{metricId}")
    public ResponseEntity<?> updateUserMetrics(@PathVariable Long metricId,
                                               @RequestBody UserMetricsRequestDto request) {
        try {
            UserMetricsResponseDto updatedMetrics = userMetricService.updateUserMetrics(metricId, request);
            return ResponseEntity.ok(updatedMetrics);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO("INVALID_REQUEST", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ErrorResponseDTO("SERVICE_ERROR", "Failed to update profile metrics: " + e.getMessage()));
        }
    }
}
