package com.aigym.controller;

import com.aigym.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class AiChatController {

    private final AiService aiService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "");
        return aiService.chatStream(message);
    }
}
