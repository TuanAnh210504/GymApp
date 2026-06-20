package com.aigym.service;

import com.aigym.domain.mongo.ChatMessage;
import com.aigym.domain.mongo.ChatSession;

import java.util.List;

public interface ChatHistoryService {
    
    // Tầng 2: Quản lý Session
    ChatSession createSession(Long userId, String title);
    List<ChatSession> getUserSessions(Long userId);
    void deleteSession(String sessionId);
    
    // Tầng 1 + Tầng 2: Lấy tin nhắn (Nạp từ MongoDB vào Cache nếu cần)
    List<ChatMessage> getSessionMessages(String sessionId);
    
    // Tầng 1 + Tầng 2: Lưu tin nhắn
    void saveMessage(String sessionId, Long userId, String sender, String content);
    
    // Lấy context (danh sách tin nhắn format dưới dạng gửi cho Gemini)
    // Trả về số lượng tin nhắn gần nhất để làm context
    List<ChatMessage> getContextMessages(String sessionId, int limit);
}
