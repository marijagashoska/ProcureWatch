package com.procurewatchbackend.dto.display;

public record PageRequestDto(
        int page,
        int size,
        String sortBy,
        String sortDir
) {
    public static PageRequestDto defaultValue() {
        return new PageRequestDto(0, 10, "id", "desc");
    }
}