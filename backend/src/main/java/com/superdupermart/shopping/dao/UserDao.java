package com.superdupermart.shopping.dao;

import com.superdupermart.shopping.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserDao {
    Optional<User> findById(Integer id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    void save(User user);
    List<User> getAllUsers();
}
