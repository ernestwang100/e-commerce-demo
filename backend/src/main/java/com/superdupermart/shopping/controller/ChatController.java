package com.superdupermart.shopping.controller;

import com.superdupermart.shopping.dao.UserDao;
import com.superdupermart.shopping.dto.ChatRequest;
import com.superdupermart.shopping.dto.ChatResponse;
import com.superdupermart.shopping.entity.User;
import com.superdupermart.shopping.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/chat")
public class ChatController {

    private final ChatService chatService;
    private final UserDao userDao;

    @Autowired
    public ChatController(ChatService chatService, UserDao userDao) {
        this.chatService = chatService;
        this.userDao = userDao;
    }

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(
            Authentication auth,
            @Valid @RequestBody ChatRequest request) {
        Integer userId = getUserId(auth);
        ChatResponse response = chatService.processMessage(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatResponse>> getHistory(@PathVariable String sessionId) {
        return ResponseEntity.ok(chatService.getConversationHistory(sessionId));
    }

    @DeleteMapping("/history/{sessionId}")
    public ResponseEntity<Map<String, String>> clearHistory(@PathVariable String sessionId) {
        chatService.clearConversation(sessionId);
        return ResponseEntity.ok(Map.of("message", "Conversation cleared"));
    }

    private Integer getUserId(Authentication auth) {
        String username = auth.getName();
        return userDao.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
