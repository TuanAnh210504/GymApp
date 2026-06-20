package com.aigym.service.impl;

import com.aigym.dto.Exercise.ExerciseResponse;
import com.aigym.dto.UserProfile.UserProfileResponse;
import com.aigym.dto.WeeklySchedule.WeeklyScheduleResponse;
import com.aigym.dto.WeeklySchedule.ScheduleDayResponse;
import com.aigym.dto.WeeklySchedule.ScheduledExerciseResponse;
import com.aigym.service.ContextGathererService;
import com.aigym.service.ExerciseService;
import com.aigym.service.UserProfileService;
import com.aigym.service.WeeklyScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContextGathererServiceImpl implements ContextGathererService {

    private final UserProfileService userProfileService;
    private final WeeklyScheduleService weeklyScheduleService;
    private final ExerciseService exerciseService;

    @Override
    public String gatherUserContext() {
        StringBuilder context = new StringBuilder();
        context.append(
                "Bạn là AIGym Personal Trainer - một trợ lý AI thông minh, chuyên gia về thể hình và sức khỏe.\n");
        context.append("Dưới đây là thông tin về người dùng bạn đang tư vấn:\n\n");

        try {
            UserProfileResponse profile = userProfileService.getMyProfile();
            if (profile != null) {
                context.append("--- HỒ SƠ NGƯỜI DÙNG ---\n");
                context.append(String.format("- Tên: %s\n", profile.getUser().getFullName()));
                context.append(String.format("- Giới tính: %s\n",
                        profile.getGender() != null ? profile.getGender() : "Chưa rõ"));
                context.append(String.format("- Chiều cao: %s cm\n",
                        profile.getHeight() != null ? profile.getHeight() : "Chưa rõ"));
                context.append(String.format("- Cân nặng: %s kg\n",
                        profile.getWeight() != null ? profile.getWeight() : "Chưa rõ"));
                context.append(String.format("- Mức độ vận động: %s\n",
                        profile.getActivityLevel() != null ? profile.getActivityLevel() : "Chưa rõ"));
                context.append(String.format("- Mục tiêu: %s\n\n",
                        profile.getGoalType() != null ? profile.getGoalType() : "Chưa rõ"));
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
                                                (ex.getTargetWeight() != null && ex.getTargetWeight() > 0)
                                                        ? " - " + ex.getTargetWeight() + "kg"
                                                        : ""))
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

        try {
            List<ExerciseResponse> exercises = exerciseService.getAllExercises();
            if (exercises != null && !exercises.isEmpty()) {
                context.append("\n--- DANH SÁCH BÀI TẬP CÓ SẴN TRONG HỆ THỐNG (chỉ để tham khảo) ---\n");
                String exerciseNames = exercises.stream()
                        .map(ExerciseResponse::getName)
                        .collect(Collectors.joining(", "));
                context.append("Danh sách: ").append(exerciseNames).append("\n\n");
            }
        } catch (Exception e) {
            // Ignore
        }

        context.append("\nHướng dẫn cho AI:\n");
        context.append("- Hãy trả lời ngắn gọn, súc tích, thân thiện và mang tính động viên.\n");
        context.append("- Khi người dùng sai phải sửa, không được đoán mò, có tư duy phản biện.\n");
        context.append("- Sử dụng Markdown để định dạng câu trả lời (in đậm, danh sách).\n");
        context.append("- Xưng hô bằng mình và tên của người dùng.\n");
        context.append(
                "- Dựa vào thông tin trên để đưa ra lời khuyên phù hợp nhất. Nếu thông tin thiếu, bạn có thể hỏi thêm.\n");
        context.append(
                "- Nếu người dùng yêu cầu tạo lịch tập, hãy cung cấp lời khuyên ngắn gọn và BẮT BUỘC kèm theo một khối JSON nằm trong thẻ ```json và ```.\n");
        context.append(
                "- Lựa chọn bài tập: Ưu tiên chọn tên từ [DANH SÁCH BÀI TẬP CÓ SẴN TRONG HỆ THỐNG] nếu phù hợp. Nếu không có bài phù hợp, được phép sáng tạo tên bài tập mới (tiếng Anh, chuẩn gym) - hệ thống sẽ tự xử lý lưu trữ.\n");
        context.append("- Cấu trúc JSON Lịch tập bắt buộc phải tuân theo định dạng sau:\n");
        context.append("```json\n");
        context.append("{\n");
        context.append("  \"type\": \"workout_plan\",\n");
        context.append("  \"data\": {\n");
        context.append("    \"name\": \"[TÊN_LỊCH_TẬP_PHÙ_HỢP_VỚI_MỤC_TIÊU]\",\n");
        context.append("    \"description\": \"[MÔ_TẢ_NGẮN_VỀ_MỤC_TIÊU_VÀ_CƯỜNG_ĐỘ]\",\n");
        context.append("    \"days\": [\n");
        context.append("      {\n");
        context.append("        \"day_of_week\": \"MONDAY\",\n");
        context.append("        \"label\": \"[NHÓM_CƠ_ĐƯỢC_LUYỆN_NGÀY_NÀY]\",\n");
        context.append("        \"is_rest_day\": false,\n");
        context.append("        \"exercises\": [\n");
        context.append("          {\n");
        context.append("             \"exercise_name\": \"[TÊN_BÀI_TẬP_CÓ_SẴN_1]\",\n");
        context.append("             \"description\": \"[HƯỚNG_DẪN_TẬP_NẾU_LÀ_BÀI_MỚI]\",\n");
        context.append("             \"primary_category\": \"CHEST_MIDDLE\",\n");
        context.append("             \"difficulty\": \"NORMAL\",\n");
        context.append("             \"equipment\": \"Barbell\",\n");
        context.append("             \"target_sets\": 3, \"target_reps\": 12, \"note\": \"\"\n");
        context.append("          }\n");
        context.append("        ]\n");
        context.append("      },\n");
        context.append("      {\n");
        context.append("        \"day_of_week\": \"TUESDAY\",\n");
        context.append("        \"label\": \"[NHÓM_CƠ_ĐƯỢC_LUYỆN_NGÀY_NÀY]\",\n");
        context.append("        \"is_rest_day\": false,\n");
        context.append("        \"exercises\": [\n");
        context.append(
                "          {\"exercise_name\": \"[TÊN_BÀI_TẬP_CÓ_SẴN_4]\", \"target_sets\": 3, \"target_reps\": 10, \"note\": \"\"}\n");
        context.append("        ]\n");
        context.append("      },\n");
        context.append("      {\n");
        context.append("        \"day_of_week\": \"WEDNESDAY\",\n");
        context.append("        \"label\": \"Nghỉ ngơi tích cực\",\n");
        context.append("        \"is_rest_day\": true,\n");
        context.append("        \"exercises\": []\n");
        context.append("      },\n");
        context.append("      {\n");
        context.append("        \"day_of_week\": \"THURSDAY\",\n");
        context.append("        \"label\": \"[NHÓM_CƠ_ĐƯỢC_LUYỆN_NGÀY_NÀY]\",\n");
        context.append("        \"is_rest_day\": false,\n");
        context.append("        \"exercises\": [\n");
        context.append(
                "          {\"exercise_name\": \"[TÊN_BÀI_TẬP_CÓ_SẴN_1]\", \"target_sets\": 4, \"target_reps\": 15, \"note\": \"\"}\n");
        context.append("        ]\n");
        context.append("      },\n");
        context.append("      {\n");
        context.append("        \"day_of_week\": \"FRIDAY\",\n");
        context.append("        \"label\": \"[NHÓM_CƠ_ĐƯỢC_LUYỆN_NGÀY_NÀY]\",\n");
        context.append("        \"is_rest_day\": false,\n");
        context.append("        \"exercises\": [\n");
        context.append(
                "          {\"exercise_name\": \"[TÊN_BÀI_TẬP_CÓ_SẴN_7]\", \"target_sets\": 3, \"target_reps\": 10, \"note\": \"\"}\n");
        context.append("        ]\n");
        context.append("      },\n");
        context.append("      {\n");
        context.append("        \"day_of_week\": \"SATURDAY\",\n");
        context.append("        \"label\": \"Nghỉ ngơi\",\n");
        context.append("        \"is_rest_day\": true,\n");
        context.append("        \"exercises\": []\n");
        context.append("      },\n");
        context.append("      {\n");
        context.append("        \"day_of_week\": \"SUNDAY\",\n");
        context.append("        \"label\": \"Nghỉ ngơi\",\n");
        context.append("        \"is_rest_day\": true,\n");
        context.append("        \"exercises\": []\n");
        context.append("      }\n");
        context.append("    ]\n");
        context.append("  }\n");
        context.append("}\n");
        context.append("```\n");
        context.append(
                "- Nếu là bài tập mới (không có trong danh sách), BẮT BUỘC cung cấp đủ: description, primary_category (dùng MỘT TRONG CÁC GIÁ TRỊ TỪ ENUM: CHEST_UPPER, CHEST_MIDDLE, CHEST_LOWER, LATS, RHOMBOIDS, TRAPS_UPPER, TRAPS_MIDDLE_LOWER, LOWER_BACK, SHOULDERS_FRONT, SHOULDERS_SIDE, SHOULDERS_REAR, ROTATOR_CUFF, BICEPS, TRICEPS, FOREARMS, QUADS, HAMSTRINGS, GLUTES, CALVES, ADDUCTORS, ABDUCTORS, ABS_UPPER, ABS_LOWER, OBLIQUES, SERRATUS_ANTERIOR, CORE, CARDIO, FULL_BODY, NECK, STRETCHING, MOBILITY), difficulty (EASY, NORMAL, HARD), equipment. Nếu đã có trong danh sách thì có thể bỏ qua các trường này.\n");
        context.append(
                "- JSON phải chứa ĐẦY ĐỦ 7 ngày (MONDAY đến SUNDAY). Ngày không tập: is_rest_day=true, exercises=[]. JSON không có comment.\n");

        return context.toString();
    }
}
