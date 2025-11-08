package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.profile;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.Profile.GetMetricsHistoryRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.Profile.UserMetricsRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Profile.UserMetricsResponseDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.common.PageResponse;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.User;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.UserMetrics;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.UserMetricsRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.security.SecurityUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserMetricService {

    private static final int SCALE = 2;
    private static final LocalDateTime DEFAULT_START = LocalDate.of(1970, 1, 1).atStartOfDay();

    private final UserMetricsRepository userMetricsRepository;
    private final SecurityUtil securityUtil;

    public UserMetricsResponseDto getLatestUserMetrics() {
        Long currentUserId = getCurrentUserId();

        return userMetricsRepository.findTopByUserUserIdOrderByRecordAt(currentUserId)
            .map(this::toResponseDto)
            .orElse(null);
    }

    public PageResponse<UserMetricsResponseDto> getUserMetricsHistory(GetMetricsHistoryRequestDto request) {
        GetMetricsHistoryRequestDto effectiveRequest = request != null
            ? request
            : new GetMetricsHistoryRequestDto();

        validateHistoryRequest(effectiveRequest);

        Long currentUserId = getCurrentUserId();

        int requestedPage = effectiveRequest.getPage() != null ? effectiveRequest.getPage() : 1;
        int requestedSize = effectiveRequest.getSize() != null ? effectiveRequest.getSize() : 20;
        String sortProperty = StringUtils.hasText(effectiveRequest.getSortBy())
            ? effectiveRequest.getSortBy()
            : "createdAt";
        Sort.Direction direction = parseSortDirection(effectiveRequest.getSortDirection());

        Pageable pageable = org.springframework.data.domain.PageRequest.of(
            Math.max(requestedPage - 1, 0),
            requestedSize,
            Sort.by(direction, sortProperty)
        );

        LocalDateTime start = resolveStartDate(effectiveRequest.getStartDate());
        LocalDateTime end = resolveEndDate(effectiveRequest.getEndDate());

        Page<UserMetrics> history = userMetricsRepository.findByUserUserIdAndCreatedAtBetween(
            currentUserId,
            start,
            end,
            pageable
        );

        return PageResponse.from(history, this::toResponseDto, requestedPage);
    }

    @Transactional
    public UserMetricsResponseDto createUserMetrics(UserMetricsRequestDto request) {
        validateRequest(request);

        User user = getCurrentUser();

        UserMetrics metrics = new UserMetrics();
        metrics.setUser(user);
        metrics.setHeightCm(request.getHeightCm());
        metrics.setWeightKg(request.getWeightKg());
        metrics.setAge(request.getAge());
        metrics.setGender(normalizeGender(request.getGender()));
        metrics.setUserGoal(request.getUserGoal());
        metrics.setRecordAt(request.getRecordAt() != null ? request.getRecordAt() : LocalDateTime.now());

        applyCalculatedValues(metrics);

        UserMetrics savedMetrics = userMetricsRepository.save(metrics);
        return toResponseDto(savedMetrics);
    }

    @Transactional
    public UserMetricsResponseDto updateUserMetrics(Long metricId, UserMetricsRequestDto request) {
        if (metricId == null || metricId <= 0) {
            throw new IllegalArgumentException("Metric ID must be a positive number");
        }

        UserMetrics metrics = userMetricsRepository.findById(metricId)
            .orElseThrow(() -> new IllegalArgumentException("User metrics not found for id: " + metricId));

        Long currentUserId = getCurrentUserId();
        if (!Objects.equals(metrics.getUserId(), currentUserId)) {
            throw new IllegalArgumentException("Metrics entry does not belong to authenticated user");
        }

        validateRequestForUpdate(request);

        if (request.getHeightCm() != null) {
            metrics.setHeightCm(request.getHeightCm());
        }
        if (request.getWeightKg() != null) {
            metrics.setWeightKg(request.getWeightKg());
        }
        if (request.getAge() != null) {
            metrics.setAge(request.getAge());
        }
        if (request.getGender() != null) {
            metrics.setGender(normalizeGender(request.getGender()));
        }
        if (request.getUserGoal() != null) {
            metrics.setUserGoal(request.getUserGoal());
        }
        if (request.getRecordAt() != null) {
            metrics.setRecordAt(request.getRecordAt());
        }

        applyCalculatedValues(metrics);

        UserMetrics savedMetrics = userMetricsRepository.save(metrics);
        return toResponseDto(savedMetrics);
    }

    private void validateRequest(UserMetricsRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }
        validateNumericFields(request.getHeightCm(), request.getWeightKg(), request.getAge());
    }

    private void validateRequestForUpdate(UserMetricsRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }
        validateNumericFields(request.getHeightCm(), request.getWeightKg(), request.getAge());
    }

    private void validateNumericFields(BigDecimal heightCm, BigDecimal weightKg, Integer age) {
        if (heightCm != null && heightCm.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Height must be greater than zero");
        }
        if (weightKg != null && weightKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Weight must be greater than zero");
        }
        if (age != null && age <= 0) {
            throw new IllegalArgumentException("Age must be a positive number");
        }
    }

    private void applyCalculatedValues(UserMetrics metrics) {
        metrics.setBmi(calculateBmi(metrics.getWeightKg(), metrics.getHeightCm()));
        metrics.setBmr(calculateBmr(metrics.getWeightKg(), metrics.getHeightCm(), metrics.getAge(), metrics.getGender()));
    }

    private UserMetricsResponseDto toResponseDto(UserMetrics metrics) {
        return UserMetricsResponseDto.builder()
            .metricId(metrics.getMetricId())
            .userId(metrics.getUserId())
            .heightCm(metrics.getHeightCm())
            .weightKg(metrics.getWeightKg())
            .age(metrics.getAge())
            .gender(metrics.getGender())
            .bmi(metrics.getBmi())
            .bmr(metrics.getBmr())
            .userGoal(metrics.getUserGoal())
            .recordAt(metrics.getRecordAt())
            .createdAt(metrics.getCreatedAt())
            .build();
    }

    private String normalizeGender(String gender) {
        if (gender == null) {
            return null;
        }
        String trimmed = gender.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }

    private BigDecimal calculateBmi(BigDecimal weightKg, BigDecimal heightCm) {
        if (weightKg == null || heightCm == null) {
            return null;
        }
        if (heightCm.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        BigDecimal heightMeters = heightCm.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal heightSquared = heightMeters.multiply(heightMeters);
        if (heightSquared.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        BigDecimal bmi = weightKg.divide(heightSquared, 4, RoundingMode.HALF_UP);
        return bmi.setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateBmr(BigDecimal weightKg, BigDecimal heightCm, Integer age, String gender) {
        if (weightKg == null || heightCm == null || age == null || gender == null) {
            return null;
        }

        double base = 10 * weightKg.doubleValue()
            + 6.25 * heightCm.doubleValue()
            - 5 * age;

        double adjustment = switch (gender) {
            case "MALE", "M" -> 5;
            case "FEMALE", "F" -> -161;
            default -> 0;
        };

        double bmrValue = base + adjustment;
        return BigDecimal.valueOf(bmrValue).setScale(SCALE, RoundingMode.HALF_UP);
    }

    private void validateHistoryRequest(GetMetricsHistoryRequestDto request) {
        if (request.getStartDate() != null && request.getEndDate() != null
            && request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }

    private LocalDateTime resolveStartDate(LocalDate startDate) {
        if (startDate == null) {
            return DEFAULT_START;
        }
        return startDate.atStartOfDay();
    }

    private LocalDateTime resolveEndDate(LocalDate endDate) {
        if (endDate == null) {
            return LocalDateTime.now();
        }
        return endDate.atTime(LocalTime.MAX);
    }

    private Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    private User getCurrentUser() {
        return securityUtil.getCurrentUserOrThrow();
    }

    private Sort.Direction parseSortDirection(String direction) {
        if (!StringUtils.hasText(direction)) {
            return Sort.Direction.DESC;
        }
        try {
            return Sort.Direction.valueOf(direction.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return Sort.Direction.DESC;
        }
    }
}
