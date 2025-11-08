package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInsightRequestDTO {
    private Long userId;
    private String analysisType; // Preferred analysis type ("nutrition", "exercise", "overall")
    private String insightType;  // Frontend alias (e.g. "fitness")
    private String focusArea;    // Alternate alias (e.g. "fitness")
    private Boolean forceRegenerate = false; // Whether to force new analysis even if recent exists

    public void setInsightType(String insightType) {
        this.insightType = insightType;
        if (!hasText(this.analysisType)) {
            this.analysisType = insightType;
        }
    }

    public void setFocusArea(String focusArea) {
        this.focusArea = focusArea;
        if (!hasText(this.analysisType)) {
            this.analysisType = focusArea;
        }
    }

    public String getResolvedAnalysisType() {
        if (hasText(analysisType)) {
            return analysisType;
        }
        if (hasText(insightType)) {
            return insightType;
        }
        if (hasText(focusArea)) {
            return focusArea;
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
