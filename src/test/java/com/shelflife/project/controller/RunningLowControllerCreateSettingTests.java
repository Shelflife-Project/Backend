package com.shelflife.project.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.shelflife.project.model.Product;
import com.shelflife.project.model.RunningLowSetting;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.StorageMember;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ProductRepository;
import com.shelflife.project.repository.RunningLowRepository;
import com.shelflife.project.repository.StorageMemberRepository;
import com.shelflife.project.repository.StorageRepository;
import com.shelflife.project.repository.UserRepository;
import com.shelflife.project.service.JwtService;

import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class RunningLowControllerCreateSettingTests {
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
    private ProductRepository productRepository;

    @Autowired
    private RunningLowRepository runningLowRepository;

    private User testAdmin;
    private User testUser;
    private User testMember;

    private Storage testUserStorage;

    private StorageMember testMemberObj;

    private Product testProduct;

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

        testProduct = new Product();
        testProduct.setName("test");
        testProduct.setOwner(testAdmin);
        testProduct.setCategory("cat");
        testProduct.setExpirationDaysDelta(2);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void successfulAddAsOwner() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(post("/api/storages/" + testUserStorage.getId() + "/runninglowsettings")
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson(testProduct.getId(), 5)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.product.id").value(testProduct.getId()))
                .andExpect(jsonPath("$.runningLow").value(5));
    }

    @Test
    void successfulAddAsAdmin() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(post("/api/storages/" + testUserStorage.getId() + "/runninglowsettings")
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson(testProduct.getId(), 5)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.product.id").value(testProduct.getId()))
                .andExpect(jsonPath("$.runningLow").value(5));
    }

    @Test
    void successfulAddAsMember() throws Exception {
        String jwt = jwtService.generateToken(testMember.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(post("/api/storages/" + testUserStorage.getId() + "/runninglowsettings")
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson(testProduct.getId(), 5)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.product.id").value(testProduct.getId()))
                .andExpect(jsonPath("$.runningLow").value(5));
    }

    @Test
    void settingExists() throws Exception {
        String jwt = jwtService.generateToken(testMember.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        RunningLowSetting setting = new RunningLowSetting();
        setting.setProduct(testProduct);
        setting.setStorage(testUserStorage);
        setting.setRunningLow(3);
        runningLowRepository.save(setting);

        mockMvc.perform(post("/api/storages/" + testUserStorage.getId() + "/runninglowsettings")
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson(testProduct.getId(), 5)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.productId").exists());
    }

    @Test
    void invalidRunningLow() throws Exception {
        String jwt = jwtService.generateToken(testMember.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(post("/api/storages/" + testUserStorage.getId() + "/runninglowsettings")
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson(testProduct.getId(), -5)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.runningLow").exists());
    }

    @Test
    void accessDeniedAsAnonymous() throws Exception {
        mockMvc.perform(post("/api/storages/" + testUserStorage.getId() + "/runninglowsettings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson(testProduct.getId(), 5)))
                .andExpect(status().isForbidden());
    }

    private String validJson(final long productId, final long runningLow) {
        return "{\"productId\":" + productId + ", \"runningLow\":" + runningLow + "}";
    }
}
