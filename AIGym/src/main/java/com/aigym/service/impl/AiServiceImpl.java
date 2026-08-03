package com.aigym.service.impl;

import com.aigym.domain.entity.User;
import com.aigym.domain.mongo.ChatMessage;
import com.aigym.security.CurrentUserService;
import com.aigym.service.AiService;
import com.aigym.service.ChatHistoryService;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

    private final ContextGathererService contextGathererService;
    private final CurrentUserService currentUserService;
    private final ChatHistoryService chatHistoryService;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key}")
    private String groqApiKey;

    private final HttpClient httpClient = HttpClient.newBuilder().build();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    // Danh sách các model dự phòng của Groq Cloud (Cập nhật mới nhất)
    private static final String[] GROQ_MODELS = {
            "llama-3.3-70b-versatile",  // Model mạnh, context window 128k, phù hợp tạo lịch dài
            "openai/gpt-oss-20b",       // Dự phòng tốc độ cao
            "llama-3.1-8b-instant"      // Dự phòng cuối
    };

    @Override
    public SseEmitter chatStream(String sessionId, String userMessage) {
        SseEmitter emitter = new SseEmitter(300000L); // 5 phút timeout

        // AtomicBoolean để theo dõi trạng thái emitter an toàn giữa các luồng
        AtomicBoolean completed = new AtomicBoolean(false);

        emitter.onTimeout(() -> {
            log.warn("SseEmitter timeout cho session: {}", sessionId);
            completed.set(true);
        });

        emitter.onCompletion(() -> {
            completed.set(true);
        });

        emitter.onError(e -> {
            log.warn("SseEmitter error cho session: {} - {}", sessionId, e.getMessage());
            completed.set(true);
        });

        // Lấy thông tin user và Context ở luồng chính (tránh mất SecurityContext trong
        // luồng async)
        User currentUser = currentUserService.getCurrentUser();
        Long userId = currentUser.getId();

        // Gọi contextGathererService TRƯỚC KHI vào executor.execute() để giữ
        // SecurityContext
        String systemContextStr;
        try {
            systemContextStr = contextGathererService.gatherUserContext();
        } catch (Exception e) {
            log.error("Failed to gather system context", e);
            systemContextStr = ""; // fallback
        }

        final String systemContext = systemContextStr;

        executor.execute(() -> {
            try {
                // Lưu câu hỏi của User vào DB
                chatHistoryService.saveMessage(sessionId, userId, "USER", userMessage);

                // Lấy lịch sử chat (giới hạn 10 tin để không vượt token limit)
                List<ChatMessage> chatHistory = chatHistoryService.getContextMessages(sessionId, 10);

                // Build Request gửi cho Groq
                var messagesArray = buildGroqMessagesNode(systemContext, chatHistory, userMessage);

                // Khởi chạy vòng lặp thử các model (bắt đầu từ model đầu tiên)
                tryModel(0, messagesArray, emitter, completed, sessionId, userId);

            } catch (Exception e) {
                log.error("Error initiating stream", e);
                if (!completed.getAndSet(true)) {
                    emitter.completeWithError(e);
                }
            }
        });

        return emitter;
    }

    private void tryModel(int modelIndex, com.fasterxml.jackson.databind.node.ArrayNode messagesArray, SseEmitter emitter, AtomicBoolean completed,
            String sessionId, Long userId) {
        if (modelIndex >= GROQ_MODELS.length) {
            // Đã thử hết tất cả các model nhưng đều thất bại
            if (!completed.getAndSet(true)) {
                try {
                    var errorJson = objectMapper.createObjectNode();
                    errorJson.put("text",
                            "Xin lỗi, hiện tại tất cả các hệ thống AI đều đang quá tải. Vui lòng thử lại sau.");
                    emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(errorJson)));
                } catch (Exception e) {
                    // bỏ qua
                }
                emitter.complete();
            }
            return;
        }

        // Kiểm tra nếu emitter đã bị đóng (do timeout từ phía client) thì không tiếp
        // tục
        if (completed.get()) {
            log.warn("Emitter đã đóng, hủy kết nối AI cho session: {}", sessionId);
            return;
        }

        String modelName = GROQ_MODELS[modelIndex];
        log.info("Đang thử kết nối AI với model: {}", modelName);

        var requestRoot = objectMapper.createObjectNode();
        requestRoot.put("model", modelName);
        requestRoot.set("messages", messagesArray);
        requestRoot.put("stream", true);
        requestRoot.put("max_tokens", 4096);  // Đã giảm xuống 4096 để không vượt qua giới hạn 12000 TPM của bản Free
        requestRoot.put("temperature", 0.7);

        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(requestRoot);
        } catch(Exception e) {
            log.error("Failed to build request body", e);
            return;
        }

        String url = "https://api.groq.com/openai/v1/chat/completions";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + groqApiKey.trim())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        StringBuilder fullAiResponse = new StringBuilder();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> {
                    if (response.statusCode() != 200) {
                        // Đọc và đóng body ngay lập tức để giải phóng socket
                        String errorBody;
                        try (var bodyStream = response.body()) {
                            errorBody = bodyStream.reduce("", (a, b) -> a + "\n" + b);
                        }
                        log.warn("Model {} bị lỗi: Status {}, Body: {}", modelName, response.statusCode(), errorBody);

                        if (response.statusCode() == 503 || response.statusCode() == 429
                                || response.statusCode() == 404 || response.statusCode() >= 500) {
                            tryModel(modelIndex + 1, messagesArray, emitter, completed, sessionId, userId);
                        } else {
                            // Lỗi cú pháp hoặc lỗi xác thực (400, 401, 403), không thử lại
                            if (!completed.getAndSet(true)) {
                                try {
                                    var errorJson = objectMapper.createObjectNode();
                                    errorJson.put("text",
                                            "Xin lỗi, đã có lỗi kết nối với AI (" + response.statusCode() + ").");
                                    emitter.send(SseEmitter.event()
                                            .data(objectMapper.writeValueAsString(errorJson)));
                                } catch (Exception e) {
                                    // bỏ qua
                                }
                                emitter.complete();
                            }
                        }
                        return;
                    }

                    // try-with-resources: Java tự đóng stream sau khi xử lý xong hoặc khi có lỗi
                    try (var lines = response.body()) {
                        lines.forEach(line -> {
                            // Dừng ngay nếu client đã ngắt kết nối
                            if (completed.get()) {
                                return;
                            }
                            if (line.startsWith("data: ")) {
                                String jsonData = line.substring(6);
                                if (!jsonData.trim().isEmpty() && !jsonData.equals("[DONE]")) {
                                    try {
                                        JsonNode rootNode = objectMapper.readTree(jsonData);
                                        JsonNode choices = rootNode.path("choices");
                                        if (choices.isArray() && choices.size() > 0) {
                                            JsonNode delta = choices.get(0).path("delta");
                                            if (delta.has("content")) {
                                                String text = delta.path("content").asText();
                                                if (text != null && !text.isEmpty()) {
                                                    fullAiResponse.append(text);
                                                    // Chỉ gửi nếu emitter chưa đóng
                                                    if (!completed.get()) {
                                                        var outputJson = objectMapper.createObjectNode();
                                                        outputJson.put("text", text);
                                                        emitter.send(SseEmitter.event()
                                                                .data(objectMapper.writeValueAsString(outputJson)));
                                                    }
                                                }
                                            }
                                        }
                                    } catch (IllegalStateException ise) {
                                        // Emitter đã đóng, đánh dấu và dừng lại
                                        log.warn("Emitter đã đóng khi đang stream dữ liệu cho session: {}", sessionId);
                                        completed.set(true);
                                    } catch (Exception e) {
                                        String errorName = e.getClass().getSimpleName();
                                        if (errorName.contains("ClientAbortException")
                                                || errorName.contains("AsyncRequestNotUsableException")
                                                || e instanceof java.io.IOException) {
                                            log.warn("Client ngắt kết nối (SSE timeout/disconnect) cho session: {}",
                                                    sessionId);
                                            completed.set(true);
                                        } else {
                                            log.error("Error parsing/sending Groq stream data", e);
                                        }
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        log.error("Error reading Groq response stream for model {}", modelName, e);
                    }

                    // Chỉ lưu lịch sử vào DB nếu phản hồi thành công và có dữ liệu
                    if (fullAiResponse.length() > 0) {
                        chatHistoryService.saveMessage(sessionId, userId, "AI", fullAiResponse.toString());
                    }

                    // Hoàn thành emitter nếu chưa đóng
                    if (!completed.getAndSet(true)) {
                        emitter.complete();
                    }
                })
                .exceptionally(ex -> {
                    log.warn("Lỗi mạng khi kết nối model {}", modelName, ex);
                    tryModel(modelIndex + 1, messagesArray, emitter, completed, sessionId, userId);
                    return null;
                });
    }

    private com.fasterxml.jackson.databind.node.ArrayNode buildGroqMessagesNode(String systemContext, List<ChatMessage> chatHistory, String currentUserMessage) {
        var messages = objectMapper.createArrayNode();

        // System Instruction
        var sysMsg = messages.addObject();
        sysMsg.put("role", "system");
        sysMsg.put("content", systemContext);

        // 1. Thêm lịch sử chat vào ngữ cảnh
        for (int i = 0; i < chatHistory.size(); i++) {
            ChatMessage msg = chatHistory.get(i);
            // Bỏ qua tin nhắn hiện tại nếu nó nằm ở ĐÚNG vị trí cuối của lịch sử
            if (i == chatHistory.size() - 1
                    && msg.getSender().equals("USER")
                    && msg.getContent().equals(currentUserMessage)) {
                continue; // Sẽ thêm ở bước 2
            }

            var historyContent = messages.addObject();
            historyContent.put("role", msg.getSender().equals("AI") ? "assistant" : "user");
            historyContent.put("content", msg.getContent());
        }

        // 2. Thêm tin nhắn hiện tại
        var userContent = messages.addObject();
        userContent.put("role", "user");
        userContent.put("content", currentUserMessage);

        return messages;
    }
}
