package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIInsightDTO {
    private Long insightId;
    private String content;
    private String suggestionFormat;
    private Boolean isActive;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private String status; // "active", "expired", "new"
    private String category; // "nutrition", "exercise", "general"
    private Integer priority; // 1-5, 5 being highest
}
