package com.aigym;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * Lớp cơ sở cho tất cả Integration Tests.
 *
 * Sử dụng:
 * - Spring Boot khởi động server thật trên cổng ngẫu nhiên
 * - java.net.http.HttpClient (có sẵn Java 11+) để gửi HTTP request thật
 * - Không cần bất kỳ thư viện test đặc biệt nào ngoài spring-boot-starter-test
 * - Không đụng gì đến source code cũ
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    /**
     * Java 11 built-in HTTP client - không cần thư viện ngoài
     */
    protected final HttpClient httpClient = HttpClient.newHttpClient();
    protected final ObjectMapper objectMapper = new ObjectMapper();

    // ---------------------------------------------------------------
    // HTTP Helper Methods
    // ---------------------------------------------------------------

    protected String baseUrl() {
        return "http://localhost:" + port;
    }

    protected String url(String path) {
        return baseUrl() + path;
    }

    /**
     * Gửi POST request với JSON body
     */
    protected HttpResponse<String> post(String path, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url(path)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Gửi POST request với JSON body và Bearer token
     */
    protected HttpResponse<String> post(String path, String jsonBody, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url(path)))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Gửi GET request với Bearer token
     */
    protected HttpResponse<String> get(String path, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url(path)))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Gửi GET request không cần token
     */
    protected HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url(path)))
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Đăng nhập và lấy JWT access token.
     * 
     * @param email    email tài khoản test
     * @param password mật khẩu
     */
    @SuppressWarnings("unchecked")
    protected String login(String email, String password) throws Exception {
        String body = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
        HttpResponse<String> response = post("/api/auth/login", body);

        if (response.statusCode() != 200) {
            throw new RuntimeException("Login thất bại [" + response.statusCode() + "]: " + response.body());
        }

        Map<?, ?> parsed = objectMapper.readValue(response.body(), Map.class);
        Map<?, ?> data = (Map<?, ?>) parsed.get("data");
        return (String) data.get("accessToken");
    }

    /**
     * Parse JSON response body thành Map để dễ kiểm tra giá trị.
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> parseBody(HttpResponse<String> response) throws Exception {
        return objectMapper.readValue(response.body(), Map.class);
    }
}
