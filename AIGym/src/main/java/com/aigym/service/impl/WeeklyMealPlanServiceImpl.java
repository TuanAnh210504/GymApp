package com.aigym.service.impl;

import com.aigym.common.exception.BadRequestException;
import com.aigym.common.exception.NotFoundException;
import com.aigym.domain.entity.*;
import com.aigym.dto.FoodItem.FoodItemResponse;
import com.aigym.dto.MealPlan.MealPlanDayResponse;
import com.aigym.dto.MealPlan.PlannedMealResponse;
import com.aigym.dto.MealPlan.WeeklyMealPlanRequest;
import com.aigym.dto.MealPlan.WeeklyMealPlanResponse;
import com.aigym.dto.NutritionLog.NutritionLogResponse;
import com.aigym.dto.user.UserResponse;
import com.aigym.mapper.GenericMapper;
import com.aigym.repository.FoodItemRepository;
import com.aigym.repository.NutritionLogRepository;
import com.aigym.repository.PlannedMealRepository;
import com.aigym.repository.WeeklyMealPlanRepository;
import com.aigym.security.CurrentUserService;
import com.aigym.service.WeeklyMealPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WeeklyMealPlanServiceImpl implements WeeklyMealPlanService {

    private final WeeklyMealPlanRepository weeklyMealPlanRepository;
    private final PlannedMealRepository plannedMealRepository;
    private final FoodItemRepository foodItemRepository;
    private final NutritionLogRepository nutritionLogRepository;
    private final CurrentUserService currentUserService;
    private final GenericMapper genericMapper;

    @Override
    @Transactional
    public WeeklyMealPlanResponse createWeeklyMealPlan(WeeklyMealPlanRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        WeeklyMealPlan plan = new WeeklyMealPlan();
        plan.setUser(currentUser);
        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setActive(request.isActive());

        if (request.isActive()) {
            deactivateCurrentActivePlan(currentUser.getId());
        }

        buildMealPlanHierarchy(plan, request);

        WeeklyMealPlan saved = weeklyMealPlanRepository.save(plan);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyMealPlanResponse getWeeklyMealPlanById(Long id) {
        WeeklyMealPlan plan = getPlanAndVerifyOwnership(id);
        return toResponse(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeeklyMealPlanResponse> getMyWeeklyMealPlans() {
        User currentUser = currentUserService.getCurrentUser();
        // Dùng EntityGraph để tải toàn bộ cây dữ liệu bằng JOIN (tránh N+1)
        List<WeeklyMealPlan> plans = weeklyMealPlanRepository.findWithDetailsByUserId(currentUser.getId());
        return plans.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyMealPlanResponse getMyActiveMealPlan() {
        User currentUser = currentUserService.getCurrentUser();
        WeeklyMealPlan plan = weeklyMealPlanRepository.findByUserIdAndIsActiveTrue(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Bạn chưa có thực đơn nào đang được kích hoạt"));
        return toResponse(plan);
    }

    @Override
    @Transactional
    public WeeklyMealPlanResponse updateWeeklyMealPlan(Long id, WeeklyMealPlanRequest request) {
        WeeklyMealPlan plan = getPlanAndVerifyOwnership(id);

        plan.setName(request.getName());
        plan.setDescription(request.getDescription());

        if (request.isActive() && !plan.isActive()) {
            deactivateCurrentActivePlan(plan.getUser().getId());
        }
        plan.setActive(request.isActive());

        // Xóa hàng loạt bằng Native SQL (nhanh hơn nhiều so để orphanRemoval xóa từng dòng)
        plannedMealRepository.hardDeleteMealsByPlanId(id);
        plannedMealRepository.hardDeleteDaysByPlanId(id);
        // Đồng bộ lại Hibernate Persistence Context (không sinh thêm DELETE)
        plan.getMealPlanDays().clear();

        // Xây dựng lại
        buildMealPlanHierarchy(plan, request);

        WeeklyMealPlan updated = weeklyMealPlanRepository.save(plan);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public WeeklyMealPlanResponse setActiveMealPlan(Long id) {
        WeeklyMealPlan plan = getPlanAndVerifyOwnership(id);

        if (!plan.isActive()) {
            deactivateCurrentActivePlan(plan.getUser().getId());
            plan.setActive(true);
            weeklyMealPlanRepository.save(plan);
        }

        return toResponse(plan);
    }

    @Override
    @Transactional
    public void deleteWeeklyMealPlan(Long id) {
        WeeklyMealPlan plan = getPlanAndVerifyOwnership(id);
        weeklyMealPlanRepository.delete(plan);
    }

    @Override
    @Transactional
    public NutritionLogResponse markMealAsEaten(Long mealId, Double actualAmount) {
        PlannedMeal meal = plannedMealRepository.findById(mealId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy món ăn trong kế hoạch với ID: " + mealId));

        User currentUser = currentUserService.getCurrentUser();
        if (!meal.getMealPlanDay().getWeeklyMealPlan().getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Bạn không có quyền đánh dấu món ăn này");
        }

        meal.setEaten(true);
        plannedMealRepository.save(meal);

        // Tạo NutritionLog
        NutritionLog log = new NutritionLog();
        log.setUser(currentUser);
        if (meal.getFoodItem() != null) {
            log.setFoodItem(meal.getFoodItem());
        } else {
            throw new BadRequestException("Món ăn này chưa có liên kết với dữ liệu dinh dưỡng (FoodItem)");
        }
        log.setAmount(actualAmount != null ? actualAmount : meal.getAmount());
        log.setMealType(meal.getMealType());
        log.setLoggedAt(LocalDate.now());

        NutritionLog savedLog = nutritionLogRepository.save(log);
        return genericMapper.mapToDto(savedLog, NutritionLogResponse.class);
    }

    @Override
    @Transactional
    public void unmarkMealAsEaten(Long mealId) {
        User currentUser = currentUserService.getCurrentUser();
        PlannedMeal meal = plannedMealRepository.findById(mealId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bữa ăn: " + mealId));

        if (!meal.getMealPlanDay().getWeeklyMealPlan().getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Bạn không có quyền sửa bữa ăn này");
        }

        if (!meal.isEaten()) {
            return;
        }

        meal.setEaten(false);
        plannedMealRepository.save(meal);

        if (meal.getFoodItem() != null) {
            List<NutritionLog> logs = nutritionLogRepository.findByUserIdAndLoggedAtOrderByMealTypeAsc(currentUser.getId(), LocalDate.now());
            for (NutritionLog log : logs) {
                if (log.getFoodItem().getId().equals(meal.getFoodItem().getId()) 
                    && log.getMealType().equals(meal.getMealType())) {
                    nutritionLogRepository.delete(log);
                    break;
                }
            }
        }
    }

    @Override
    @Transactional
    public com.aigym.dto.MealPlan.PlannedMealResponse updateMealAmount(Long mealId, Double newAmount) {
        User currentUser = currentUserService.getCurrentUser();
        PlannedMeal meal = plannedMealRepository.findById(mealId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bữa ăn: " + mealId));

        if (!meal.getMealPlanDay().getWeeklyMealPlan().getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Bạn không có quyền sửa bữa ăn này");
        }

        meal.setAmount(newAmount);
        PlannedMeal saved = plannedMealRepository.save(meal);

        // Map sang Response (cần map thủ công FoodItem)
        com.aigym.dto.MealPlan.PlannedMealResponse response = genericMapper.mapToDto(saved, com.aigym.dto.MealPlan.PlannedMealResponse.class);
        if (saved.getFoodItem() != null) {
            response.setFoodItem(genericMapper.mapToDto(saved.getFoodItem(), com.aigym.dto.FoodItem.FoodItemResponse.class));
        }
        return response;
    }

    private void deactivateCurrentActivePlan(Long userId) {
        Optional<WeeklyMealPlan> activePlan = weeklyMealPlanRepository.findByUserIdAndIsActiveTrue(userId);
        activePlan.ifPresent(p -> {
            p.setActive(false);
            weeklyMealPlanRepository.save(p);
        });
    }

    private void buildMealPlanHierarchy(WeeklyMealPlan plan, WeeklyMealPlanRequest request) {
        if (request.getMealPlanDays() != null) {
            request.getMealPlanDays().forEach(dayReq -> {
                MealPlanDay day = new MealPlanDay();
                day.setWeeklyMealPlan(plan);
                day.setDayOfWeek(dayReq.getDayOfWeek());
                day.setLabel(dayReq.getLabel());
                day.setRestDay(Boolean.TRUE.equals(dayReq.getRestDay()));

                if (dayReq.getPlannedMeals() != null) {
                    dayReq.getPlannedMeals().forEach(mealReq -> {
                        PlannedMeal meal = new PlannedMeal();
                        meal.setMealPlanDay(day);

                        if (mealReq.getFoodItemId() != null) {
                            // Dùng thực phẩm có sẵn
                            FoodItem foodItem = foodItemRepository.findById(mealReq.getFoodItemId())
                                    .orElseThrow(() -> new NotFoundException("Không tìm thấy thực phẩm ID: " + mealReq.getFoodItemId()));
                            meal.setFoodItem(foodItem);
                        } else if (mealReq.getCustomFoodName() != null && !mealReq.getCustomFoodName().isEmpty()) {
                            // AI gợi ý món mới
                            String customName = mealReq.getCustomFoodName().trim();
                            User currentUser = currentUserService.getCurrentUser();
                            
                            // 1. Tìm món public cùng tên
                            Optional<FoodItem> existingPublic = foodItemRepository.findByNameIgnoreCaseAndIsPublicTrue(customName);
                            if (existingPublic.isPresent()) {
                                meal.setFoodItem(existingPublic.get());
                            } else {
                                // 2. Tìm món private của user hiện tại cùng tên
                                Optional<FoodItem> existingPrivate = foodItemRepository.findByNameIgnoreCaseAndIsPublicFalseAndUserId(customName, currentUser.getId());
                                if (existingPrivate.isPresent()) {
                                    meal.setFoodItem(existingPrivate.get());
                                } else {
                                    // 3. Tạo mới (private) cho user hiện tại
                                    FoodItem newFood = new FoodItem();
                                    newFood.setName(customName);
                                    newFood.setPublic(false);
                                    newFood.setUser(currentUser);
                                    newFood.setCaloriesPer100g(mealReq.getCaloriesPer100g() != null ? mealReq.getCaloriesPer100g() : 0);
                                    newFood.setProtein(mealReq.getProtein() != null ? mealReq.getProtein() : 0.0);
                                    newFood.setCarbs(mealReq.getCarbs() != null ? mealReq.getCarbs() : 0.0);
                                    newFood.setFat(mealReq.getFat() != null ? mealReq.getFat() : 0.0);
                                    newFood.setFiber(mealReq.getFiber());
                                    
                                    FoodItem savedFood = foodItemRepository.save(newFood);
                                    meal.setFoodItem(savedFood);
                                }
                            }
                        } else {
                            return; // Bỏ qua nếu không có id lẫn tên
                        }

                        meal.setCustomFoodName(mealReq.getCustomFoodName());
                        meal.setAmount(mealReq.getAmount());
                        meal.setMealType(mealReq.getMealType());
                        meal.setOrderIndex(mealReq.getOrderIndex() != null ? mealReq.getOrderIndex() : day.getPlannedMeals().size());
                        meal.setNote(mealReq.getNote());
                        meal.setEaten(Boolean.TRUE.equals(mealReq.getEaten()));

                        day.getPlannedMeals().add(meal);
                    });
                }
                plan.getMealPlanDays().add(day);
            });
        }
    }

    private WeeklyMealPlan getPlanAndVerifyOwnership(Long id) {
        // Dùng EntityGraph để tải toàn bộ cây dữ liệu bằng JOIN (tránh N+1)
        WeeklyMealPlan plan = weeklyMealPlanRepository.findWithDetailsById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thực đơn với ID: " + id));

        User currentUser = currentUserService.getCurrentUser();
        if (!plan.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Bạn không có quyền truy cập thực đơn này");
        }
        return plan;
    }

    // Thủ công map response do cấu trúc lồng sâu 3 tầng
    private WeeklyMealPlanResponse toResponse(WeeklyMealPlan plan) {
        WeeklyMealPlanResponse response = new WeeklyMealPlanResponse();
        response.setId(plan.getId());
        response.setName(plan.getName());
        response.setDescription(plan.getDescription());
        response.setActive(plan.isActive());
        response.setCreatedAt(plan.getCreatedAt());

        if (plan.getUser() != null) {
            response.setUser(genericMapper.mapToDto(plan.getUser(), UserResponse.class));
        }

        List<MealPlanDayResponse> dayResponses = new ArrayList<>();
        if (plan.getMealPlanDays() != null) {
            plan.getMealPlanDays().forEach(day -> {
                MealPlanDayResponse dayResp = new MealPlanDayResponse();
                dayResp.setId(day.getId());
                dayResp.setDayOfWeek(day.getDayOfWeek());
                dayResp.setLabel(day.getLabel());
                dayResp.setRestDay(day.isRestDay());

                List<PlannedMealResponse> mealResponses = new ArrayList<>();
                if (day.getPlannedMeals() != null) {
                    day.getPlannedMeals().forEach(meal -> {
                        PlannedMealResponse mealResp = new PlannedMealResponse();
                        mealResp.setId(meal.getId());
                        if (meal.getFoodItem() != null) {
                            mealResp.setFoodItem(genericMapper.mapToDto(meal.getFoodItem(), FoodItemResponse.class));
                        }
                        mealResp.setCustomFoodName(meal.getCustomFoodName());
                        mealResp.setAmount(meal.getAmount());
                        mealResp.setMealType(meal.getMealType());
                        mealResp.setOrderIndex(meal.getOrderIndex());
                        mealResp.setNote(meal.getNote());
                        mealResp.setEaten(meal.isEaten());
                        mealResponses.add(mealResp);
                    });
                }
                dayResp.setPlannedMeals(mealResponses);
                dayResponses.add(dayResp);
            });
        }
        response.setMealPlanDays(dayResponses);
        return response;
    }
}
