package com.superdupermart.shopping.controller;

import com.superdupermart.shopping.dto.AdminStatsResponse;
import com.superdupermart.shopping.dto.UserStatsResponse;
import com.superdupermart.shopping.entity.User;
import com.superdupermart.shopping.dao.UserDao;
import com.superdupermart.shopping.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatsController {

    private final StatsService statsService;
    private final UserDao userDao;

    @Autowired
    public StatsController(StatsService statsService, UserDao userDao) {
        this.statsService = statsService;
        this.userDao = userDao;
    }

    @GetMapping("/user/stats")
    public ResponseEntity<UserStatsResponse> getUserStats(Authentication auth) {
        Integer userId = getUserId(auth);
        return ResponseEntity.ok(statsService.getUserStats(userId));
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<AdminStatsResponse> getAdminStats() {
        return ResponseEntity.ok(statsService.getAdminStats());
    }

    private Integer getUserId(Authentication auth) {
        String username = auth.getName();
        return userDao.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
