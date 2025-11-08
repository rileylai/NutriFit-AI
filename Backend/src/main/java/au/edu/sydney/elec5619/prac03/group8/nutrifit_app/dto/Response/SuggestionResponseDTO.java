package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionResponseDTO {
    private String suggestionType;
    private String userGoal;
    private List<String> recommendations;
    private Map<String, Object> specificMetrics;
    private String rationale;
    private Integer confidenceScore; // 1-100
}
