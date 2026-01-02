package com.shelflife.project.storageservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.StorageItem;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageItemRepository;
import com.shelflife.project.repository.StorageRepository;
import com.shelflife.project.repository.UserRepository;
import com.shelflife.project.service.StorageService;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class GetExpiredItemsTests {
    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private StorageItemRepository storageItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StorageService storageService;

    Authentication auth;

    private User user;
    private Storage storage;
    private Product product;
    private StorageItem storageItem;

    @BeforeEach
    void setup() {
        user = new User();
        user.setUsername("test");
        user.setEmail("test@storageTest.test");
        user.setAdmin(false);
        user.setPassword("pass");

        storage = new Storage();
        storage.setOwner(user);
        storage.setName("test");

        product = new Product();
        product.setOwner(user);
        product.setBarcode("12345");
        product.setCategory("test");
        product.setName("test");
        product.setRunningLow(1);
        product.setExpirationDaysDelta(1);

        storageItem = new StorageItem();
        storageItem.setStorage(storage);
        storageItem.setProduct(product);
        storageItem.setExpiresAt(LocalDateTime.now().plusDays(10));

        storage.getItems().add(storageItem);

        user.getOwnedStorages().add(storage);
        user.getOwnedProducts().add(product);

        user = userRepository.save(user);

        userRepository.flush();
        storageRepository.flush();
        storageItemRepository.flush();
    }

    @Test
    void throwsNotFound() {
        assertThrows(ItemNotFoundException.class, () -> storageService.getExpiredItemsInStorage(storage.getId() - 1));
    }

    @Test
    void returnsOnlyExpired() {
        StorageItem item = new StorageItem();
        item.setStorage(storage);
        item.setProduct(product);
        item.setExpiresAt(LocalDateTime.now().minusDays(1));

        storage.getItems().add(item);
        storageRepository.save(storage);

        storageRepository.flush();
        storageItemRepository.flush();

        assertDoesNotThrow(() -> storageService.getExpiredItemsInStorage(storage.getId()));
        assertEquals(2, storageService.getItemsInStorage(storage.getId()).size());
        assertEquals(1, storageService.getExpiredItemsInStorage(storage.getId()).size());
        assertEquals(item.getExpiresAt(),
                storageService.getExpiredItemsInStorage(storage.getId()).get(0).getExpiresAt());
    }

    @Test
    void returnsExpiresTomorrow() {
        StorageItem item = new StorageItem();
        item.setStorage(storage);
        item.setProduct(product);
        item.setExpiresAt(LocalDateTime.now().plusDays(1));

        storage.getItems().add(item);
        storageRepository.save(storage);

        storageRepository.flush();
        storageItemRepository.flush();

        assertDoesNotThrow(() -> storageService.getExpiredItemsInStorage(storage.getId()));
        assertEquals(2, storageService.getItemsInStorage(storage.getId()).size());
        assertEquals(1, storageService.getItemsAboutToExpire(storage.getId()).size());
        assertEquals(item.getExpiresAt(),
                storageService.getItemsAboutToExpire(storage.getId()).get(0).getExpiresAt());
    }
}
