package com.shelflife.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shelflife.project.model.Product;
import java.util.List;


@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByOwnerId(long ownerId);
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByCategoryContainingIgnoreCase(String category);
    List<Product> findByBarcodeContainingIgnoreCase(String barcode);
    
    List<Product> findByNameContainingIgnoreCaseAndCategoryContainingIgnoreCase(String name, String category);

    List<Product> findByNameContainingIgnoreCaseAndCategoryContainingIgnoreCaseAndBarcodeContainingIgnoreCase(String name, String category, String barcode);


    boolean existsByBarcode(String barcode);

    @Query("select p.category from Product p")
    List<String> getCategories();
}