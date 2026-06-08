package com.aigym.controller;

import com.aigym.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Auth API - Integration Tests")
class AuthControllerTest extends BaseIntegrationTest {

    @Test
    @DisplayName("POST /api/auth/register → 200 OK khi đăng ký tài khoản hợp lệ")
    void testRegister_Success() throws Exception {
        String uniqueEmail = "testuser" + System.currentTimeMillis() + "@aigym.com";
        String body = """
            {
              "fullName": "Test User",
              "email": "%s",
              "password": "password123"
            }
        """.formatted(uniqueEmail);

        HttpResponse<String> response = post("/api/auth/register", body);

        assertThat(response.statusCode()).isEqualTo(200);

        Map<String, Object> parsed = parseBody(response);
        assertThat(parsed.get("success")).isEqualTo(true);
    }

    @Test
    @DisplayName("POST /api/auth/register → 400 Bad Request khi email đã tồn tại")
    void testRegister_DuplicateEmail_Returns400() throws Exception {
        // Cố tình dùng admin@aigym.com (đã có sẵn trong DB)
        String body = """
            {
              "fullName": "Duplicate User",
              "email": "admin@aigym.com",
              "password": "password123"
            }
        """;

        HttpResponse<String> response = post("/api/auth/register", body);

        assertThat(response.statusCode()).isEqualTo(400);

        Map<String, Object> parsed = parseBody(response);
        assertThat(parsed.get("success")).isEqualTo(false);
    }

    @Test
    @DisplayName("POST /api/auth/login → 200 OK và trả về token khi đăng nhập đúng")
    void testLogin_Success() throws Exception {
        String body = """
            {
              "email": "admin@aigym.com",
              "password": "admin123"
            }
        """;

        HttpResponse<String> response = post("/api/auth/login", body);

        assertThat(response.statusCode()).isEqualTo(200);

        Map<String, Object> parsed = parseBody(response);
        assertThat(parsed.get("success")).isEqualTo(true);
        Map<?, ?> data = (Map<?, ?>) parsed.get("data");
        assertThat(data.get("accessToken")).isNotNull();
        assertThat(data.get("refreshToken")).isNotNull();
    }

    @Test
    @DisplayName("POST /api/auth/login → 401 Unauthorized khi sai mật khẩu")
    void testLogin_WrongPassword_Returns401() throws Exception {
        String body = """
            {
              "email": "admin@aigym.com",
              "password": "wrongpassword"
            }
        """;

        HttpResponse<String> response = post("/api/auth/login", body);

        assertThat(response.statusCode()).isEqualTo(401);
        Map<String, Object> parsed = parseBody(response);
        assertThat(parsed.get("success")).isEqualTo(false);
    }
}
