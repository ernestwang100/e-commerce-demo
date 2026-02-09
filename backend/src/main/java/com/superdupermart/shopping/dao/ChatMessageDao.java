package com.superdupermart.shopping.dao;

import com.superdupermart.shopping.entity.ChatMessage;
import java.util.List;

public interface ChatMessageDao {
    ChatMessage save(ChatMessage message);

    List<ChatMessage> findBySessionId(String sessionId);

    List<ChatMessage> findBySessionIdAndUserId(String sessionId, Integer userId);

    List<ChatMessage> findByUserId(Integer userId);

    void deleteBySessionId(String sessionId);
}
