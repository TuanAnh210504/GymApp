package com.aigym.domain.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
public class ChatMessage {
    @Id
    private String id;
    
    private String sessionId; // Liên kết tới ChatSession
    
    private Long userId; // Cho mục đích truy vấn/phân quyền nhanh
    
    private String sender; // "USER" hoặc "AI"
    
    private String content; // Nội dung tin nhắn
    
    private LocalDateTime timestamp;
}
