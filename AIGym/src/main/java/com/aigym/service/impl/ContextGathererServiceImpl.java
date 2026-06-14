package com.aigym.service.impl;

import com.aigym.dto.UserProfile.UserProfileResponse;
import com.aigym.dto.WeeklySchedule.WeeklyScheduleResponse;
import com.aigym.dto.WeeklySchedule.ScheduleDayResponse;
import com.aigym.dto.WeeklySchedule.ScheduledExerciseResponse;
import com.aigym.service.ContextGathererService;
import com.aigym.service.UserProfileService;
import com.aigym.service.WeeklyScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContextGathererServiceImpl implements ContextGathererService {

    private final UserProfileService userProfileService;
    private final WeeklyScheduleService weeklyScheduleService;

    @Override
    public String gatherUserContext() {
        StringBuilder context = new StringBuilder();
        context.append("Bạn là AIGym Personal Trainer - một trợ lý AI thông minh, chuyên gia về thể hình và sức khỏe.\n");
        context.append("Dưới đây là thông tin về người dùng bạn đang tư vấn:\n\n");

        try {
            UserProfileResponse profile = userProfileService.getMyProfile();
            if (profile != null) {
                context.append("--- HỒ SƠ NGƯỜI DÙNG ---\n");
                context.append(String.format("- Tên: %s\n", profile.getUser().getFullName()));
                context.append(String.format("- Giới tính: %s\n", profile.getGender() != null ? profile.getGender() : "Chưa rõ"));
                context.append(String.format("- Chiều cao: %s cm\n", profile.getHeight() != null ? profile.getHeight() : "Chưa rõ"));
                context.append(String.format("- Cân nặng: %s kg\n", profile.getWeight() != null ? profile.getWeight() : "Chưa rõ"));
                context.append(String.format("- Mức độ vận động: %s\n", profile.getActivityLevel() != null ? profile.getActivityLevel() : "Chưa rõ"));
                context.append(String.format("- Mục tiêu: %s\n\n", profile.getGoalType() != null ? profile.getGoalType() : "Chưa rõ"));
            }
        } catch (Exception e) {
            // Ignore if no profile found
        }

        try {
            WeeklyScheduleResponse schedule = weeklyScheduleService.getMyActiveSchedule();
            if (schedule != null) {
                context.append("--- LỊCH TẬP HIỆN TẠI (TÊN LỊCH: ").append(schedule.getName()).append(") ---\n");
                if (schedule.getScheduleDays() != null && !schedule.getScheduleDays().isEmpty()) {
                    for (ScheduleDayResponse day : schedule.getScheduleDays()) {
                        context.append("- ").append(day.getDayOfWeek()).append(": ");
                        if (day.isRestDay()) {
                            context.append("Ngày nghỉ ngơi\n");
                        } else {
                            if (day.getScheduledExercises() == null || day.getScheduledExercises().isEmpty()) {
                                context.append("Chưa có bài tập\n");
                            } else {
                                String exercisesStr = day.getScheduledExercises().stream()
                                        .map(ex -> String.format("%s (%d sets x %d reps%s)", 
                                                ex.getExercise() != null ? ex.getExercise().getName() : "Bài tập", 
                                                ex.getTargetSets(), 
                                                ex.getTargetReps(),
                                                (ex.getTargetWeight() != null && ex.getTargetWeight() > 0) ? " - " + ex.getTargetWeight() + "kg" : ""))
                                        .collect(Collectors.joining(", "));
                                context.append(exercisesStr).append("\n");
                            }
                        }
                    }
                }
            } else {
                context.append("Người dùng hiện chưa áp dụng lịch tập nào.\n");
            }
        } catch (Exception e) {
            // Ignore if no schedule found
            context.append("Không thể lấy lịch tập hiện tại.\n");
        }

        context.append("\nHướng dẫn cho AI:\n");
        context.append("- Hãy trả lời ngắn gọn, súc tích, thân thiện và mang tính động viên.\n");
        context.append("- Khi người dùng sai phải sửa, không được đoán mò, có tư duy phản biện.\n");
        context.append("- Sử dụng Markdown để định dạng câu trả lời (in đậm, danh sách).\n");
        context.append("- Xưng hô bằng mình và tên của người dùng.\n");
        context.append("- Dựa vào thông tin trên để đưa ra lời khuyên phù hợp nhất. Nếu thông tin thiếu, bạn có thể hỏi thêm.\n");

        return context.toString();
    }
}
