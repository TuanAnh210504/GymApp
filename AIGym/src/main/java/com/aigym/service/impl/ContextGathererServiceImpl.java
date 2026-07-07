package com.aigym.service.impl;

import com.aigym.dto.Exercise.ExerciseResponse;
import com.aigym.dto.UserProfile.UserProfileResponse;
import com.aigym.dto.WeeklySchedule.WeeklyScheduleResponse;
import com.aigym.dto.WeeklySchedule.ScheduleDayResponse;
import com.aigym.dto.MealPlan.WeeklyMealPlanResponse;
import com.aigym.dto.MealPlan.MealPlanDayResponse;
import com.aigym.service.ContextGathererService;
import com.aigym.service.ExerciseService;
import com.aigym.service.UserProfileService;
import com.aigym.service.WeeklyScheduleService;
import com.aigym.service.WeeklyMealPlanService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ── FIX: Đã refactor từ ~250 dòng StringBuilder thành template-based approach.
 * System prompt được lưu trong resources/ai_system_prompt.txt để dễ bảo trì.
 * Data động được inject qua placeholder {{SECTION_NAME}}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContextGathererServiceImpl implements ContextGathererService {

    private final UserProfileService userProfileService;
    private final WeeklyScheduleService weeklyScheduleService;
    private final ExerciseService exerciseService;
    private final WeeklyMealPlanService weeklyMealPlanService;

    /** Template được load một lần khi khởi động app (không load lại mỗi request). */
    private String promptTemplate;

    @PostConstruct
    public void loadTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource("ai_system_prompt.txt");
            promptTemplate = resource.getContentAsString(StandardCharsets.UTF_8);
            log.info("AI system prompt template loaded successfully ({} chars)", promptTemplate.length());
        } catch (IOException e) {
            log.error("Cannot load ai_system_prompt.txt – falling back to empty template", e);
            promptTemplate = "Bạn là AIGym Personal Trainer.\n{{USER_PROFILE}}\n{{WORKOUT_SCHEDULE}}\n{{EXERCISE_LIST}}\n{{MEAL_PLAN}}";
        }
    }

    @Override
    public String gatherUserContext() {
        return promptTemplate
                .replace("{{USER_PROFILE}}", buildUserProfileSection())
                .replace("{{WORKOUT_SCHEDULE}}", buildWorkoutScheduleSection())
                .replace("{{EXERCISE_LIST}}", buildExerciseListSection())
                .replace("{{MEAL_PLAN}}", buildMealPlanSection());
    }

    // ─────────────────────────── SECTION BUILDERS ────────────────────────────

    private String buildUserProfileSection() {
        try {
            UserProfileResponse profile = userProfileService.getMyProfile();
            if (profile == null) return "";

            StringBuilder sb = new StringBuilder("--- HỒ SƠ NGƯỜI DÙNG ---\n");
            sb.append(String.format("- Tên: %s%n", profile.getUser().getFullName()));
            sb.append(String.format("- Giới tính: %s%n", nullSafe(profile.getGender())));
            sb.append(String.format("- Chiều cao: %s cm%n", nullSafe(profile.getHeight())));
            sb.append(String.format("- Cân nặng: %s kg%n", nullSafe(profile.getWeight())));
            sb.append(String.format("- Mức độ vận động: %s%n", nullSafe(profile.getActivityLevel())));
            sb.append(String.format("- Mục tiêu: %s%n", nullSafe(profile.getGoalType())));

            if (profile.getDailyCalorieGoal() != null) {
                sb.append(String.format("- Mức Năng lượng Mục tiêu (TDEE): %s kcal/ngày%n", profile.getDailyCalorieGoal()));
                sb.append("  (Chỉ số dinh dưỡng: ");
                if (profile.getDailyProteinGoal() != null) sb.append(String.format("Protein: %sg, ", profile.getDailyProteinGoal()));
                if (profile.getDailyCarbsGoal() != null)   sb.append(String.format("Carbs: %sg, ", profile.getDailyCarbsGoal()));
                if (profile.getDailyFatsGoal() != null)    sb.append(String.format("Fat: %sg, ", profile.getDailyFatsGoal()));
                if (profile.getDailyFiberGoal() != null)   sb.append(String.format("Fiber: %sg", profile.getDailyFiberGoal()));
                sb.append(")\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.debug("No user profile found for AI context: {}", e.getMessage());
            return "";
        }
    }

    private String buildWorkoutScheduleSection() {
        try {
            WeeklyScheduleResponse schedule = weeklyScheduleService.getMyActiveSchedule();
            if (schedule == null) {
                return "Người dùng hiện chưa áp dụng lịch tập nào.\n";
            }

            StringBuilder sb = new StringBuilder("--- LỊCH TẬP HIỆN TẠI (TÊN LỊCH: ")
                    .append(schedule.getName()).append(") ---\n");

            if (schedule.getScheduleDays() != null) {
                for (ScheduleDayResponse day : schedule.getScheduleDays()) {
                    sb.append("- ").append(day.getDayOfWeek()).append(": ");
                    if (day.isRestDay()) {
                        sb.append("Ngày nghỉ ngơi\n");
                    } else if (day.getScheduledExercises() == null || day.getScheduledExercises().isEmpty()) {
                        sb.append("Chưa có bài tập\n");
                    } else {
                        String exStr = day.getScheduledExercises().stream()
                                .map(ex -> String.format("%s (%d sets x %d reps%s)",
                                        ex.getExercise() != null ? ex.getExercise().getName() : "Bài tập",
                                        ex.getTargetSets(), ex.getTargetReps(),
                                        (ex.getTargetWeight() != null && ex.getTargetWeight() > 0)
                                                ? " - " + ex.getTargetWeight() + "kg" : ""))
                                .collect(Collectors.joining(", "));
                        sb.append(exStr).append("\n");
                    }
                }
            }
            return sb.toString();
        } catch (Exception e) {
            log.debug("Could not load workout schedule for AI context: {}", e.getMessage());
            return "Không thể lấy lịch tập hiện tại.\n";
        }
    }

    private String buildExerciseListSection() {
        try {
            // Dùng getAllExercises() không phân trang để lấy tên bài tập cho AI context
            List<ExerciseResponse> exercises = exerciseService.getAllExercises();
            if (exercises == null || exercises.isEmpty()) return "";

            String names = exercises.stream()
                    .map(ExerciseResponse::getName)
                    .collect(Collectors.joining(", "));
            return "\n--- DANH SÁCH BÀI TẬP CÓ SẴN TRONG HỆ THỐNG (chỉ để tham khảo) ---\n"
                    + "Danh sách: " + names + "\n";
        } catch (Exception e) {
            log.debug("Could not load exercise list for AI context: {}", e.getMessage());
            return "";
        }
    }

    private String buildMealPlanSection() {
        try {
            WeeklyMealPlanResponse mealPlan = weeklyMealPlanService.getMyActiveMealPlan();
            if (mealPlan == null) {
                return "Người dùng hiện chưa áp dụng thực đơn nào.\n";
            }

            StringBuilder sb = new StringBuilder("--- THỰC ĐƠN HIỆN TẠI (TÊN: ")
                    .append(mealPlan.getName()).append(") ---\n");

            if (mealPlan.getMealPlanDays() != null) {
                for (MealPlanDayResponse day : mealPlan.getMealPlanDays()) {
                    sb.append("- ").append(day.getDayOfWeek()).append(": ");
                    if (day.isRestDay()) {
                        sb.append("Ngày ăn tự do (Cheat day)\n");
                    } else if (day.getPlannedMeals() == null || day.getPlannedMeals().isEmpty()) {
                        sb.append("Chưa có món ăn\n");
                    } else {
                        String mealsStr = day.getPlannedMeals().stream()
                                .map(m -> String.format("%s (%.0fg) - %s",
                                        m.getFoodItem() != null ? m.getFoodItem().getName() : m.getCustomFoodName(),
                                        m.getAmount(), m.getMealType()))
                                .collect(Collectors.joining(", "));
                        sb.append(mealsStr).append("\n");
                    }
                }
            }
            return sb.toString();
        } catch (Exception e) {
            log.debug("Could not load meal plan for AI context: {}", e.getMessage());
            return "";
        }
    }

    private String nullSafe(Object value) {
        return value != null ? value.toString() : "Chưa rõ";
    }
}
