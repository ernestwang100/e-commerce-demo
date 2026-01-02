package com.superdupermart.shopping.service;

import com.superdupermart.shopping.dto.AdminStatsResponse;
import com.superdupermart.shopping.dto.UserStatsResponse;

public interface StatsService {
    UserStatsResponse getUserStats(Integer userId);
    AdminStatsResponse getAdminStats();
}
