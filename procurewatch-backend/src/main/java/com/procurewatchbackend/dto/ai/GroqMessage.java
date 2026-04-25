package com.procurewatchbackend.dto.ai;

public record GroqMessage(
        String role,
        String content
) {
}