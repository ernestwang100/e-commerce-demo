package com.superdupermart.shopping.dao.impl;

import com.superdupermart.shopping.dao.PaymentMethodDao;
import com.superdupermart.shopping.entity.PaymentMethod;
import com.superdupermart.shopping.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class PaymentMethodDaoImpl implements PaymentMethodDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<PaymentMethod> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(PaymentMethod.class, id));
    }

    @Override
    public List<PaymentMethod> findByUser(User user) {
        return entityManager.createQuery("SELECT p FROM PaymentMethod p WHERE p.user = :user", PaymentMethod.class)
                .setParameter("user", user)
                .getResultList();
    }

    @Override
    public void save(PaymentMethod paymentMethod) {
        if (paymentMethod.getId() == null) {
            entityManager.persist(paymentMethod);
        } else {
            entityManager.merge(paymentMethod);
        }
    }

    @Override
    public void delete(PaymentMethod paymentMethod) {
        entityManager.remove(paymentMethod);
    }
}
