package com.superdupermart.shopping.dao;

import com.superdupermart.shopping.entity.Address;
import com.superdupermart.shopping.entity.User;
import java.util.List;
import java.util.Optional;

public interface AddressDao {
    Optional<Address> findById(Integer id);

    List<Address> findByUser(User user);

    void save(Address address);

    void delete(Address address);
}
