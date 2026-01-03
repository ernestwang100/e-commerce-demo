package com.superdupermart.shopping.dao.impl;

import com.superdupermart.shopping.dao.OrderDao;
import com.superdupermart.shopping.entity.Order;
import com.superdupermart.shopping.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderDaoImpl implements OrderDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Order> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(Order.class, id));
    }

    @Override
    public List<Order> getAllOrders() {
        return entityManager.createQuery("SELECT o FROM Order o", Order.class).getResultList();
    }

    @Override
    public List<Order> getOrdersByUser(User user) {
        return entityManager.createQuery("SELECT o FROM Order o WHERE o.user = :user", Order.class)
                .setParameter("user", user)
                .getResultList();
    }

    @Override
    public List<Order> getPaginatedOrders(int page, int size) {
        return entityManager.createQuery("SELECT o FROM Order o ORDER BY o.datePlaced DESC", Order.class)
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public void save(Order order) {
        entityManager.persist(order);
    }

    @Override
    public void update(Order order) {
        entityManager.merge(order);
    }
}
