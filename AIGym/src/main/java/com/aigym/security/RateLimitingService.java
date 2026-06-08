package com.aigym.security;

import com.aigym.common.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class RateLimitingService {

    // Lưu trữ số lần request của một IP.
    private final Map<String, Deque<Long>> requestHistory = new ConcurrentHashMap<>();

    // Cấu hình: tối đa 3 request mỗi phút.
    private static final int MAX_REQUESTS = 3;
    private static final long TIME_WINDOW_MS = 60000;

    /**
     * Kiểm tra xem IP có vượt quá giới hạn request không.
     * @param ip Địa chỉ IP cần kiểm tra
     */
    public void checkRateLimit(String ip) {
        long currentTime = System.currentTimeMillis();

        requestHistory.putIfAbsent(ip, new ConcurrentLinkedDeque<>());
        Deque<Long> requests = requestHistory.get(ip);

        // Xóa các request cũ ngoài cửa sổ thời gian
        while (!requests.isEmpty() && currentTime - requests.peekFirst() > TIME_WINDOW_MS) {
            requests.pollFirst();
        }

        // Kiểm tra số lượng
        if (requests.size() >= MAX_REQUESTS) {
            throw new BadRequestException("Bạn đã thực hiện quá nhiều yêu cầu. Vui lòng thử lại sau 1 phút.");
        }

        // Thêm request mới
        requests.addLast(currentTime);
    }
}
