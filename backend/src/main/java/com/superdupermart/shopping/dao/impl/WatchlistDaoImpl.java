package com.superdupermart.shopping.dao.impl;

import com.superdupermart.shopping.dao.WatchlistDao;
import com.superdupermart.shopping.entity.User;
import com.superdupermart.shopping.entity.Watchlist;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class WatchlistDaoImpl implements WatchlistDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Watchlist> getWatchlistByUser(User user) {
        return entityManager.createQuery("SELECT w FROM Watchlist w WHERE w.user = :user", Watchlist.class)
                .setParameter("user", user)
                .getResultList();
    }

    @Override
    public void save(Watchlist watchlist) {
        entityManager.persist(watchlist);
    }

    @Override
    public void delete(Watchlist watchlist) {
        entityManager.remove(watchlist);
    }

    @Override
    public boolean existsByUserAndProduct(Integer userId, Integer productId) {
        Long count = entityManager.createQuery(
                "SELECT COUNT(w) FROM Watchlist w WHERE w.user.id = :userId AND w.product.id = :productId", Long.class)
                .setParameter("userId", userId)
                .setParameter("productId", productId)
                .getSingleResult();
        return count > 0;
    }
}
