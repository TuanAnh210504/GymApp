package com.aigym.security;

import com.aigym.config.AppJwtProperties;
import com.aigym.domain.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {
    public static final String REFRESH_TOKEN_TYPE = "refresh";
    public static final String ACCESS_TOKEN_TYPE = "access";
    public static final String TOKEN_TYPE_CLAIM = "tokenType";

    private final AppJwtProperties properties;
    private final SecretKey secretKey;

     public JwtService(AppJwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    // 1. Tạo Access Token (Lưu các thông tin User, Role vào token)
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(properties.accessExpiration());
        return generateAccessToken(user, now, expiresAt);
    }

    public String generateAccessToken(User user, Instant now, Instant expiresAt) {
        return Jwts.builder()
                .issuer(properties.issuer())
                .subject(user.getEmail()) // Dùng email làm subject
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .claim("roles", List.of(user.getRole().name()))
                .claim("userId", user.getId())
                .claim("fullName", user.getFullName())
                .signWith(secretKey)
                .compact();
    }

    // 2. Tạo Refresh Token
    public String generateRefreshToken(User user, String jti, Instant now, Instant expiresAt) {
        return Jwts.builder()
                .issuer(properties.issuer())
                .subject(user.getEmail())
                .id(jti) // JWT ID để quản lý blacklist nếu cần
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .claim("userId", user.getId())
                .signWith(secretKey)
                .compact();
    }

    // 3. Hàm cốt lõi để parse Token
    private Jws<Claims> parser(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(properties.issuer()) // Kiểm tra issuer cho bảo mật
                .build()
                .parseSignedClaims(token);
    }

    public Claims parseClaim(String token) {
        return parser(token).getPayload();
    }

    // 4. Lấy Username (Email) từ token
    public String extractUsername(String token) {
        return parseClaim(token).getSubject();
    }

    // 5. Lấy danh sách Roles từ token (Trả về dạng String)
    public List<String> extractRoles(String token) {
        Claims claim = parser(token).getPayload();
        Object rolesObject = claim.get("roles");
        if (rolesObject instanceof List<?> rolesList) {
            return rolesList.stream().map(String::valueOf).toList();
        } else {
            return Collections.emptyList();
        }
    }

    // 6. Kiểm tra Token Type
    public boolean isRefreshToken(String token) {
        return REFRESH_TOKEN_TYPE.equals(parseClaim(token).get(TOKEN_TYPE_CLAIM, String.class));
    }

    public boolean isAccessToken(String token) {
        return ACCESS_TOKEN_TYPE.equals(parseClaim(token).get(TOKEN_TYPE_CLAIM, String.class));
    }

    public String extractJti(String token) {
        return parseClaim(token).getId();
    }

    public Instant extractExpiration(String token) {
        Date expiration = parseClaim(token).getExpiration();
        return expiration != null ? expiration.toInstant() : null;
    }

    public String generateJti() {
        return UUID.randomUUID().toString();
    }
}
