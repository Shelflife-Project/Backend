package com.shelflife.project.seed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.shelflife.project.model.Product;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ProductRepository;
import com.shelflife.project.repository.UserRepository;

@Component
public class ProductSeeder implements Seeder {
    @Autowired
    private ProductRepository repository;

    @Autowired
    private UserRepository userRepository;

    public void seed() {
        User user = userRepository.findByEmail("test@test.test").get();

        Product userProduct = new Product();
        userProduct.setName("Bread");
        userProduct.setDescription("A very nice and fresh loaf of bread.");
        userProduct.setCategory("Baked good");
        userProduct.setBarcode("1234");
        userProduct.setExpirationDaysDelta(5);
        userProduct.setOwner(user);
        repository.save(userProduct);

        Product chips = new Product();
        chips.setName("Chips");
        chips.setDescription("300g Crunchy Chips");
        chips.setCategory("Snacks");
        chips.setExpirationDaysDelta(365);
        chips.setOwner(user);
        repository.save(chips);

        Product cookie = new Product();
        cookie.setName("Cookie");
        cookie.setCategory("Snacks");
        cookie.setExpirationDaysDelta(200);
        cookie.setOwner(user);
        repository.save(cookie);

        Product apple = new Product();
        apple.setName("Apple");
        apple.setCategory("Fruit");
        apple.setExpirationDaysDelta(20);
        apple.setOwner(user);
        repository.save(apple);

        Product banana = new Product();
        banana.setName("Banana");
        banana.setCategory("Fruit");
        banana.setExpirationDaysDelta(10);
        banana.setOwner(user);
        repository.save(banana);

        Product adminProduct = new Product();
        adminProduct.setName("Milk");
        adminProduct.setCategory("Dairy");
        adminProduct.setBarcode("4567");
        adminProduct.setExpirationDaysDelta(90);
        adminProduct.setOwner(userRepository.findByEmail("admin@test.test").get());
        repository.save(adminProduct);
    }

    @Override
    public boolean shouldSeed() {
        if (userRepository.findByEmail("test@test.test").isEmpty())
            return false;

        return repository.count() == 0;
    }
}
