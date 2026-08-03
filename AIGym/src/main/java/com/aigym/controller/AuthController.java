package com.aigym.controller;

import com.aigym.common.ApiResponse;
import com.aigym.dto.authdto.AuthResponseDto;
import com.aigym.dto.authdto.LoginRequestDto;
import com.aigym.dto.authdto.RefreshTokenRequestDto;
import com.aigym.dto.authdto.RegisterRequestDto;
import com.aigym.dto.authdto.VerifyOtpRequestDto;
import com.aigym.dto.authdto.ForgotPasswordRequestDto;
import com.aigym.dto.authdto.ResetPasswordRequestDto;
import com.aigym.service.AuthService;
import com.aigym.security.RateLimitingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho Authentication.
 * Tuân thủ SRP: Không tự xử lý exception.
 * Mọi exception được GlobalExceptionHandler bắt thống nhất.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RateLimitingService rateLimitingService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequestDto request, HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        rateLimitingService.checkRateLimit("register:" + clientIp);
        authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký thành công. Vui lòng kiểm tra email để lấy mã OTP.", null));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyOtpRequestDto request, HttpServletRequest httpRequest) {
        // ── FIX: Thêm rate limit để chống brute-force OTP
        String clientIp = getClientIp(httpRequest);
        rateLimitingService.checkRateLimit("verify-email:" + clientIp);
        authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success("Xác thực email thành công. Bạn có thể đăng nhập.", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(@Valid @RequestBody LoginRequestDto request, HttpServletRequest httpRequest) {
        // ── FIX: Thêm rate limit để chống brute-force mật khẩu
        String clientIp = getClientIp(httpRequest);
        rateLimitingService.checkRateLimit("login:" + clientIp);
        AuthResponseDto response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDto>> refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        AuthResponseDto response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success("Làm mới token thành công", response));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request, HttpServletRequest httpRequest) {
        // ── FIX: Thêm rate limit để chống Email Bombing
        String clientIp = getClientIp(httpRequest);
        rateLimitingService.checkRateLimit("forgot-password:" + clientIp);
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Mã OTP đã được gửi đến email của bạn.", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request, HttpServletRequest httpRequest) {
        // ── FIX: Thêm rate limit để chống brute-force OTP reset password
        String clientIp = getClientIp(httpRequest);
        rateLimitingService.checkRateLimit("reset-password:" + clientIp);
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công. Bạn có thể đăng nhập bằng mật khẩu mới.", null));
    }

    /**
     * ── Đổi mật khẩu cho user đang đăng nhập.
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody com.aigym.dto.authdto.ChangePasswordRequestDto request) {
        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công.", null));
    }

    /**
     * ── FIX: Endpoint Logout – Thu hồi Refresh Token phía Backend.
     * Khi đăng xuất, client gửi refreshToken để server đánh dấu revoked trong DB.
     * Điều này đảm bảo token bị đánh cắp không thể dùng để lấy access token mới.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody(required = false) RefreshTokenRequestDto request) {
        String refreshToken = (request != null) ? request.getRefreshToken() : null;
        authService.logout(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công.", null));
    }

    /**
     * Lấy địa chỉ IP thực của client.
     * ── FIX: Lấy IP đầu tiên từ chuỗi X-Forwarded-For để tránh bị giả mạo bởi các IP cuối.
     * Ví dụ: "1.2.3.4, 5.6.7.8, proxy-ip" -> lấy "1.2.3.4"
     */
    private String getClientIp(HttpServletRequest request) {
        if (request == null) return "unknown";
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            // Lấy IP đầu tiên (client thực), tránh tin tưởng vào các giá trị do proxy thêm vào
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
