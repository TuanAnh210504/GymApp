-- Migration: Hỗ trợ bài tập AI tự chế
-- Spring Boot sẽ tự chạy file này khi khởi động (spring.sql.init.mode=always)
-- Dùng IF EXISTS / IF NOT EXISTS để đảm bảo an toàn khi chạy lại nhiều lần

-- Bước 1: Cho phép exercise_id = NULL
ALTER TABLE scheduled_exercises MODIFY COLUMN exercise_id BIGINT NULL;

-- Bước 2: Thêm cột custom_exercise_name nếu chưa có
ALTER TABLE scheduled_exercises
    ADD COLUMN IF NOT EXISTS custom_exercise_name VARCHAR(255) NULL;
