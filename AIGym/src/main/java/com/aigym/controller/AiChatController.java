package com.aigym.controller;

import com.aigym.domain.entity.User;
import com.aigym.domain.mongo.ChatMessage;
import com.aigym.domain.mongo.ChatSession;
import com.aigym.security.CurrentUserService;
import com.aigym.service.AiService;
import com.aigym.service.ChatHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class AiChatController {

    private final AiService aiService;
    private final ChatHistoryService chatHistoryService;
    private final CurrentUserService currentUserService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "");
        String sessionId = request.get("sessionId"); // Có thể null nếu chưa có session
        
        if (sessionId == null || sessionId.isEmpty()) {
            // Tự động tạo session mới nếu chưa truyền
            User currentUser = currentUserService.getCurrentUser();
            String title = message.length() > 20 ? message.substring(0, 20) + "..." : message;
            ChatSession newSession = chatHistoryService.createSession(currentUser.getId(), title);
            sessionId = newSession.getId();
        }

        return aiService.chatStream(sessionId, message);
    }

    // API lấy danh sách các Session của người dùng hiện tại
    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSession>> getMySessions() {
        User currentUser = currentUserService.getCurrentUser();
        return ResponseEntity.ok(chatHistoryService.getUserSessions(currentUser.getId()));
    }

    // API tạo một Session mới
    @PostMapping("/sessions")
    public ResponseEntity<ChatSession> createSession(@RequestBody Map<String, String> request) {
        User currentUser = currentUserService.getCurrentUser();
        String title = request.getOrDefault("title", "Đoạn chat mới");
        return ResponseEntity.ok(chatHistoryService.createSession(currentUser.getId(), title));
    }

    // API lấy chi tiết các tin nhắn trong một Session
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<List<ChatMessage>> getSessionMessages(@PathVariable String sessionId) {
        return ResponseEntity.ok(chatHistoryService.getSessionMessages(sessionId));
    }
    
    // API xóa một Session
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable String sessionId) {
        chatHistoryService.deleteSession(sessionId);
        return ResponseEntity.noContent().build();
    }
}
