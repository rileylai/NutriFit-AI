package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.Profile;

import java.time.LocalDate;

import au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GetMetricsHistoryRequestDto extends PageRequest {
    private LocalDate startDate;
    private LocalDate endDate;
}
