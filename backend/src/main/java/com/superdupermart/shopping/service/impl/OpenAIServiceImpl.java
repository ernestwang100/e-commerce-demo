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

// Disabled: Using GeminiServiceImpl instead
// @Service
public class OpenAIServiceImpl implements AIService {

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.model:gpt-4}")
    private String model;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    private final ObjectMapper objectMapper;
    private final ProductService productService;
    private final RestTemplate restTemplate;

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
    public OpenAIServiceImpl(ProductService productService, ObjectMapper objectMapper) {
        this.productService = productService;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String generateResponse(String userMessage, List<ChatMessage> conversationHistory) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "I apologize, but the AI service is not configured. Please contact support.";
        }

        try {
            // Build messages array
            ArrayNode messages = objectMapper.createArrayNode();
            
            // System message with context
            ObjectNode systemMsg = objectMapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("content", SYSTEM_PROMPT + buildContextInfo());
            messages.add(systemMsg);
            
            // Conversation history (limit to last 10 messages to control token usage)
            int startIdx = Math.max(0, conversationHistory.size() - 10);
            for (int i = startIdx; i < conversationHistory.size(); i++) {
                ChatMessage msg = conversationHistory.get(i);
                ObjectNode historyMsg = objectMapper.createObjectNode();
                historyMsg.put("role", msg.getRole().name().toLowerCase());
                historyMsg.put("content", msg.getContent());
                messages.add(historyMsg);
            }
            
            // Current user message
            ObjectNode userMsg = objectMapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            // Build request body
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.set("messages", messages);
            requestBody.put("max_tokens", 500);
            requestBody.put("temperature", 0.7);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> entity = new HttpEntity<>(
                objectMapper.writeValueAsString(requestBody), 
                headers
            );

            // Call OpenAI API
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                String.class
            );

            // Parse response
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            return responseJson.path("choices").get(0).path("message").path("content").asText();

        } catch (Exception e) {
            return "I apologize, but I'm having trouble processing your request. Please try again later or contact our support team.";
        }
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
