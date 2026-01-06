package com.superdupermart.shopping.controller;

import com.superdupermart.shopping.dto.ChatRequest;
import com.superdupermart.shopping.dto.ChatResponse;
import com.superdupermart.shopping.security.SecurityUtils;
import com.superdupermart.shopping.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        Integer userId = SecurityUtils.getCurrentUserId();
        // Allow anonymous chat? Probably not if we track history by user.
        // If userId is null, maybe handle as guest or error.
        if (userId == null) {
             return ResponseEntity.badRequest().build(); 
        }
        ChatResponse response = chatService.processMessage(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatResponse>> getHistory(@PathVariable String sessionId) {
        // Validation could be added here to ensure users only see their own history
        return ResponseEntity.ok(chatService.getConversationHistory(sessionId));
    }

    @DeleteMapping("/history/{sessionId}")
    public ResponseEntity<Map<String, String>> clearHistory(@PathVariable String sessionId) {
        chatService.clearConversation(sessionId);
        return ResponseEntity.ok(Map.of("message", "Conversation cleared"));
    }
}
