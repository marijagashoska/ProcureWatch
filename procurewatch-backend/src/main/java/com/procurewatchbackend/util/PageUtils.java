package com.procurewatchbackend.util;

import com.procurewatchbackend.dto.display.PagedResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageUtils {

    private PageUtils() {
    }

    public static Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(safePage, safeSize, Sort.by(direction, safeSortBy));
    }

    public static <T> PagedResponseDto<T> toPagedResponse(
            Page<T> page,
            String sortBy,
            String sortDir
    ) {
        return new PagedResponseDto<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                sortBy,
                sortDir
        );
    }
}