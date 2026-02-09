package com.superdupermart.shopping.service.impl;

import com.superdupermart.shopping.dao.ChatMessageDao;
import com.superdupermart.shopping.dto.ChatRequest;
import com.superdupermart.shopping.dto.ChatResponse;
import com.superdupermart.shopping.entity.ChatMessage;
import com.superdupermart.shopping.service.AIService;
import com.superdupermart.shopping.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChatMessageDao chatMessageDao;
    private final AIService aiService;

    @Autowired
    public ChatServiceImpl(ChatMessageDao chatMessageDao, AIService aiService) {
        this.chatMessageDao = chatMessageDao;
        this.aiService = aiService;
    }

    @Override
    public ChatResponse processMessage(ChatRequest request, Integer userId) {
        // Generate session ID if not provided
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        // Save user message
        ChatMessage userMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .userId(userId)
                .role(ChatMessage.MessageRole.USER)
                .content(request.getMessage())
                .createdAt(LocalDateTime.now())
                .build();
        chatMessageDao.save(userMessage);

        // Get conversation history (excluding the message we just saved)
        List<ChatMessage> history = chatMessageDao.findBySessionIdAndUserId(sessionId, userId);
        // Remove the last message (the one we just saved) from history for AI context
        if (!history.isEmpty()) {
            history = history.subList(0, history.size() - 1);
        }

        // Generate AI response
        String aiResponseText = aiService.generateResponse(request.getMessage(), history);

        // Save AI response
        ChatMessage assistantMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .userId(userId)
                .role(ChatMessage.MessageRole.ASSISTANT)
                .content(aiResponseText)
                .createdAt(LocalDateTime.now())
                .build();
        chatMessageDao.save(assistantMessage);

        return ChatResponse.builder()
                .sessionId(sessionId)
                .message(aiResponseText)
                .role("assistant")
                .timestamp(assistantMessage.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatResponse> getConversationHistory(String sessionId, Integer userId) {
        return chatMessageDao.findBySessionIdAndUserId(sessionId, userId).stream()
                .map(msg -> ChatResponse.builder()
                        .sessionId(msg.getSessionId())
                        .message(msg.getContent())
                        .role(msg.getRole().name().toLowerCase())
                        .timestamp(msg.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void clearConversation(String sessionId) {
        chatMessageDao.deleteBySessionId(sessionId);
    }
}
