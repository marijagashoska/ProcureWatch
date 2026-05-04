package com.procurewatchbackend.dto.ai;

import java.util.List;

public record GroqChatResponse(
        List<Choice> choices
) {
    public record Choice(
            GroqMessage message
    ) {
    }
}