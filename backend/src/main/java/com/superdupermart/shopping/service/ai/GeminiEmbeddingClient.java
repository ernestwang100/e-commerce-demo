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
    
    @Value("${google.gemini.api.key}")
    private String apiKey;
    
    private final String MODEL = "text-embedding-004";

    public GeminiEmbeddingClient() {
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
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
            "model", "models/" + MODEL,
            "content", Map.of(
                "parts", List.of(Map.of("text", text))
            )
        );

        Map<String, Object> response = webClient.post()
            .uri(uriBuilder -> uriBuilder
                .path("/models/{model}:embedContent")
                .queryParam("key", apiKey)
                .build(MODEL))
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
