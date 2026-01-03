package com.superdupermart.shopping.dao;

import com.superdupermart.shopping.entity.Order;
import com.superdupermart.shopping.entity.User;
import java.util.List;
import java.util.Optional;

public interface OrderDao {
    Optional<Order> findById(Integer id);
    List<Order> getAllOrders();
    List<Order> getOrdersByUser(User user);
    List<Order> getPaginatedOrders(int page, int size);
    void save(Order order);
    void update(Order order);
}
