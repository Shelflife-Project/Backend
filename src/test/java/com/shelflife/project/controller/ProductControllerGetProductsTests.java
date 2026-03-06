package com.shelflife.project.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.shelflife.project.model.Product;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ProductRepository;
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
public class ProductControllerGetProductsTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ProductRepository productRepository;

    private User testAdmin;
    private User testUser;

    private Product testProduct;
    private Product otherProduct;

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

        testProduct = new Product();
        testProduct.setName("Chips");
        testProduct.setOwner(testUser);
        testProduct.setExpirationDaysDelta(200);
        testProduct.setCategory("Snack");
        testProduct.setBarcode("12345");
        testProduct = productRepository.save(testProduct);

        otherProduct = new Product();
        otherProduct.setName("Test");
        otherProduct.setOwner(testUser);
        otherProduct.setExpirationDaysDelta(100);
        otherProduct.setCategory("Snack");
        otherProduct.setBarcode("1234567");
        otherProduct = productRepository.save(otherProduct);
    }

    @Test
    void getProductsAsUser() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(get("/api/products")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(testProduct.getId()))
                .andExpect(jsonPath("$[0].name").value(testProduct.getName()))
                .andExpect(jsonPath("$[0].barcode").value(testProduct.getBarcode()))
                .andExpect(jsonPath("$[0].category").value(testProduct.getCategory()))
                .andExpect(jsonPath("$[0].ownerId").value(testProduct.getOwnerId()))
                .andExpect(jsonPath("$[0].expirationDaysDelta").value(testProduct.getExpirationDaysDelta()));
    }

    @Test
    void getProductsAsAdmin() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(get("/api/products")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(testProduct.getId()))
                .andExpect(jsonPath("$[0].name").value(testProduct.getName()))
                .andExpect(jsonPath("$[0].barcode").value(testProduct.getBarcode()))
                .andExpect(jsonPath("$[0].category").value(testProduct.getCategory()))
                .andExpect(jsonPath("$[0].ownerId").value(testProduct.getOwnerId()))
                .andExpect(jsonPath("$[0].expirationDaysDelta").value(testProduct.getExpirationDaysDelta()));
    }

    @Test
    void cantGetProductsAsAnonymous() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    void getProductsPaginated() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(get("/api/products?page=0&size=1")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testProduct.getId()))
                .andExpect(jsonPath("$[0].name").value(testProduct.getName()))
                .andExpect(jsonPath("$[0].barcode").value(testProduct.getBarcode()))
                .andExpect(jsonPath("$[0].category").value(testProduct.getCategory()))
                .andExpect(jsonPath("$[0].ownerId").value(testProduct.getOwnerId()))
                .andExpect(jsonPath("$[0].expirationDaysDelta").value(testProduct.getExpirationDaysDelta()));
    }
}
