package com.shelflife.project.itemcontroller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
import com.shelflife.project.service.JwtService;

import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class GetAboutToExpireTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private StorageMemberRepository storageMemberRepository;

    @Autowired
    private StorageItemRepository storageItemRepository;

    @Autowired
    private ProductRepository productRepository;

    private User testAdmin;
    private User testUser;
    private User testMember;

    private Storage testUserStorage;
    private Storage testAdminStorage;

    private StorageMember testMemberObj;

    private Product testProduct;
    private StorageItem testExpiredItem;
    private StorageItem testItem;

    @BeforeEach
    void setup() {
        testAdmin = new User();
        testAdmin.setEmail("test@test.test");
        testAdmin.setUsername("test");
        testAdmin.setPassword("test123");
        testAdmin.setAdmin(true);
        testAdmin = userRepository.save(testAdmin);

        testUser = new User();
        testUser.setEmail("testuser@test.test");
        testUser.setUsername("testuser");
        testUser.setPassword("test123");
        testUser.setAdmin(false);
        testUser = userRepository.save(testUser);

        testMember = new User();
        testMember.setEmail("testmember@test.test");
        testMember.setUsername("testmember");
        testMember.setPassword("test123");
        testMember.setAdmin(false);
        testMember = userRepository.save(testMember);

        testUserStorage = new Storage();
        testUserStorage.setOwner(testUser);
        testUserStorage.setName("userTest");
        testUserStorage = storageRepository.save(testUserStorage);

        testMemberObj = new StorageMember();
        testMemberObj.setStorage(testUserStorage);
        testMemberObj.setUser(testMember);
        testMemberObj.setAccepted(true);
        testMemberObj = storageMemberRepository.save(testMemberObj);

        testAdminStorage = new Storage();
        testAdminStorage.setOwner(testAdmin);
        testAdminStorage.setName("adminTest");
        testAdminStorage = storageRepository.save(testAdminStorage);

        testProduct = new Product();
        testProduct.setName("test");
        testProduct.setOwner(testAdmin);
        testProduct.setCategory("cat");
        testProduct.setRunningLow(2);
        testProduct.setExpirationDaysDelta(2);
        testProduct = productRepository.save(testProduct);

        testExpiredItem = new StorageItem();
        testExpiredItem.setProduct(testProduct);
        testExpiredItem.setStorage(testUserStorage);
        testExpiredItem.setExpiresAt(LocalDate.now());
        testExpiredItem = storageItemRepository.save(testExpiredItem);

        testItem = new StorageItem();
        testItem.setProduct(testProduct);
        testItem.setStorage(testUserStorage);
        testItem.setExpiresAt(LocalDate.now().plusDays(2));
        testItem = storageItemRepository.save(testItem);
    }

    @Test
    void successfulGetItemsAsOwner() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(get("/api/storages/" + testUserStorage.getId() + "/abouttoexpire")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testExpiredItem.getId()))
                .andExpect(jsonPath("$[0].product.id").value(testProduct.getId()));
    }

    @Test
    void successfulGetItemsAsAdmin() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(get("/api/storages/" + testUserStorage.getId() + "/abouttoexpire")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testExpiredItem.getId()))
                .andExpect(jsonPath("$[0].product.id").value(testProduct.getId()));
    }

    @Test
    void successfulGetItemsAsMember() throws Exception {
        String jwt = jwtService.generateToken(testMember.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(get("/api/storages/" + testUserStorage.getId() + "/abouttoexpire")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testExpiredItem.getId()))
                .andExpect(jsonPath("$[0].product.id").value(testProduct.getId()));
    }

    @Test
    void accessDeniedAsAnonymous() throws Exception {
        mockMvc.perform(get("/api/storages/" + testUserStorage.getId() + "/abouttoexpire"))
                .andExpect(status().isForbidden());
    }
}
