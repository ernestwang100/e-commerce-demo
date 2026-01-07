package com.superdupermart.shopping.service.impl;

import com.superdupermart.shopping.entity.ChatMessage;
import com.superdupermart.shopping.service.AIService;
import com.superdupermart.shopping.service.ProductService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class GeminiServiceImpl implements AIService {

    private String apiKey;

    @Value("${google.gemini.model:gemini-2.0-flash}")
    private String model;

    @Value("${google.gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String apiBaseUrl;

    private final ObjectMapper objectMapper;
    private final ProductService productService;
    private final RestTemplate restTemplate;
    
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setApiKey(@Value("${google.gemini.api.key:}") String apiKey) {
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

    @Autowired
    public GeminiServiceImpl(ProductService productService, ObjectMapper objectMapper) {
        this.productService = productService;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String generateResponse(String userMessage, List<ChatMessage> conversationHistory) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "I apologize, but the AI service is not configured. Please contact support.";
        }

        int maxRetries = 3;
        int retryCount = 0;
        long backoffMs = 2000; // Start with 2 seconds

        while (retryCount <= maxRetries) {
            try {
                // Build Gemini API URL
                String url = apiBaseUrl + "/" + model + ":generateContent?key=" + apiKey;

                // Build contents array for Gemini
                ArrayNode contents = objectMapper.createArrayNode();
                
                // Add system instruction as first user message context
                ObjectNode systemContext = objectMapper.createObjectNode();
                systemContext.put("role", "user");
                ArrayNode systemParts = objectMapper.createArrayNode();
                ObjectNode systemPart = objectMapper.createObjectNode();
                systemPart.put("text", "System context: " + SYSTEM_PROMPT + buildContextInfo() + "\n\nNow respond to user messages as this assistant.");
                systemParts.add(systemPart);
                systemContext.set("parts", systemParts);
                contents.add(systemContext);
                
                // Add model acknowledgment
                ObjectNode modelAck = objectMapper.createObjectNode();
                modelAck.put("role", "model");
                ArrayNode modelParts = objectMapper.createArrayNode();
                ObjectNode modelPart = objectMapper.createObjectNode();
                modelPart.put("text", "Understood. I am the SuperDuper Mart assistant. How can I help you today?");
                modelParts.add(modelPart);
                modelAck.set("parts", modelParts);
                contents.add(modelAck);
                
                // Conversation history (limit to last 10 messages)
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
                
                // Current user message
                ObjectNode userMsg = objectMapper.createObjectNode();
                userMsg.put("role", "user");
                ArrayNode userParts = objectMapper.createArrayNode();
                ObjectNode userPart = objectMapper.createObjectNode();
                userPart.put("text", userMessage);
                userParts.add(userPart);
                userMsg.set("parts", userParts);
                contents.add(userMsg);

                // Build request body
                ObjectNode requestBody = objectMapper.createObjectNode();
                requestBody.set("contents", contents);
                
                // Generation config
                ObjectNode generationConfig = objectMapper.createObjectNode();
                generationConfig.put("temperature", 0.7);
                generationConfig.put("maxOutputTokens", 500);
                requestBody.set("generationConfig", generationConfig);

                // Set headers
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> entity = new HttpEntity<>(
                    objectMapper.writeValueAsString(requestBody), 
                    headers
                );

                // Call Gemini API
                ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
                );

                // Parse response
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                return responseJson
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text").asText();

            } catch (Exception e) {
                // Check for 429 Too Many Requests
                if (e.getMessage().contains("429") || e.getMessage().contains("Too Many Requests")) {
                    retryCount++;
                    if (retryCount > maxRetries) {
                        return "I apologize, but I'm currently experiencing high traffic. Please try again in a minute.";
                    }
                    try {
                        // Wait with exponential backoff
                        Thread.sleep(backoffMs);
                        backoffMs *= 2; // Double the wait time
                        continue; // Retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return "Request interrupted.";
                    }
                }
                
                // Log full error
                System.err.println("Gemini API Error: " + e.getMessage());
                e.printStackTrace();
                return "I apologize, but I'm having trouble processing your request. Please try again later or contact our support team. Error: " + e.getMessage();
            }
        }
        return "Service unavailable.";
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
}
