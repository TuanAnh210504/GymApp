package com.aigym.security;

import com.aigym.common.exception.TooManyRequestsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Filter áp dụng Rate Limiting toàn cục cho TẤT CẢ API endpoint.
 * Sử dụng Bucket4j (Token Bucket) thông qua RateLimitingService.
 *
 * - Chạy một lần duy nhất mỗi request (OncePerRequestFilter).
 * - Trả HTTP 429 ngay tại tầng Filter, trước khi vào Controller.
 * - Bỏ qua các đường dẫn static/health check trong whitelist.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;
    private final ObjectMapper objectMapper;

    private static final String[] WHITELIST_PATHS = {
        "/actuator/health",
        "/v3/api-docs",
        "/swagger-ui"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        for (String whitelisted : WHITELIST_PATHS) {
            if (path.startsWith(whitelisted)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        String clientIp = getClientIp(request);

        try {
            rateLimitingService.checkApiRateLimit(clientIp);
            filterChain.doFilter(request, response);
        } catch (TooManyRequestsException ex) {
            log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
            sendRateLimitResponse(response, ex.getMessage());
        }
    }

    private void sendRateLimitResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> body = Map.of("success", false, "message", message, "data", (Object) null);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
