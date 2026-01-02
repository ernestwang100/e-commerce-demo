package com.superdupermart.shopping.service;

import com.superdupermart.shopping.dto.WatchlistResponse;
import java.util.List;

public interface WatchlistService {
    List<WatchlistResponse> getWatchlistByUser(Integer userId);
    void addToWatchlist(Integer userId, Integer productId);
    void removeFromWatchlist(Integer userId, Integer productId);
}
