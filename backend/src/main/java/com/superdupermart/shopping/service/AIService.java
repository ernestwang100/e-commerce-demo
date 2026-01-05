package com.superdupermart.shopping.service;

import com.superdupermart.shopping.entity.ChatMessage;
import java.util.List;

public interface AIService {
    String generateResponse(String userMessage, List<ChatMessage> conversationHistory);
}
