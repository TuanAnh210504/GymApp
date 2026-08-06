package com.aigym.service.impl;

import com.aigym.common.exception.BadRequestException;
import com.aigym.common.exception.NotFoundException;
import com.aigym.config.AppJwtProperties;
import com.aigym.dto.authdto.AuthResponseDto;
import com.aigym.dto.authdto.LoginRequestDto;
import com.aigym.dto.authdto.RegisterRequestDto;
import com.aigym.dto.authdto.RefreshTokenRequestDto;
import com.aigym.dto.authdto.VerifyOtpRequestDto;
import com.aigym.dto.authdto.ForgotPasswordRequestDto;
import com.aigym.dto.authdto.ResetPasswordRequestDto;
import com.aigym.domain.entity.User;
import com.aigym.domain.entity.RefreshTokenSession;
import com.aigym.domain.enums.Role;
import com.aigym.repository.UserRepository;
import com.aigym.repository.RefreshTokenSessionRepository;
import com.aigym.security.JwtService;
import com.aigym.service.AuthService;
import com.aigym.security.EmailService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppJwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Override
    @Transactional
    public void register(RegisterRequestDto request) {
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            User existingUser = userRepository.findByEmail(request.getEmail()).orElse(null);
            if (existingUser != null && existingUser.isActive()) {
                // Tài khoản đã tồn tại VÀ đã xác thực → không cho đăng ký lại
                throw new BadRequestException("Email này đã được đăng ký và xác thực. Vui lòng đăng nhập.");
            } else if (existingUser != null && !existingUser.isActive()) {
                // Tài khoản tồn tại nhưng CHƯA xác thực → cập nhật thông tin và gửi lại OTP
                String otpCode = generateOtp();
                existingUser.setFullName(request.getFullName());
                existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
                existingUser.setVerificationCode(otpCode);
                existingUser.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(5));
                existingUser.setFailedOtpAttempts(0);
                userRepository.save(existingUser);
                emailService.sendVerificationEmail(existingUser.getEmail(), otpCode);
                return;
            }
        }

        // Tạo người dùng mới
        String otpCode = generateOtp();
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .isActive(false)
                .verificationCode(otpCode)
                .verificationCodeExpiresAt(LocalDateTime.now().plusMinutes(5))
                .build();
        userRepository.save(user);

        // Gửi email
        emailService.sendVerificationEmail(user.getEmail(), otpCode);
    }

    @Override
    @Transactional
    public void verifyEmail(VerifyOtpRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản với email này"));

        if (user.isActive()) {
            throw new BadRequestException("Tài khoản đã được xác thực trước đó");
        }

        if (user.getVerificationCodeExpiresAt() != null
                && user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Mã OTP đã hết hạn");
        }

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(request.getOtp())) {
            user.setFailedOtpAttempts(user.getFailedOtpAttempts() + 1);
            if (user.getFailedOtpAttempts() >= 5) {
                user.setVerificationCode(null);
                user.setFailedOtpAttempts(0);
                userRepository.save(user);
                throw new BadRequestException(
                        "Bạn đã nhập sai mã xác nhận 5 lần. Mã xác nhận đã bị hủy, vui lòng yêu cầu mã mới.");
            }
            userRepository.save(user);
            throw new BadRequestException(
                    "Mã xác nhận không chính xác. Bạn còn " + (5 - user.getFailedOtpAttempts()) + " lần thử.");
        }

        user.setActive(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        user.setFailedOtpAttempts(0);
        userRepository.save(user);
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Override
    @Transactional
    public AuthResponseDto login(LoginRequestDto request) {
        // 1. Tìm user theo email trước
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email hoặc mật khẩu không chính xác"));

        // 2. Kiểm tra trạng thái kích hoạt TRƯỚC khi verify password
        if (!user.isActive()) {
            if (user.getVerificationCode() == null) {
                throw new BadCredentialsException("Tài khoản của bạn đã bị khóa bởi quản trị viên");
            } else {
                throw new BadCredentialsException("Vui lòng xác thực email trước khi đăng nhập");
            }
        }

        // 3. Xác thực mật khẩu qua Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        return buildTokensForUser(user);
    }

    @Override
    @Transactional
    public AuthResponseDto refresh(RefreshTokenRequestDto request) {
        String email;
        String currentJti;
        try {
            if (!jwtService.isRefreshToken(request.getRefreshToken())) {
                throw new BadCredentialsException("Invalid refresh token");
            }
            email = jwtService.extractUsername(request.getRefreshToken());
            currentJti = jwtService.extractJti(request.getRefreshToken());
            Instant refreshExpiresAt = jwtService.extractExpiration(request.getRefreshToken());
            if (refreshExpiresAt == null) {
                throw new BadCredentialsException("Invalid refresh token");
            }
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        if (currentJti == null || currentJti.isBlank()) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        RefreshTokenSession currentSession = refreshTokenSessionRepository.findByJti(currentJti)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (!currentSession.getUser().getId().equals(user.getId())) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        
        if (!user.isActive()) {
            throw new BadCredentialsException("Tài khoản của bạn đã bị khóa bởi quản trị viên.");
        }

        Instant now = Instant.now();
        if (currentSession.getRevokedAt() != null || !currentSession.getExpiresAt().isAfter(now)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        Instant accessExpiresAt = now.plusMillis(jwtProperties.accessExpiration());
        Instant nextRefreshExpiresAt = now.plusMillis(jwtProperties.refreshExpiration());

        String nextRefreshJti = jwtService.generateJti();
        String accessToken = jwtService.generateAccessToken(user, now, accessExpiresAt);
        String refreshToken = jwtService.generateRefreshToken(user, nextRefreshJti, now, nextRefreshExpiresAt);

        currentSession.setRevokedAt(now);
        currentSession.setReplacedByJti(nextRefreshJti);
        refreshTokenSessionRepository.save(currentSession);

        RefreshTokenSession nextSession = new RefreshTokenSession();
        nextSession.setJti(nextRefreshJti);
        nextSession.setUser(user);
        nextSession.setExpiresAt(nextRefreshExpiresAt);
        refreshTokenSessionRepository.save(nextSession);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .accessTokenExpiresAt(accessExpiresAt)
                .refreshTokenExpiresAt(nextRefreshExpiresAt)
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .build();
    }

    private AuthResponseDto buildTokensForUser(User user) {
        Instant now = Instant.now();
        Instant accessExpiresAt = now.plusMillis(jwtProperties.accessExpiration());
        Instant refreshExpiresAt = now.plusMillis(jwtProperties.refreshExpiration());
        String refreshJti = jwtService.generateJti();

        String accessToken = jwtService.generateAccessToken(user, now, accessExpiresAt);
        String refreshToken = jwtService.generateRefreshToken(user, refreshJti, now, refreshExpiresAt);

        RefreshTokenSession refreshTokenSession = new RefreshTokenSession();
        refreshTokenSession.setJti(refreshJti);
        refreshTokenSession.setUser(user);
        refreshTokenSession.setExpiresAt(refreshExpiresAt);
        refreshTokenSessionRepository.save(refreshTokenSession);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .accessTokenExpiresAt(accessExpiresAt)
                .refreshTokenExpiresAt(refreshExpiresAt)
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .build();
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản với email này"));

        if (!user.isActive()) {
            throw new BadRequestException("Tài khoản chưa được xác thực email. Vui lòng xác thực trước.");
        }

        String otpCode = generateOtp();
        user.setVerificationCode(otpCode);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(5));
        user.setFailedOtpAttempts(0);
        userRepository.save(user);

        emailService.sendForgotPasswordEmail(user.getEmail(), otpCode);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản với email này"));

        if (user.getVerificationCodeExpiresAt() != null
                && user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Mã OTP đã hết hạn");
        }

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(request.getOtp())) {
            user.setFailedOtpAttempts(user.getFailedOtpAttempts() + 1);
            if (user.getFailedOtpAttempts() >= 5) {
                user.setVerificationCode(null);
                user.setFailedOtpAttempts(0);
                userRepository.save(user);
                throw new BadRequestException(
                        "Bạn đã nhập sai mã xác nhận 5 lần. Mã xác nhận đã bị hủy, vui lòng yêu cầu mã mới.");
            }
            userRepository.save(user);
            throw new BadRequestException(
                    "Mã xác nhận không chính xác. Bạn còn " + (5 - user.getFailedOtpAttempts()) + " lần thử.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        user.setFailedOtpAttempts(0);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        // ── FIX: Revoke Refresh Token trong DB để vô hiệu hóa phiên đăng nhập.
        // Ngay cả khi hacker có access token, họ không thể làm mới sau khi token hết hạn.
        if (refreshToken == null || refreshToken.isBlank()) return;
        try {
            String jti = jwtService.extractJti(refreshToken);
            if (jti == null || jti.isBlank()) return;
            refreshTokenSessionRepository.findByJti(jti).ifPresent(session -> {
                if (session.getRevokedAt() == null) {
                    session.setRevokedAt(Instant.now());
                    refreshTokenSessionRepository.save(session);
                }
            });
        } catch (Exception ignored) {
            // Token không hợp lệ hoặc đã hết hạn – bỏ qua, vẫn logout thành công phía client
        }
    }

    @Override
    @Transactional
    public void changePassword(com.aigym.dto.authdto.ChangePasswordRequestDto request) {
        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new com.aigym.common.exception.NotFoundException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new com.aigym.common.exception.BadRequestException("Mật khẩu hiện tại không chính xác");
        }

        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new com.aigym.common.exception.BadRequestException("Mật khẩu mới không được trùng với mật khẩu cũ");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
