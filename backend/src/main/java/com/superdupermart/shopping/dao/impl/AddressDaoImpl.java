package com.superdupermart.shopping.dao.impl;

import com.superdupermart.shopping.dao.AddressDao;
import com.superdupermart.shopping.entity.Address;
import com.superdupermart.shopping.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class AddressDaoImpl implements AddressDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Address> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(Address.class, id));
    }

    @Override
    public List<Address> findByUser(User user) {
        return entityManager.createQuery("SELECT a FROM Address a WHERE a.user = :user", Address.class)
                .setParameter("user", user)
                .getResultList();
    }

    @Override
    public void save(Address address) {
        if (address.getId() == null) {
            entityManager.persist(address);
        } else {
            entityManager.merge(address);
        }
    }

    @Override
    public void delete(Address address) {
        entityManager.remove(address);
    }
}
