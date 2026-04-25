package com.procurewatchbackend.service.integration;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.procurewatchbackend.dto.ai.GeneratedExplanationDto;
import com.procurewatchbackend.dto.ai.GroqChatRequest;
import com.procurewatchbackend.dto.ai.GroqChatResponse;
import com.procurewatchbackend.dto.ai.GroqMessage;

@Component
public class GroqExplanationClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    public GeneratedExplanationDto generateExplanation(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GROQ_API_KEY is missing. Set it as an environment variable.");
        }

        GroqChatRequest request = new GroqChatRequest(
                model,
                List.of(
                        new GroqMessage(
                                "system",
                                "You are a public procurement risk analyst. Return only valid JSON with keys: summaryText, explanationText, recommendationText."
                        ),
                        new GroqMessage("user", prompt)
                ),
                0.2,
                1000,
                Map.of("type", "json_object")
        );

        GroqChatResponse response = RestClient.create()
                .post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(request)
                .retrieve()
                .body(GroqChatResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("Groq returned an empty response.");
        }

        String content = response.choices().get(0).message().content();

        try {
            return objectMapper.readValue(content, GeneratedExplanationDto.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not parse Groq JSON response: " + content, ex);
        }
    }
}