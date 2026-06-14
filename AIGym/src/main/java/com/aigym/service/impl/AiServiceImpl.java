package com.aigym.service.impl;

import com.aigym.service.AiService;
import com.aigym.service.ContextGathererService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

    private final ContextGathererService contextGathererService;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final HttpClient httpClient = HttpClient.newBuilder().build();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public SseEmitter chatStream(String userMessage) {
        SseEmitter emitter = new SseEmitter(60000L); // 1 minute timeout

        executor.execute(() -> {
            try {
                String context = contextGathererService.gatherUserContext();

                String requestBody = buildGeminiRequest(context, userMessage);

                String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:streamGenerateContent?alt=sse&key="
                        + geminiApiKey;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                        .thenAccept(response -> {
                            if (response.statusCode() != 200) {
                                String errorBody = response.body().reduce("", (a, b) -> a + "\n" + b);
                                log.error("Gemini API Error: Status {}, Body: {}", response.statusCode(), errorBody);
                                try {
                                    var errorJson = objectMapper.createObjectNode();
                                    errorJson.put("text", "Xin lỗi, đã có lỗi kết nối với AI (" + response.statusCode() + ").");
                                    emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(errorJson)));
                                } catch (Exception e) {}
                                emitter.complete();
                                return;
                            }
                            
                            response.body().forEach(line -> {
                                if (line.startsWith("data: ")) {
                                    String jsonData = line.substring(6);
                                    if (!jsonData.trim().isEmpty() && !jsonData.equals("[DONE]")) {
                                        try {
                                            JsonNode rootNode = objectMapper.readTree(jsonData);
                                            JsonNode candidates = rootNode.path("candidates");
                                            if (candidates.isArray() && candidates.size() > 0) {
                                                JsonNode parts = candidates.get(0).path("content").path("parts");
                                                if (parts.isArray() && parts.size() > 0) {
                                                    String text = parts.get(0).path("text").asText();
                                                    if (text != null && !text.isEmpty()) {
                                                        var outputJson = objectMapper.createObjectNode();
                                                        outputJson.put("text", text);
                                                        emitter.send(SseEmitter.event()
                                                                .data(objectMapper.writeValueAsString(outputJson)));
                                                    }
                                                }
                                            }
                                        } catch (Exception e) {
                                            log.error("Error parsing Gemini stream data", e);
                                        }
                                    }
                                }
                            });
                            emitter.complete();
                        })
                        .exceptionally(ex -> {
                            log.error("Error in Gemini API call", ex);
                            emitter.completeWithError(ex);
                            return null;
                        });

            } catch (Exception e) {
                log.error("Error initiating stream", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private String buildGeminiRequest(String systemContext, String userMessage) throws Exception {
        var root = objectMapper.createObjectNode();

        var systemInstruction = root.putObject("system_instruction");
        systemInstruction.putObject("parts").put("text", systemContext);

        var contents = root.putArray("contents");
        var userContent = contents.addObject();
        userContent.put("role", "user");
        var parts = userContent.putArray("parts");
        parts.addObject().put("text", userMessage);

        return objectMapper.writeValueAsString(root);
    }
}
