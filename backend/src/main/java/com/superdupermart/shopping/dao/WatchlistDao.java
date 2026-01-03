package com.superdupermart.shopping.dao;

import com.superdupermart.shopping.entity.User;
import com.superdupermart.shopping.entity.Watchlist;
import java.util.List;

public interface WatchlistDao {
    List<Watchlist> getWatchlistByUser(User user);
    void save(Watchlist watchlist);
    void delete(Watchlist watchlist);
    boolean existsByUserAndProduct(Integer userId, Integer productId);
}
