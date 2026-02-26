package com.shelflife.project.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.shelflife.project.dto.runninglow.RunningLowNotification;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.RunningLowSetting;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.StorageItem;
import com.shelflife.project.model.User;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RunningLowRepositoryFindItemsRunningLowTests {
    @Autowired
    private RunningLowRepository rlRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StorageItemRepository siRepository;

    User user;
    Product product;
    Storage storage;
    StorageItem item;
    RunningLowSetting setting;

    @BeforeEach
    void setup() {
        user = new User();
        user.setEmail("test@test.test");
        user.setUsername("test");
        user.setAdmin(false);
        user.setPassword("12345");
        userRepository.save(user);

        product = new Product();
        product.setName("test");
        product.setCategory("test");
        product.setOwner(user);
        product.setExpirationDaysDelta(100);
        productRepository.save(product);

        storage = new Storage();
        storage.setName("test");
        storage.setOwner(user);
        storageRepository.save(storage);

        item = new StorageItem();
        item.setExpiresAt(LocalDate.now().plusDays(100));
        item.setProduct(product);
        item.setStorage(storage);
        siRepository.save(item);

        setting = new RunningLowSetting();
        setting.setProduct(product);
        setting.setStorage(storage);
        setting.setRunningLow(1);
        rlRepository.save(setting);
    }

    @Test
    void returnsNotificationForRunningLow() {
        List<RunningLowNotification> notifications = rlRepository.findItemsRunningLow(storage.getId());

        assertEquals(1, notifications.size());
        assertEquals(notifications.get(0).getProduct().getId(), product.getId());
    }

    @Test
    void returnsOneNotificationForRunningLowWithTwoItems() {
        StorageItem other = new StorageItem();
        other.setExpiresAt(LocalDate.now().plusDays(100));
        other.setProduct(product);
        other.setStorage(storage);
        siRepository.save(other);

        setting.setRunningLow(2);
        rlRepository.save(setting);

        List<RunningLowNotification> notifications = rlRepository.findItemsRunningLow(storage.getId());

        assertEquals(1, notifications.size());
        assertEquals(notifications.get(0).getAmount(), 2);
    }

    @Test
    void returnsNothingForRunningLow() {
        StorageItem extraItem = new StorageItem();
        extraItem.setExpiresAt(LocalDate.now().plusDays(100));
        extraItem.setProduct(product);
        extraItem.setStorage(storage);
        siRepository.save(extraItem);

        List<RunningLowNotification> notifications = rlRepository.findItemsRunningLow(storage.getId());

        assertEquals(0, notifications.size());
    }

    @Test
    void returnsMultipleNotificationsForRunningLow() {
        Product extraProduct = new Product();
        extraProduct.setName("other");
        extraProduct.setCategory("other");
        extraProduct.setOwner(user);
        productRepository.save(extraProduct);

        RunningLowSetting extraSetting = new RunningLowSetting();
        extraSetting.setProduct(extraProduct);
        extraSetting.setStorage(storage);
        extraSetting.setRunningLow(1);
        rlRepository.save(extraSetting);

        StorageItem extraItem = new StorageItem();
        extraItem.setExpiresAt(LocalDate.now().plusDays(100));
        extraItem.setProduct(extraProduct);
        extraItem.setStorage(storage);
        siRepository.save(extraItem);

        List<RunningLowNotification> notifications = rlRepository.findItemsRunningLow(storage.getId());

        assertEquals(2, notifications.size());
        assertEquals(notifications.get(0).getProduct().getId(), product.getId());
        assertEquals(notifications.get(0).getAmount(), 1);
        assertEquals(notifications.get(1).getAmount(), 1);
    }

    @Test
    void returnsNotificationWhileNoItemsInStorage() {
        siRepository.delete(item);

        List<RunningLowNotification> notifications = rlRepository.findItemsRunningLow(storage.getId());

        assertEquals(1, notifications.size());
        assertEquals(notifications.get(0).getProduct().getId(), product.getId());
        assertEquals(notifications.get(0).getAmount(), 0);
    }

    @Test
    void returnsOneNotification_ForRunningLow_WithoutOtherSetting() {
        Product extraProduct = new Product();
        extraProduct.setName("other");
        extraProduct.setCategory("other");
        extraProduct.setOwner(user);
        productRepository.save(extraProduct);

        StorageItem extraItem = new StorageItem();
        extraItem.setExpiresAt(LocalDate.now().plusDays(100));
        extraItem.setProduct(extraProduct);
        extraItem.setStorage(storage);
        siRepository.save(extraItem);

        List<RunningLowNotification> notifications = rlRepository.findItemsRunningLow(storage.getId());

        assertEquals(1, notifications.size());
        assertEquals(notifications.get(0).getProduct().getId(), product.getId());
    }

    @Test
    void returnsOnlyFromStorage() {
        Storage otherStorage = new Storage();
        otherStorage.setName("Other");
        otherStorage.setOwner(user);
        otherStorage = storageRepository.save(otherStorage);

        List<RunningLowNotification> notifications = rlRepository.findItemsRunningLow(otherStorage.getId());

        assertEquals(0, notifications.size());
    }
}
