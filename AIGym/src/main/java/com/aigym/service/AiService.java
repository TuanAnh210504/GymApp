package com.aigym.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AiService {
    SseEmitter chatStream(String sessionId, String userMessage);
}
