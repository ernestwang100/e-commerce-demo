package com.superdupermart.shopping.controller;

import com.superdupermart.shopping.dto.AdminStatsResponse;
import com.superdupermart.shopping.dto.UserStatsResponse;
import com.superdupermart.shopping.security.SecurityUtils;
import com.superdupermart.shopping.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final StatsService statsService;

    @Autowired
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminStatsResponse> getAdminStats() {
        return ResponseEntity.ok(statsService.getAdminStats());
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserStatsResponse> getUserStats() {
        Integer userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(statsService.getUserStats(userId));
    }
}
