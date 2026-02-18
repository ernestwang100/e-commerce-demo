package com.superdupermart.shopping.dao;

import com.superdupermart.shopping.entity.PaymentMethod;
import com.superdupermart.shopping.entity.User;
import java.util.List;
import java.util.Optional;

public interface PaymentMethodDao {
    Optional<PaymentMethod> findById(Integer id);

    List<PaymentMethod> findByUser(User user);

    void save(PaymentMethod paymentMethod);

    void delete(PaymentMethod paymentMethod);
}
