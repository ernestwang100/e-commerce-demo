package com.superdupermart.shopping.service.ai;

import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GeminiEmbeddingClient implements EmbeddingClient {

    private final WebClient webClient;

    private final String apiKey;
    private final String model;
    private final String apiBaseUrl;

    public GeminiEmbeddingClient(
            @Value("${google.gemini.api.key}") String apiKey,
            @Value("${google.gemini.embedding.model:gemini-embedding-001}") String model,
            @Value("${google.gemini.embedding.api.url:https://generativelanguage.googleapis.com/v1beta/models}") String apiBaseUrl) {
        this.apiKey = apiKey;
        this.model = model;
        this.apiBaseUrl = apiBaseUrl;
        this.webClient = WebClient.builder()
                .baseUrl(apiBaseUrl)
                .build();
    }

    @Override
    public List<Double> embed(String text) {
        return callEmbeddingApi(text);
    }

    @Override
    public List<Double> embed(org.springframework.ai.document.Document document) {
        return embed(document.getContent());
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        throw new UnsupportedOperationException("Batch embedding not implemented");
    }

    @SuppressWarnings("unchecked")
    private List<Double> callEmbeddingApi(String text) {
        Map<String, Object> requestBody = Map.of(
                "model", "models/" + model,
                "content", Map.of(
                        "parts", List.of(Map.of("text", text))));

        Map<String, Object> response = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/{model}:embedContent")
                        .queryParam("key", apiKey)
                        .build(model))
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response != null && response.containsKey("embedding")) {
            Map<String, Object> embedding = (Map<String, Object>) response.get("embedding");
            return (List<Double>) embedding.get("values");
        }
        return List.of();
    }
}
