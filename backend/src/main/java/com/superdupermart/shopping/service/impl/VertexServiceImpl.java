package com.superdupermart.shopping.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.superdupermart.shopping.entity.ChatMessage;
import com.superdupermart.shopping.service.AIService;
import com.superdupermart.shopping.service.ProductService;
import com.superdupermart.shopping.service.RagService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service("vertexService")
public class VertexServiceImpl implements AIService {

    private String apiKey;

    @Value("${vertex.ai.chat.model:gemini-2.5-flash-lite}")
    private String model;

    @Value("${vertex.ai.chat.api.url:https://aiplatform.googleapis.com/v1/publishers/google/models}")
    private String apiBaseUrl;

    private final ObjectMapper objectMapper;
    private final ProductService productService;
    private final RestTemplate restTemplate;
    private final RagService ragService;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setApiKey(@Value("${vertex.ai.api.key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    private static final String SYSTEM_PROMPT = """
            You are a helpful customer support assistant for SuperDuper Mart, an online shopping platform.

            You can help customers with:
            - Product information and recommendations
            - Order status inquiries
            - Return and refund policies
            - General shopping questions

            Be friendly, concise, and helpful. If you don't know something, say so honestly.

            Current store policies:
            - Free shipping on orders over $50
            - 30-day return policy for unused items
            - Customer service hours: 9 AM - 9 PM EST
            """;

    public VertexServiceImpl(ProductService productService, ObjectMapper objectMapper, RagService ragService) {
        this.productService = productService;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
        this.ragService = ragService;
    }

    @Override
    public String generateResponse(String userMessage, List<ChatMessage> conversationHistory) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "I apologize, but the AI service is not configured. Please contact support.";
        }

        int maxRetries = 3;
        int retryCount = 0;
        long backoffMs = 2000;

        while (retryCount <= maxRetries) {
            try {
                List<org.springframework.ai.document.Document> retrievedDocs = ragService
                        .retrieveDocuments(userMessage);
                String ragContext = formatRagContext(retrievedDocs);

                String url = apiBaseUrl + "/" + model + ":streamGenerateContent?key=" + apiKey;

                ArrayNode contents = objectMapper.createArrayNode();

                ObjectNode systemContext = objectMapper.createObjectNode();
                systemContext.put("role", "user");
                ArrayNode systemParts = objectMapper.createArrayNode();
                ObjectNode systemPart = objectMapper.createObjectNode();

                String fullSystemPrompt = "System context: " + SYSTEM_PROMPT +
                        buildContextInfo() +
                        "\n\nRelevant Product Information:\n" + ragContext +
                        "\n\nNow respond to user messages as this assistant.";

                systemPart.put("text", fullSystemPrompt);
                systemParts.add(systemPart);
                systemContext.set("parts", systemParts);
                contents.add(systemContext);

                ObjectNode modelAck = objectMapper.createObjectNode();
                modelAck.put("role", "model");
                ArrayNode modelParts = objectMapper.createArrayNode();
                ObjectNode modelPart = objectMapper.createObjectNode();
                modelPart.put("text",
                        "Understood. I have access to the store's product information. How can I help you today?");
                modelParts.add(modelPart);
                modelAck.set("parts", modelParts);
                contents.add(modelAck);

                int startIdx = Math.max(0, conversationHistory.size() - 10);
                for (int i = startIdx; i < conversationHistory.size(); i++) {
                    ChatMessage msg = conversationHistory.get(i);
                    ObjectNode historyMsg = objectMapper.createObjectNode();
                    historyMsg.put("role", msg.getRole() == ChatMessage.MessageRole.USER ? "user" : "model");
                    ArrayNode parts = objectMapper.createArrayNode();
                    ObjectNode part = objectMapper.createObjectNode();
                    part.put("text", msg.getContent());
                    parts.add(part);
                    historyMsg.set("parts", parts);
                    contents.add(historyMsg);
                }

                ObjectNode userMsg = objectMapper.createObjectNode();
                userMsg.put("role", "user");
                ArrayNode userParts = objectMapper.createArrayNode();
                ObjectNode userPart = objectMapper.createObjectNode();
                userPart.put("text", userMessage);
                userParts.add(userPart);
                userMsg.set("parts", userParts);
                contents.add(userMsg);

                ObjectNode requestBody = objectMapper.createObjectNode();
                requestBody.set("contents", contents);

                ObjectNode generationConfig = objectMapper.createObjectNode();
                generationConfig.put("temperature", 0.7);
                generationConfig.put("maxOutputTokens", 500);
                requestBody.set("generationConfig", generationConfig);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<>(
                        objectMapper.writeValueAsString(requestBody),
                        headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        entity,
                        String.class);

                return parseStreamedResponse(response.getBody());

            } catch (Exception e) {
                if (e.getMessage() != null
                        && (e.getMessage().contains("429") || e.getMessage().contains("Too Many Requests"))) {
                    retryCount++;
                    if (retryCount > maxRetries) {
                        return "I apologize, but I'm currently experiencing high traffic. Please try again in a minute.";
                    }
                    try {
                        Thread.sleep(backoffMs);
                        backoffMs *= 2;
                        continue;
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return "Request interrupted.";
                    }
                }

                System.err.println("Vertex AI Error: " + e.getMessage());
                e.printStackTrace();
                return "I apologize, but I'm having trouble processing your request. Please try again later or contact our support team. Error: "
                        + e.getMessage();
            }
        }

        return "Service unavailable.";
    }

    private String parseStreamedResponse(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            return "I apologize, but I didn't receive a response from the AI service.";
        }

        StringBuilder text = new StringBuilder();
        String[] lines = responseBody.split("\\R");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                JsonNode node = objectMapper.readTree(trimmed);
                JsonNode candidates = node.path("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode parts = candidates.get(0).path("content").path("parts");
                    if (parts.isArray()) {
                        for (JsonNode part : parts) {
                            String partText = part.path("text").asText("");
                            if (!partText.isEmpty()) {
                                text.append(partText);
                            }
                        }
                    }
                }
            } catch (Exception ignore) {
                // Skip malformed streaming fragments
            }
        }

        if (text.length() == 0) {
            return "I apologize, but I'm having trouble interpreting the AI response right now.";
        }

        return text.toString();
    }

    private String buildContextInfo() {
        StringBuilder context = new StringBuilder("\n\nCurrent store information:\n");

        try {
            int totalProducts = productService.getAllProducts(false).size();
            context.append("- Total products available: ").append(totalProducts).append("\n");
        } catch (Exception e) {
            // Ignore if service unavailable
        }

        return context.toString();
    }

    private String formatRagContext(List<org.springframework.ai.document.Document> docs) {
        if (docs == null || docs.isEmpty()) {
            return "No specific product information found.";
        }
        return docs.stream()
                .map(org.springframework.ai.document.Document::getContent)
                .reduce((a, b) -> a + "\n---\n" + b)
                .orElse("");
    }
}
