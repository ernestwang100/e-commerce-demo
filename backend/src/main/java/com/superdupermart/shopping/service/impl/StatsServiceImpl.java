package com.superdupermart.shopping.service.impl;

import com.superdupermart.shopping.dto.AdminStatsResponse;
import com.superdupermart.shopping.dto.ProductStatDto;
import com.superdupermart.shopping.dto.UserStatsResponse;
import com.superdupermart.shopping.service.StatsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatsServiceImpl implements StatsService {

        @PersistenceContext
        private EntityManager entityManager;

        @Override
        public UserStatsResponse getUserStats(Integer userId) {
                // Top 3 most recently purchased item names
                // Using GROUP BY with MAX to avoid DISTINCT + ORDER BY incompatibility in MySQL
                List<String> mostRecent = entityManager.createQuery(
                                "SELECT i.product.name FROM OrderItem i WHERE i.order.user.id = :userId GROUP BY i.product.name ORDER BY MAX(i.order.datePlaced) DESC",
                                String.class)
                                .setParameter("userId", userId)
                                .setMaxResults(3)
                                .getResultList();

                // Top 3 most frequently purchased item names
                List<String> mostFrequent = entityManager.createQuery(
                                "SELECT i.product.name FROM OrderItem i WHERE i.order.user.id = :userId GROUP BY i.product.name ORDER BY COUNT(i) DESC",
                                String.class)
                                .setParameter("userId", userId)
                                .setMaxResults(3)
                                .getResultList();

                return UserStatsResponse.builder()
                                .mostRecent(mostRecent)
                                .mostFrequent(mostFrequent)
                                .build();
        }

        @Override
        public AdminStatsResponse getAdminStats() {
                // Total successfully sold items (from Completed orders only)
                Long totalSoldItems = entityManager.createQuery(
                                "SELECT COALESCE(SUM(i.quantity), 0) FROM OrderItem i WHERE i.order.orderStatus = 'Completed'",
                                Long.class)
                                .getSingleResult();

                // Top 3 most popular products (by total quantity sold)
                List<Object[]> popularData = entityManager.createQuery(
                                "SELECT i.product.name, SUM(CAST(i.quantity AS bigdecimal)) FROM OrderItem i WHERE i.order.orderStatus IN ('Completed', 'Processing') GROUP BY i.product.name ORDER BY SUM(i.quantity) DESC",
                                Object[].class)
                                .setMaxResults(3)
                                .getResultList();

                List<ProductStatDto> mostPopular = popularData.stream()
                                .map(data -> new ProductStatDto((String) data[0], (BigDecimal) data[1]))
                                .collect(Collectors.toList());

                // Top 3 most profitable products
                // Profit = (purchasedPrice - wholesalePrice) * quantity
                List<Object[]> profitData = entityManager.createQuery(
                                "SELECT i.product.name, SUM((i.purchasedPrice - i.product.wholesalePrice) * i.quantity) FROM OrderItem i WHERE i.order.orderStatus IN ('Completed', 'Processing') GROUP BY i.product.name ORDER BY SUM((i.purchasedPrice - i.product.wholesalePrice) * i.quantity) DESC",
                                Object[].class)
                                .setMaxResults(3)
                                .getResultList();

                List<ProductStatDto> mostProfitable = profitData.stream()
                                .map(data -> new ProductStatDto((String) data[0], (BigDecimal) data[1]))
                                .collect(Collectors.toList());

                return AdminStatsResponse.builder()
                                .totalSoldItems(totalSoldItems)
                                .mostPopular(mostPopular)
                                .mostProfitable(mostProfitable)
                                .build();
        }
}
