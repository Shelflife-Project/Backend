package com.shelflife.project.seed;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.shelflife.project.model.Product;
import com.shelflife.project.model.StorageItem;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ProductRepository;
import com.shelflife.project.repository.StorageItemRepository;
import com.shelflife.project.repository.StorageRepository;
import com.shelflife.project.repository.UserRepository;

@Component
public class StorageItemSeeder implements Seeder {
    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private StorageItemRepository storageItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public void seed() {
        User user = userRepository.findByEmail("test@test.test").get();
        User admin = userRepository.findByEmail("admin@test.test").get();

        Product bread = productRepository.searchProducts("Bread", Pageable.unpaged()).toList().get(0);
        Product milk = productRepository.searchProducts("Milk", Pageable.unpaged()).toList().get(0);

        StorageItem userItem = new StorageItem();
        userItem.setProduct(bread);
        userItem.setStorage(storageRepository.findByOwnerId(user.getId()).get(0));
        userItem.setExpiresAt(LocalDate.now().plusDays(bread.getExpirationDaysDelta()));

        storageItemRepository.save(userItem);

        StorageItem adminItem = new StorageItem();
        adminItem.setProduct(milk);
        adminItem.setStorage(storageRepository.findByOwnerId(admin.getId()).get(0));
        adminItem.setExpiresAt(LocalDate.now().plusDays(milk.getExpirationDaysDelta()));

        storageItemRepository.save(adminItem);
    }

    @Override
    public boolean shouldSeed() {
        if (userRepository.findByEmail("test@test.test").isEmpty())
            return false;

        if (userRepository.findByEmail("admin@test.test").isEmpty())
            return false;

        if (productRepository.searchProducts("Bread", Pageable.unpaged()).toList().isEmpty())
            return false;

        if (productRepository.searchProducts("Milk", Pageable.unpaged()).toList().isEmpty())
            return false;

        return storageItemRepository.count() == 0;
    }
}
