package com.shelflife.project.seed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.shelflife.project.model.Product;
import com.shelflife.project.repository.ProductRepository;
import com.shelflife.project.repository.UserRepository;

@Component
public class ProductSeeder implements Seeder {
    @Autowired
    private ProductRepository repository;

    @Autowired
    private UserRepository userRepository;

    public void seed() {
        Product userProduct = new Product();
        userProduct.setName("Bread");
        userProduct.setCategory("Baked good");
        userProduct.setBarcode("1234");
        userProduct.setExpirationDaysDelta(5);
        userProduct.setOwner(userRepository.findByEmail("test@test.test").get());

        repository.save(userProduct);

        Product adminProduct = new Product();
        adminProduct.setName("Milk");
        adminProduct.setCategory("Dairy");
        adminProduct.setBarcode("4567");
        adminProduct.setExpirationDaysDelta(90);
        adminProduct.setOwner(userRepository.findByEmail("admin@test.test").get());

        repository.save(adminProduct);
    }
}
