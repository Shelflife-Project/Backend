package com.shelflife.project.storagecontroller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageRepository;
import com.shelflife.project.repository.UserRepository;
import com.shelflife.project.service.JwtService;

import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class GetStoragesTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private StorageRepository storageRepository;

    private User testAdmin;
    private User testUser;

    private Storage testUserStorage;
    private Storage testAdminStorage;

    @BeforeEach
    void setup() {
        testAdmin = new User();
        testAdmin.setEmail("test@test.test");
        testAdmin.setUsername("test");
        testAdmin.setPassword(encoder.encode("test123"));
        testAdmin.setAdmin(true);
        testAdmin = userRepository.save(testAdmin);

        testUser = new User();
        testUser.setEmail("testuser@test.test");
        testUser.setUsername("testuser");
        testUser.setPassword(encoder.encode("test123"));
        testUser.setAdmin(false);
        testUser = userRepository.save(testUser);

        testUserStorage = new Storage();
        testUserStorage.setOwner(testUser);
        testUserStorage.setName("userTest");
        testUserStorage = storageRepository.save(testUserStorage);

        testAdminStorage = new Storage();
        testAdminStorage.setOwner(testAdmin);
        testAdminStorage.setName("adminTest");
        testAdminStorage = storageRepository.save(testAdminStorage);
    }

    @Test
    void getStoragesAsUser() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(get("/api/storages")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testUserStorage.getId()))
                .andExpect(jsonPath("$[0].name").value(testUserStorage.getName()))
                .andExpect(jsonPath("$[0].owner.id").value(testUserStorage.getOwner().getId()));
    }

    @Test
    void getStoragesAsAdmin() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(get("/api/storages")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(testUserStorage.getId()))
                .andExpect(jsonPath("$[1].id").value(testAdminStorage.getId()));
    }

    @Test
    void cantGetStoragesAsAnonymous() throws Exception {
        mockMvc.perform(get("/api/storages"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }
}
