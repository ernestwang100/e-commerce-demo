package com.superdupermart.shopping.service.impl;

import com.superdupermart.shopping.dao.ProductDao;
import com.superdupermart.shopping.dao.UserDao;
import com.superdupermart.shopping.dao.WatchlistDao;
import com.superdupermart.shopping.dto.WatchlistResponse;
import com.superdupermart.shopping.entity.Product;
import com.superdupermart.shopping.entity.User;
import com.superdupermart.shopping.entity.Watchlist;
import com.superdupermart.shopping.service.WatchlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WatchlistServiceImpl implements WatchlistService {

    private final WatchlistDao watchlistDao;
    private final UserDao userDao;
    private final ProductDao productDao;

    @Autowired
    public WatchlistServiceImpl(WatchlistDao watchlistDao, UserDao userDao, ProductDao productDao) {
        this.watchlistDao = watchlistDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    @Override
    public List<WatchlistResponse> getWatchlistByUser(Integer userId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return watchlistDao.getWatchlistByUser(user).stream()
                .map(w -> mapToResponse(w.getProduct()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addToWatchlist(Integer userId, Integer productId) {
        if (watchlistDao.existsByUserAndProduct(userId, productId)) {
            return; // Already in watchlist
        }

        User user = userDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Watchlist watchlist = Watchlist.builder()
                .user(user)
                .product(product)
                .build();
        
        watchlistDao.save(watchlist);
    }

    @Override
    @Transactional
    public void removeFromWatchlist(Integer userId, Integer productId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Watchlist> items = watchlistDao.getWatchlistByUser(user);
        for (Watchlist item : items) {
            if (item.getProduct().getId().equals(productId)) {
                watchlistDao.delete(item);
                break;
            }
        }
    }

    private WatchlistResponse mapToResponse(Product product) {
        return WatchlistResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .description(product.getDescription())
                .retailPrice(product.getRetailPrice())
                .inStock(product.getQuantity() > 0)
                .build();
    }
}
