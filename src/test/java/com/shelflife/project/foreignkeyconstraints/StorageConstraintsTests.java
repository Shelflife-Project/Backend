package com.shelflife.project.foreignkeyconstraints;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

import com.shelflife.project.model.Product;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.StorageItem;
import com.shelflife.project.model.StorageMember;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ProductRepository;
import com.shelflife.project.repository.StorageItemRepository;
import com.shelflife.project.repository.StorageMemberRepository;
import com.shelflife.project.repository.StorageRepository;
import com.shelflife.project.repository.UserRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class StorageConstraintsTests {

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private StorageItemRepository storageItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StorageMemberRepository storageMemberRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Storage storage;
    private Product product;
    private StorageItem storageItem;
    private StorageMember member;

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
        storageItem.setExpiresAt(LocalDate.now().plusDays(1));
        storageItem.setCreatedAt(LocalDateTime.now());

        member = new StorageMember();
        member.setStorage(storage);
        member.setUser(user);

        storage.getMembers().add(member);
        storage.getItems().add(storageItem);

        user.getOwnedStorages().add(storage);
        user.getMembersIn().add(member);
        user.getOwnedProducts().add(product);

        user = userRepository.save(user);

        userRepository.flush();
        storageRepository.flush();
        storageItemRepository.flush();
        storageMemberRepository.flush();

    }

    @Test
    void storageGetsRemovedWithUser() {
        assertTrue(storageRepository.findById(storage.getId()).isPresent());

        userRepository.deleteById(user.getId());

        assertFalse(userRepository.findById(user.getId()).isPresent());
        assertFalse(storageRepository.findById(storage.getId()).isPresent());
    }

    @Test
    void storageItemGetsRemovedWithStorage() {
        assertTrue(storageItemRepository.findById(storageItem.getId()).isPresent());

        storageRepository.deleteById(storage.getId());

        assertFalse(storageRepository.findById(storage.getId()).isPresent());
        assertFalse(storageItemRepository.findById(storageItem.getId()).isPresent());
    }

    @Test
    void storageMemberGetsRemovedWithStorage() {
        assertTrue(storageMemberRepository.findById(member.getId()).isPresent());

        storageRepository.deleteById(storage.getId());

        assertFalse(storageRepository.findById(storage.getId()).isPresent());
        assertFalse(storageMemberRepository.findById(member.getId()).isPresent());
    }

    @Test
    void storageMemberGetsRemovedWithUser() {
        assertTrue(storageMemberRepository.findById(member.getId()).isPresent());

        userRepository.deleteById(user.getId());

        assertFalse(userRepository.findById(user.getId()).isPresent());
        assertFalse(storageMemberRepository.findById(member.getId()).isPresent());
    }

    @Test
    void productGetsRemovedWithOwner() {
        assertTrue(productRepository.findById(product.getId()).isPresent());

        userRepository.deleteById(user.getId());

        assertFalse(userRepository.findById(user.getId()).isPresent());
        assertFalse(productRepository.findById(product.getId()).isPresent());
    }
}
