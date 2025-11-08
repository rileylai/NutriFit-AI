package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.HomePageService;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.Profile.GetMetricsHistoryRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.Profile.UserMetricsRequestDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.Profile.UserMetricsResponseDto;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.common.PageResponse;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.User;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.model.UserMetrics;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.repository.UserMetricsRepository;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.security.SecurityUtil;
import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.profile.UserMetricService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserMetricServiceTest {

    @Mock
    private UserMetricsRepository userMetricsRepository;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private UserMetricService userMetricService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(4L);
        lenient().when(securityUtil.getCurrentUserOrThrow()).thenReturn(user);
    }

    @Test
    void getLatestUserMetrics_returnsDtoWhenMetricsExist() {
        UserMetrics metrics = new UserMetrics();
        metrics.setMetricId(11L);
        metrics.setUser(user);
        metrics.setHeightCm(BigDecimal.valueOf(180));
        metrics.setWeightKg(BigDecimal.valueOf(78));
        metrics.setAge(29);
        metrics.setGender("MALE");
        metrics.setBmi(BigDecimal.valueOf(24.1));
        metrics.setBmr(BigDecimal.valueOf(1750));
        metrics.setCreatedAt(LocalDateTime.now());

        when(userMetricsRepository.findTopByUserUserIdOrderByRecordAt(4L))
            .thenReturn(Optional.of(metrics));

        UserMetricsResponseDto dto = userMetricService.getLatestUserMetrics();

        assertEquals(11L, dto.getMetricId());
        assertEquals(BigDecimal.valueOf(78), dto.getWeightKg());
        assertEquals(BigDecimal.valueOf(24.1), dto.getBmi());
    }

    @Test
    void createUserMetrics_calculatesDerivedValues() {
        when(userMetricsRepository.save(any(UserMetrics.class))).thenAnswer(invocation -> {
            UserMetrics saved = invocation.getArgument(0);
            saved.setMetricId(20L);
            saved.setCreatedAt(LocalDateTime.now());
            return saved;
        });

        UserMetricsRequestDto request = UserMetricsRequestDto.builder()
            .heightCm(BigDecimal.valueOf(175))
            .weightKg(BigDecimal.valueOf(70))
            .age(28)
            .gender("male")
            .userGoal("fitness")
            .build();

        UserMetricsResponseDto response = userMetricService.createUserMetrics(request);

        assertEquals(20L, response.getMetricId());

        ArgumentCaptor<UserMetrics> captor = ArgumentCaptor.forClass(UserMetrics.class);
        verify(userMetricsRepository).save(captor.capture());

        UserMetrics persisted = captor.getValue();
        assertThat(persisted.getBmi()).isEqualTo(new BigDecimal("22.86"));
        assertThat(persisted.getBmr()).isEqualTo(new BigDecimal("1658.75"));
        assertEquals("MALE", persisted.getGender());
        assertEquals(user.getUserId(), persisted.getUser().getUserId());
    }

    @Test
    void updateUserMetrics_validatesOwnership() {
        UserMetrics metrics = new UserMetrics();
        User otherUser = new User();
        otherUser.setUserId(99L);
        metrics.setUser(otherUser);
        metrics.setMetricId(30L);

        when(userMetricsRepository.findById(30L)).thenReturn(Optional.of(metrics));

        assertThrows(IllegalArgumentException.class,
            () -> userMetricService.updateUserMetrics(30L, new UserMetricsRequestDto()));

        verify(userMetricsRepository, never()).save(any());
    }

    @Test
    void getUserMetricsHistory_defaultsRequestAndReturnsPage() {
        UserMetrics entry = new UserMetrics();
        entry.setMetricId(41L);
        entry.setUser(user);
        entry.setRecordAt(LocalDateTime.now().minusDays(1));

        when(userMetricsRepository.findByUserUserIdAndCreatedAtBetween(
            eq(4L), any(), any(), any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(entry),
                org.springframework.data.domain.PageRequest.of(0, 20), 1));

        PageResponse<UserMetricsResponseDto> response = userMetricService.getUserMetricsHistory(null);

        assertEquals(1, response.getCurrentPage());
        assertEquals(1, response.getData().size());
        assertEquals(Long.valueOf(1L), response.getTotalItems());
    }

    @Test
    void createUserMetrics_throwsWhenHeightInvalid() {
        UserMetricsRequestDto request = UserMetricsRequestDto.builder()
            .heightCm(BigDecimal.valueOf(-10))
            .weightKg(BigDecimal.valueOf(70))
            .age(28)
            .gender("male")
            .build();

        assertThrows(IllegalArgumentException.class, () -> userMetricService.createUserMetrics(request));
    }

    @Test
    void getUserMetricsHistory_throwsWhenDateRangeInvalid() {
        GetMetricsHistoryRequestDto request = new GetMetricsHistoryRequestDto();
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().minusDays(1));

        assertThrows(IllegalArgumentException.class, () -> userMetricService.getUserMetricsHistory(request));
    }
}
