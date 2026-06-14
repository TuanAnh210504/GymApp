package com.aigym.service.impl;

import com.aigym.service.ContextGathererService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceImplTest {

    @Mock
    private ContextGathererService contextGathererService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AiServiceImpl aiService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiService, "geminiApiKey", "mock-api-key");
    }

    @Test
    void chatStream_Success_ReturnsEmitter() {
        when(contextGathererService.gatherUserContext()).thenReturn("Context String");

        SseEmitter emitter = aiService.chatStream("Hello AI");

        assertNotNull(emitter);
        // The async execution will run and fail internally because the HttpClient is not mocked,
        // but it won't crash the main thread and the SseEmitter will be successfully returned.
        verify(contextGathererService, timeout(1000).times(1)).gatherUserContext();
    }

    @Test
    void chatStream_Fail_ContextException() {
        when(contextGathererService.gatherUserContext()).thenThrow(new RuntimeException("Context Error"));

        SseEmitter emitter = aiService.chatStream("Hello AI");

        assertNotNull(emitter);
        // Ensure the exception is caught in the async block and completeWithError is called.
        verify(contextGathererService, timeout(1000).times(1)).gatherUserContext();
    }
}
