package com.shelflife.project.storageitemservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

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
import com.shelflife.project.service.StorageItemService;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class StorageItemServiceGetExpiredItemsTests {
    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private StorageItemRepository storageItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StorageItemService service;

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
        product.setExpirationDaysDelta(1);

        storageItem = new StorageItem();
        storageItem.setStorage(storage);
        storageItem.setProduct(product);
        storageItem.setExpiresAt(LocalDate.now().plusDays(10));

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
        assertThrows(ItemNotFoundException.class, () -> service.getExpiredItemsInStorage(storage.getId() - 1));
    }

    @Test
    void returnsOnlyExpired() {
        StorageItem item = new StorageItem();
        item.setStorage(storage);
        item.setProduct(product);
        item.setExpiresAt(LocalDate.now().minusDays(1));

        storage.getItems().add(item);
        storageRepository.save(storage);

        storageRepository.flush();
        storageItemRepository.flush();

        assertDoesNotThrow(() -> service.getExpiredItemsInStorage(storage.getId()));
        assertEquals(2, service.getItemsInStorage(storage.getId()).size());
        assertEquals(1, service.getExpiredItemsInStorage(storage.getId()).size());
        assertEquals(item.getExpiresAt(),
                service.getExpiredItemsInStorage(storage.getId()).get(0).getExpiresAt());
    }

    @Test
    void returnsExpiresTomorrow() {
        StorageItem item = new StorageItem();
        item.setStorage(storage);
        item.setProduct(product);
        item.setExpiresAt(LocalDate.now());

        storage.getItems().add(item);
        storageRepository.save(storage);

        storageRepository.flush();
        storageItemRepository.flush();

        assertDoesNotThrow(() -> service.getExpiredItemsInStorage(storage.getId()));
        assertEquals(2, service.getItemsInStorage(storage.getId()).size());
        assertEquals(1, service.getItemsAboutToExpire(storage.getId()).size());
        assertEquals(item.getExpiresAt(),
                service.getItemsAboutToExpire(storage.getId()).get(0).getExpiresAt());
    }
}
