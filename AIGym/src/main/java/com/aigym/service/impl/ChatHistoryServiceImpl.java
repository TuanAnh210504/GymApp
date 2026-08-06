package com.aigym.service.impl;

import com.aigym.domain.mongo.ChatMessage;
import com.aigym.domain.mongo.ChatSession;
import com.aigym.repository.mongo.ChatMessageRepository;
import com.aigym.repository.mongo.ChatSessionRepository;
import com.aigym.service.ChatHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatHistoryServiceImpl implements ChatHistoryService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;

    // Tầng 1: Cache trong bộ nhớ
    // Key: sessionId, Value: Danh sách tin nhắn gần đây
    private final Map<String, List<ChatMessage>> activeContexts = new ConcurrentHashMap<>();

    @Override
    public ChatSession createSession(Long userId, String title, String tag) {
        ChatSession session = ChatSession.builder()
                .userId(userId)
                .title(title)
                .tag(tag != null ? tag : "GENERAL")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return sessionRepository.save(session);
    }

    @Override
    public List<ChatSession> getUserSessions(Long userId) {
        return sessionRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    @Override
    public void deleteSession(String sessionId) {
        messageRepository.deleteBySessionId(sessionId);
        sessionRepository.deleteById(sessionId);
        activeContexts.remove(sessionId);
    }

    @Override
    public List<ChatMessage> getSessionMessages(String sessionId) {
        return messageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
    }

    @Override
    public void saveMessage(String sessionId, Long userId, String sender, String content) {
        // Lưu vào Tầng 2 (MongoDB)
        ChatMessage message = ChatMessage.builder()
                .sessionId(sessionId)
                .userId(userId)
                .sender(sender)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
        messageRepository.save(message);

        // Cập nhật thời gian update của Session
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setUpdatedAt(LocalDateTime.now());
            sessionRepository.save(session);
        });

        // Nạp vào Tầng 1 (Cache)
        activeContexts.computeIfAbsent(sessionId, k -> new ArrayList<>(getSessionMessages(sessionId)))
                .add(message);
    }

    @Override
    public List<ChatMessage> getContextMessages(String sessionId, int limit) {
        // Nếu chưa có trong cache, nạp từ MongoDB lên
        List<ChatMessage> messages = activeContexts.computeIfAbsent(sessionId, k -> new ArrayList<>(getSessionMessages(sessionId)));
        
        // Trả về n tin nhắn gần nhất để làm context cho AI (tránh vượt giới hạn token)
        if (messages.size() <= limit) {
            return new ArrayList<>(messages);
        }
        return new ArrayList<>(messages.subList(messages.size() - limit, messages.size()));
    }
}
