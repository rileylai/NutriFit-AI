package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Response.common;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private Integer currentPage;
    private Integer pageSize;
    private Long totalItems;
    private Integer totalPages;
    private List<T> data;
    private Boolean hasPrevious;
    private Boolean hasNext;

    public PageResponse(Integer currentPage, Integer pageSize, Long totalItems, List<T> data) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.data = data;
        this.totalPages = pageSize == null || pageSize == 0
            ? 0
            : (int) Math.ceil((double) totalItems / pageSize);
        this.hasPrevious = currentPage != null && currentPage > 1;
        this.hasNext = currentPage != null && totalPages != null && currentPage < totalPages;
    }

    public static <T> PageResponse<T> of(Integer currentPage, Integer pageSize,
                                         Long totalItems, List<T> data) {
        return new PageResponse<>(currentPage, pageSize, totalItems, data);
    }

    public static <S, T> PageResponse<T> from(Page<S> page, Function<S, T> mapper, int requestPage) {
        if (page == null) {
            return PageResponse.of(1, 0, 0L, Collections.emptyList());
        }

        List<T> content = page.getContent()
            .stream()
            .map(mapper)
            .collect(Collectors.toList());

        int currentPage = Math.max(requestPage, 1);
        int size = page.getSize();
        long total = page.getTotalElements();
        PageResponse<T> response = PageResponse.of(currentPage, size, total, content);
        response.setTotalPages(page.getTotalPages());
        response.setHasPrevious(page.hasPrevious());
        response.setHasNext(page.hasNext());
        return response;
    }
}
