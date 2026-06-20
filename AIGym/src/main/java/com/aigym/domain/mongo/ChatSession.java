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
@Document(collection = "chat_sessions")
public class ChatSession {
    @Id
    private String id;
    
    private Long userId;
    
    private String title;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
