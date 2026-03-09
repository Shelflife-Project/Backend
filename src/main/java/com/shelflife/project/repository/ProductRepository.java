package com.shelflife.project.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shelflife.project.model.Product;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByOwnerId(long ownerId);

    @Query("""
            select p from Product p
            where
            lower(p.name) like lower(concat('%', ?1, '%')) OR
            lower(p.category) like lower(concat('%', ?1, '%')) OR
            lower(p.barcode) like lower(concat('%', ?1, '%'))
            """)
    Page<Product> searchProducts(String search, Pageable pageable);

    boolean existsByBarcode(String barcode);

    @Query("select distinct p.category from Product p")
    List<String> getCategories();
}