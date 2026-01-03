package com.superdupermart.shopping.controller;

import com.superdupermart.shopping.dto.WatchlistResponse;
import com.superdupermart.shopping.entity.User;
import com.superdupermart.shopping.dao.UserDao;
import com.superdupermart.shopping.service.WatchlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/user/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final UserDao userDao;

    @Autowired
    public WatchlistController(WatchlistService watchlistService, UserDao userDao) {
        this.watchlistService = watchlistService;
        this.userDao = userDao;
    }

    @GetMapping
    public ResponseEntity<List<WatchlistResponse>> getWatchlist(Authentication auth) {
        Integer userId = getUserId(auth);
        return ResponseEntity.ok(watchlistService.getWatchlistByUser(userId));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<String> addToWatchlist(Authentication auth, @PathVariable Integer productId) {
        Integer userId = getUserId(auth);
        watchlistService.addToWatchlist(userId, productId);
        return ResponseEntity.ok("Product added to watchlist");
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<String> removeFromWatchlist(Authentication auth, @PathVariable Integer productId) {
        Integer userId = getUserId(auth);
        watchlistService.removeFromWatchlist(userId, productId);
        return ResponseEntity.ok("Product removed from watchlist");
    }

    private Integer getUserId(Authentication auth) {
        String username = auth.getName();
        return userDao.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
