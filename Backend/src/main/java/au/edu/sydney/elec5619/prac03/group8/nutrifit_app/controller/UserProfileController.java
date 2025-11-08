package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.controller;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Common.ErrorResponseDTO;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Profile.UserProfileResponseDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.Profile.UpsertUserProfileRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.UserProfile;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.security.SecurityUtil;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.profile.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints to manage the user's profile (birth date and gender).
 */
@RestController
@RequestMapping("/api/users/me/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public ResponseEntity<?> getMyProfile() {
        try {
            Long userId = securityUtil.getCurrentUserOrThrow().getUserId();
            UserProfile profile = userProfileService.getProfile(userId);
            if (profile == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponseDTO("PROFILE_NOT_FOUND", "Profile not found for the user"));
            }
            return ResponseEntity.ok(UserProfileResponseDto.fromEntity(profile));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponseDTO("SERVICE_ERROR", "Failed to load profile: " + e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<?> upsertMyProfile(@RequestBody @Valid UpsertUserProfileRequestDto request) {
        try {
            var user = securityUtil.getCurrentUserOrThrow();
            UserProfile saved = userProfileService.upsertProfile(user, request.getBirthDate(), request.getGender());
            return ResponseEntity.ok(UserProfileResponseDto.fromEntity(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseDTO("INVALID_REQUEST", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponseDTO("SERVICE_ERROR", "Failed to update profile: " + e.getMessage()));
        }
    }
}

