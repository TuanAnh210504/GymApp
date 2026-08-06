package com.aigym.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Category {
    // --- 1. NGỰC (CHEST) ---
    CHEST_UPPER, // Ngực trên
    CHEST_MIDDLE, // Ngực giữa
    CHEST_LOWER, // Ngực dưới

    // --- 2. LƯNG (BACK) ---
    LATS, // Cơ xô (Latissimus Dorsi)
    RHOMBOIDS, // Cơ trám (Giữa lưng)
    TRAPS_UPPER, // Cầu vai trên
    TRAPS_MIDDLE_LOWER, // Cầu vai giữa và dưới
    LOWER_BACK, // Lưng dưới / Cơ dựng gai

    // --- 3. VAI (SHOULDERS) ---
    SHOULDERS_FRONT, // Vai trước
    SHOULDERS_SIDE, // Vai giữa / Vai ngang
    SHOULDERS_REAR, // Vai sau
    ROTATOR_CUFF, // Cơ chóp xoay (Khởi động/Phục hồi khớp vai)

    // --- 4. TAY (ARMS) ---
    BICEPS, // Tay trước (Nhị đầu)
    TRICEPS, // Tay sau (Tam đầu)
    FOREARMS, // Cẳng tay

    // --- 5. CHÂN (LEGS) ---
    QUADS, // Đùi trước (Tứ đầu đùi)
    HAMSTRINGS, // Đùi sau
    GLUTES, // Cơ mông
    CALVES, // Bắp chân
    ADDUCTORS, // Cơ khép (Đùi trong)
    ABDUCTORS, // Cơ khớp háng (Đùi ngoài)

    // --- 6. BỤNG & CORE ---
    ABS_UPPER, // Bụng trên
    ABS_LOWER, // Bụng dưới
    OBLIQUES, // Cơ bụng xiên (Liên sườn)
    SERRATUS_ANTERIOR, // Cơ răng cưa (Cạnh sườn)
    CORE, // Vùng lõi tổng hợp (Plank, Deadbug...)

    // --- 7. TỔNG HỢP & KHÁC ---
    CARDIO, // Tim mạch / Đốt mỡ
    FULL_BODY, // Toàn thân
    NECK, // Cơ cổ
    STRETCHING, // Giãn cơ
    MOBILITY // Tập vận động / Linh hoạt
}