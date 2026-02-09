package com.superdupermart.shopping.service;

import com.superdupermart.shopping.dto.ChatRequest;
import com.superdupermart.shopping.dto.ChatResponse;
import java.util.List;

public interface ChatService {
    ChatResponse processMessage(ChatRequest request, Integer userId);

    List<ChatResponse> getConversationHistory(String sessionId, Integer userId);

    void clearConversation(String sessionId);
}
