package com.superdupermart.shopping.dao.impl;

import com.superdupermart.shopping.dao.ProductDao;
import com.superdupermart.shopping.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductDaoImpl implements ProductDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Product> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(Product.class, id));
    }

    @Override
    public List<Product> getAllProducts() {
        return entityManager.createQuery("SELECT p FROM Product p", Product.class).getResultList();
    }

    @Override
    public List<Product> getInStockProducts() {
        // Implementation using Criteria API as required
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> cq = cb.createQuery(Product.class);
        Root<Product> product = cq.from(Product.class);

        cq.select(product)
                .where(cb.gt(product.get("quantity"), 0));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public void save(Product product) {
        entityManager.persist(product);
    }

    @Override
    public void update(Product product) {
        entityManager.merge(product);
    }

    @Override
    public List<Product> searchProducts(String query, Double minPrice, Double maxPrice) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> cq = cb.createQuery(Product.class);
        Root<Product> product = cq.from(Product.class);

        java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

        // Search by name or description
        if (query != null && !query.isEmpty()) {
            String likePattern = "%" + query.toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(product.get("name")), likePattern),
                    cb.like(cb.lower(product.get("description")), likePattern)));
        }

        // Filter by Price Range
        if (minPrice != null) {
            predicates.add(cb.ge(product.get("retailPrice"), minPrice));
        }

        if (maxPrice != null) {
            predicates.add(cb.le(product.get("retailPrice"), maxPrice));
        }

        cq.where(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));

        return entityManager.createQuery(cq).getResultList();
    }
}
