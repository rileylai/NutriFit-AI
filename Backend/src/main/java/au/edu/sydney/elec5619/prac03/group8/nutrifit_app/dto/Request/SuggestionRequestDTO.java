package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionRequestDTO {
    private Long userId;
    private String suggestionType; // "exercise", "diet"
    private String userGoal; // "weight_loss", "muscle_gain", "maintenance"
    private String timeFrame; // "today", "week", "month"
    private String preferredIntensity;
    private String experienceLevel;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> focusAreas;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> equipment;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> dietaryPreferences;

    private String weeklySchedule;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> preferredTimes;

    private String notes;
}
