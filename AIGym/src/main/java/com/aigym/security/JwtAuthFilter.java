package com.aigym.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // 1. Kiểm tra header Authorization
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            // 2. Xác thực loại Token (Chỉ chấp nhận Access Token)
            if (!jwtService.isAccessToken(token)) {
                writeUnauthorizedResponse(response, "Invalid access token type");
                return;
            }

            // 3. Lấy dữ liệu Username và Role TRỰC TIẾP từ token (Không query DB)
            String username = jwtService.extractUsername(token);
            List<SimpleGrantedAuthority> authorities = jwtService.extractRoles(token).stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role)) // Thêm tiền tố ROLE_ theo chuẩn Spring Security
                    .toList();

            // 4. Thiết lập SecurityContext
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                );
                // (Tùy chọn) Thêm details nếu cần
                // authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JwtException | IllegalArgumentException ex) {
            // 5. Bắt lỗi token hết hạn (ExpiredJwtException) hoặc token không hợp lệ
            SecurityContextHolder.clearContext();
            writeUnauthorizedResponse(response, "Token is invalid or expired");
            return;
        }

        filterChain.doFilter(request, response);
    }

    // Hàm tiện ích: Trả về JSON khi Token lỗi/hết hạn
    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        // Giả lập cấu trúc ApiResponse để frontend dễ xử lý
        response.getWriter().write("{\"success\":false,\"message\":\"" + escapeJson(message)
                + "\",\"data\":null,\"timestamp\":null}");
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
