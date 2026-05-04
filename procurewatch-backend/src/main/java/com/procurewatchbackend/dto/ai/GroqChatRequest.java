package com.procurewatchbackend.dto.ai;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GroqChatRequest(
        String model,
        List<GroqMessage> messages,
        Double temperature,
        @JsonProperty("max_completion_tokens")
        Integer maxCompletionTokens,
        @JsonProperty("response_format")
        Map<String, String> responseFormat
) {
}