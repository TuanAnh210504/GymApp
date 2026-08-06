package com.aigym.security;

import com.aigym.common.exception.TooManyRequestsException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Rate Limiting Service sử dụng Bucket4j (Token Bucket Algorithm) + Caffeine Cache.
 *
 * Thuật toán Token Bucket (chuẩn ngành):
 *  - Mỗi IP có một "xô" (bucket) chứa tối đa N token.
 *  - Mỗi request tiêu thụ 1 token.
 *  - Token được nạp lại (refill) liên tục theo tốc độ cố định.
 *  - Nếu xô hết token → request bị từ chối ngay lập tức (HTTP 429).
 *  - Ưu điểm: Không bị lỗi "boundary burst" của Fixed Window Counter.
 *
 * Caffeine Cache đóng vai trò lưu trữ Bucket theo từng Key (IP / action):
 *  - TTL: tự xóa bucket sau 2 phút không có request (tránh memory leak).
 *  - MaxSize: giới hạn tối đa 20,000 bucket đồng thời trong RAM.
 */
@Service
public class RateLimitingService {

    /**
     * Cấu hình cho Auth endpoints nhạy cảm (login, register, forgot-password...):
     *  - Tối đa 5 request / phút
     *  - Nạp lại 5 token mỗi phút (greedy refill: nạp ngay khi đủ thời gian)
     */
    private static final int AUTH_CAPACITY = 5;
    private static final Duration AUTH_REFILL_PERIOD = Duration.ofMinutes(1);

    /**
     * Cấu hình cho API endpoints bình thường (đã xác thực JWT):
     *  - Tối đa 120 request / phút
     *  - Nạp lại 120 token mỗi phút
     */
    private static final int API_CAPACITY = 120;
    private static final Duration API_REFILL_PERIOD = Duration.ofMinutes(1);

    /**
     * Cache lưu Bucket cho Auth endpoints.
     * Key: "action:ip" (ví dụ: "login:192.168.1.1")
     * TTL: 2 phút không dùng → tự xóa → tránh memory leak hoàn toàn.
     */
    private final Cache<String, Bucket> authBucketCache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(2))
            .maximumSize(20_000)
            .build();

    /**
     * Cache lưu Bucket cho API endpoints bình thường.
     * Key: IP address của client
     */
    private final Cache<String, Bucket> apiBucketCache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(2))
            .maximumSize(20_000)
            .build();

    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Kiểm tra rate limit cho Auth endpoints.
     * Ném TooManyRequestsException (HTTP 429) nếu hết token.
     *
     * @param action Key dạng "action:ip" (ví dụ: "login:192.168.1.1")
     */
    public void checkAuthRateLimit(String action) {
        Bucket bucket = authBucketCache.get(action, k -> createAuthBucket());
        if (!bucket.tryConsume(1)) {
            throw new TooManyRequestsException(
                "Bạn đã thực hiện quá nhiều yêu cầu. Vui lòng thử lại sau 1 phút."
            );
        }
    }

    /**
     * Kiểm tra rate limit cho API endpoints bình thường.
     * Ném TooManyRequestsException (HTTP 429) nếu hết token.
     *
     * @param ip Địa chỉ IP của client
     */
    public void checkApiRateLimit(String ip) {
        Bucket bucket = apiBucketCache.get(ip, k -> createApiBucket());
        if (!bucket.tryConsume(1)) {
            throw new TooManyRequestsException(
                "Bạn đang gửi quá nhiều request. Vui lòng làm chậm lại."
            );
        }
    }

    // ── Backward compatibility ────────────────────────────────────────────────
    /** @deprecated Dùng checkAuthRateLimit(action) thay thế */
    @Deprecated(forRemoval = true)
    public void checkRateLimit(String action) {
        checkAuthRateLimit(action);
    }

    // ── Tạo Bucket mới ────────────────────────────────────────────────────────

    /**
     * Tạo Bucket cho Auth endpoint với Bandwidth giới hạn 5 req/phút.
     * Dùng greedy refill: token được nạp đều đặn theo từng khoảng thời gian nhỏ,
     * không chờ đến cuối chu kỳ mới nạp lại tất cả.
     */
    private Bucket createAuthBucket() {
        Bandwidth limit = Bandwidth.classic(
            AUTH_CAPACITY,
            Refill.greedy(AUTH_CAPACITY, AUTH_REFILL_PERIOD)
        );
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Tạo Bucket cho API endpoint với Bandwidth giới hạn 120 req/phút.
     */
    private Bucket createApiBucket() {
        Bandwidth limit = Bandwidth.classic(
            API_CAPACITY,
            Refill.greedy(API_CAPACITY, API_REFILL_PERIOD)
        );
        return Bucket.builder().addLimit(limit).build();
    }
}
