package com.superdupermart.shopping.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Primary
@Component
public class DummyEmbeddingClient implements EmbeddingClient {

    @Override
    public List<Double> embed(String text) {
        return Collections.emptyList();
    }

    @Override
    public List<Double> embed(Document document) {
        return Collections.emptyList();
    }

    @Override
    public List<List<Double>> embed(List<String> texts) {
        return Collections.emptyList();
    }

    @Override
    public EmbeddingResponse embedForResponse(List<String> texts) {
        return new EmbeddingResponse(Collections.emptyList());
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        return new EmbeddingResponse(Collections.emptyList());
    }

    @Override
    public int dimensions() {
        return 768; // Standard for many models, specifically Gemini embedding models often use this
                    // or 1536
    }
}
