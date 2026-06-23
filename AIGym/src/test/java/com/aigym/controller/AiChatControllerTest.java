package com.aigym.controller;

import com.aigym.service.AiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiChatController.class)
@AutoConfigureMockMvc(addFilters = false)
class AiChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AiService aiService;

    @MockitoBean
    private com.aigym.security.JwtService jwtService;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockitoBean
    private com.aigym.security.CurrentUserService currentUserService;

    @MockitoBean
    private com.aigym.service.ChatHistoryService chatHistoryService;

    @Test
    void chat_Success() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("message", "Hello AI");

        com.aigym.domain.entity.User mockUser = new com.aigym.domain.entity.User();
        mockUser.setId(1L);
        when(currentUserService.getCurrentUser()).thenReturn(mockUser);

        com.aigym.domain.mongo.ChatSession mockSession = new com.aigym.domain.mongo.ChatSession();
        mockSession.setId("new-session-id");
        when(chatHistoryService.createSession(1L, "Hello AI", "GENERAL")).thenReturn(mockSession);

        SseEmitter mockEmitter = new SseEmitter();
        when(aiService.chatStream("new-session-id", "Hello AI")).thenReturn(mockEmitter);

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));

        verify(aiService, times(1)).chatStream("new-session-id", "Hello AI");
    }

    @Test
    void chat_Fail_MissingContentType() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("message", "Hello AI");

        mockMvc.perform(post("/api/v1/chat")
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnsupportedMediaType());

        verify(aiService, never()).chatStream(anyString(), any());
    }
}
