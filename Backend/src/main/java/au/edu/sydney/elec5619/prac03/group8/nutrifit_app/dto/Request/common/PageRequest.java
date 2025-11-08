package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Request.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageRequest {

    @Min(value = 1, message = "Page number must be greater than 0")
    private Integer page = 1;

    @Min(value = 1, message = "Page size must be greater than 0")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private Integer size = 20;

    private String sortBy;

    private String sortDirection = "DESC";

    public Integer getOffset() {
        int currentPage = page != null ? page : 1;
        int pageSize = size != null ? size : 20;
        return (currentPage - 1) * pageSize;
    }
}
