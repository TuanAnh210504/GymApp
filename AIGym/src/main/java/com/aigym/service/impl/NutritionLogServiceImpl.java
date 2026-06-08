package com.aigym.service.impl;

import com.aigym.common.exception.BadRequestException;
import com.aigym.common.exception.NotFoundException;
import com.aigym.domain.entity.FoodItem;
import com.aigym.domain.entity.NutritionLog;
import com.aigym.domain.entity.User;
import com.aigym.dto.FoodItem.FoodItemResponse;
import com.aigym.dto.NutritionLog.NutritionLogRequest;
import com.aigym.dto.NutritionLog.NutritionLogResponse;
import com.aigym.dto.user.UserResponse;
import com.aigym.mapper.GenericMapper;
import com.aigym.repository.FoodItemRepository;
import com.aigym.repository.NutritionLogRepository;
import com.aigym.security.CurrentUserService;
import com.aigym.service.NutritionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NutritionLogServiceImpl implements NutritionLogService {

    private final NutritionLogRepository nutritionLogRepository;
    private final FoodItemRepository foodItemRepository;
    private final CurrentUserService currentUserService;
    private final GenericMapper genericMapper;

    @Override
    @Transactional
    public NutritionLogResponse createNutritionLog(NutritionLogRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        FoodItem foodItem = foodItemRepository.findById(request.getFoodItemId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thực phẩm với ID: " + request.getFoodItemId()));

        NutritionLog log = genericMapper.mapToEntity(request, NutritionLog.class);
        log.setUser(currentUser);
        log.setFoodItem(foodItem);

        NutritionLog saved = nutritionLogRepository.save(log);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public NutritionLogResponse getNutritionLogById(Long id) {
        NutritionLog log = getLogAndVerifyOwnership(id);
        return toResponse(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NutritionLogResponse> getMyLogsByDate(LocalDate date) {
        User currentUser = currentUserService.getCurrentUser();
        List<NutritionLog> logs = nutritionLogRepository.findByUserIdAndLoggedAtOrderByMealTypeAsc(currentUser.getId(), date);
        return logs.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public NutritionLogResponse updateNutritionLog(Long id, NutritionLogRequest request) {
        NutritionLog log = getLogAndVerifyOwnership(id);

        FoodItem foodItem = foodItemRepository.findById(request.getFoodItemId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thực phẩm với ID: " + request.getFoodItemId()));

        log.setFoodItem(foodItem);
        log.setAmount(request.getAmount());
        log.setMealType(request.getMealType());
        log.setLoggedAt(request.getLoggedAt());

        NutritionLog updated = nutritionLogRepository.save(log);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteNutritionLog(Long id) {
        NutritionLog log = getLogAndVerifyOwnership(id);
        nutritionLogRepository.delete(log);
    }

    private NutritionLog getLogAndVerifyOwnership(Long id) {
        NutritionLog log = nutritionLogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy nhật ký với ID: " + id));

        User currentUser = currentUserService.getCurrentUser();
        if (!log.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Bạn không có quyền truy cập nhật ký này");
        }
        return log;
    }

    private NutritionLogResponse toResponse(NutritionLog log) {
        NutritionLogResponse response = genericMapper.mapToDto(log, NutritionLogResponse.class);
        if (log.getFoodItem() != null) {
            response.setFoodItem(genericMapper.mapToDto(log.getFoodItem(), FoodItemResponse.class));
        }
        return response;
    }
}
