package com.aigym.controller;

import com.aigym.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Test cho ExerciseController.
 *
 * Cách hoạt động:
 *   - Server Spring Boot thật được khởi động (đúng như lúc chạy thật)
 *   - java.net.http.HttpClient gửi HTTP request thật đến server
 *   - Giống y như Postman, nhưng chạy tự động và kiểm tra kết quả
 *
 * Không cần MockMvc. Không cần thư viện ngoài. Không đụng source code cũ.
 */
@DisplayName("Exercise API - Integration Tests")
class ExerciseControllerTest extends BaseIntegrationTest {

    // =====================================================================
    //  TEST CASE 1: Không có token → 401 Unauthorized
    //  Kịch bản đơn giản nhất, không cần đăng nhập
    // =====================================================================
    @Test
    @DisplayName("POST /api/exercises → 401 Unauthorized khi không có token")
    void testCreateExercise_NoToken_Returns401() throws Exception {
        // ACT: Gửi request không có Authorization header
        HttpResponse<String> response = post("/api/exercises", "{\"name\": \"Test\"}");

        // ASSERT: Server từ chối (401 Unauthorized hoặc 403 Forbidden tùy SecurityConfig)
        assertThat(response.statusCode()).isIn(401, 403);
    }

    // =====================================================================
    //  TEST CASE 2: Tạo bài tập thành công với quyền ADMIN
    //  Kịch bản: ADMIN đăng nhập → lấy token → POST tạo bài tập hợp lệ
    // =====================================================================
    @Test
    @DisplayName("POST /api/exercises → 201 Created khi ADMIN tạo bài tập hợp lệ")
    void testCreateExercise_Success() throws Exception {
        // ARRANGE: Đăng nhập lấy token (thay email/password thật của ADMIN)
        String token = login("admin@aigym.com", "admin123");

        String uniqueName = "Test Incline Barbell Press " + System.currentTimeMillis();
        String body = """
            {
              "name": "%s",
              "description": "Mô tả bài tập test",
              "primaryCategory": "CHEST_UPPER",
              "difficulty": "NORMAL",
              "isPublic": true
            }
        """.formatted(uniqueName);

        // ACT: Gọi API tạo bài tập
        HttpResponse<String> response = post("/api/exercises", body, token);

        // ASSERT: Kiểm tra kết quả
        assertThat(response.statusCode()).isEqualTo(201);

        Map<String, Object> parsed = parseBody(response);
        assertThat(parsed.get("success")).isEqualTo(true);
        assertThat(((Map<?, ?>) parsed.get("data")).get("name"))
            .isEqualTo(uniqueName);
    }

    // =====================================================================
    //  TEST CASE 3: Validation Error - thiếu tên bài tập
    //  Kịch bản: ADMIN gửi request thiếu trường "name" bắt buộc → 400
    // =====================================================================
    @Test
    @DisplayName("POST /api/exercises → 400 Bad Request khi thiếu trường 'name'")
    void testCreateExercise_MissingName_Returns400() throws Exception {
        String token = login("admin@aigym.com", "admin123");

        String body = """
            {
              "description": "Thiếu trường name",
              "primaryCategory": "CHEST_UPPER",
              "difficulty": "NORMAL"
            }
        """;

        HttpResponse<String> response = post("/api/exercises", body, token);

        assertThat(response.statusCode()).isEqualTo(400);

        Map<String, Object> parsed = parseBody(response);
        assertThat(parsed.get("success")).isEqualTo(false);
    }

    // =====================================================================
    //  TEST CASE 4: Lấy danh sách bài tập
    // =====================================================================
    @Test
    @DisplayName("GET /api/exercises → Trả về danh sách bài tập")
    void testGetAllExercises_Returns2xx() throws Exception {
        String token = login("admin@aigym.com", "admin123");

        HttpResponse<String> response = get("/api/exercises", token);

        // Kiểm tra là 2xx (200 hoặc 204)
        assertThat(response.statusCode()).isBetween(200, 299);
    }
}
