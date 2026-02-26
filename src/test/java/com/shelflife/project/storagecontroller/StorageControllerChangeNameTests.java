package com.shelflife.project.storagecontroller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class StorageControllerChangeNameTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private JwtService jwtService;

    private User testAdmin;
    private User testUser;

    private Storage testUserStorage;
    private Storage testAdminStorage;

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
    void successfulRenameAsOwner() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/storages/" + testUserStorage.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"newName\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserStorage.getId()))
                .andExpect(jsonPath("$.name").value("newName"))
                .andExpect(jsonPath("$.owner.id").value(testUser.getId()));
    }

    @Test
    void successfulRenameAsAdmin() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/storages/" + testUserStorage.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"newName\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserStorage.getId()))
                .andExpect(jsonPath("$.name").value("newName"))
                .andExpect(jsonPath("$.owner.id").value(testUser.getId()));
    }

    @Test
    void accessDeniedAsNonOwner() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/storages/" + testAdminStorage.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"newName\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void notFound() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/storages/" + testAdminStorage.getId() + 1)
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"newName\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void accessDeniedAsAnonymous() throws Exception {
        mockMvc.perform(patch("/api/storages/" + testUserStorage.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void emptyNameError() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/storages/" + testUserStorage.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("name").exists());
    }

    @Test
    void nullNameError() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/storages/" + testUserStorage.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("name").exists());
    }

    @Test
    void whitespaceNameError() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/storages/" + testUserStorage.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("name").exists());
    }
}
