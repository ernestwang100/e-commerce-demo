package com.superdupermart.shopping.controller;

import com.superdupermart.shopping.dto.WatchlistResponse;
import com.superdupermart.shopping.security.SecurityUtils;
import com.superdupermart.shopping.service.WatchlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;

    @Autowired
    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @GetMapping("/products/all")
    public ResponseEntity<List<WatchlistResponse>> getWatchlist() {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(watchlistService.getWatchlistByUser(userId));
    }

    @PostMapping("/product/{productId}")
    public ResponseEntity<String> addToWatchlist(@PathVariable Integer productId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        watchlistService.addToWatchlist(userId, productId);
        return ResponseEntity.ok("Product added to watchlist");
    }

    @DeleteMapping("/product/{productId}")
    public ResponseEntity<String> removeFromWatchlist(@PathVariable Integer productId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        watchlistService.removeFromWatchlist(userId, productId);
        return ResponseEntity.ok("Product removed from watchlist");
    }
}
