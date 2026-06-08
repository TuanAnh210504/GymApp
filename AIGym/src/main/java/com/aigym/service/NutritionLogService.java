package com.aigym.service;

import com.aigym.dto.NutritionLog.NutritionLogRequest;
import com.aigym.dto.NutritionLog.NutritionLogResponse;

import java.time.LocalDate;
import java.util.List;

public interface NutritionLogService {

    NutritionLogResponse createNutritionLog(NutritionLogRequest request);

    NutritionLogResponse getNutritionLogById(Long id);

    List<NutritionLogResponse> getMyLogsByDate(LocalDate date);

    NutritionLogResponse updateNutritionLog(Long id, NutritionLogRequest request);

    void deleteNutritionLog(Long id);
}
