package com.shelflife.project.productcontroller;

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
public class GetProductsByNameTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ProductRepository productRepository;

    private User testUser;

    private Product testProduct;
    private Product otherTestProduct;

    @BeforeEach
    void setup() {
        testUser = new User();
        testUser.setEmail("testuser@test.test");
        testUser.setUsername("testuser");
        testUser.setPassword("test123");
        testUser.setAdmin(false);
        testUser = userRepository.save(testUser);

        testProduct = new Product();
        testProduct.setName("Chips");
        testProduct.setOwner(testUser);
        testProduct.setRunningLow(2);
        testProduct.setExpirationDaysDelta(200);
        testProduct.setCategory("Snack");
        testProduct.setBarcode("12345");
        testProduct = productRepository.save(testProduct);

        otherTestProduct = new Product();
        otherTestProduct.setName("Milk");
        otherTestProduct.setOwner(testUser);
        otherTestProduct.setRunningLow(2);
        otherTestProduct.setExpirationDaysDelta(20);
        otherTestProduct.setCategory("Dairy");
        otherTestProduct.setBarcode("6789");
        otherTestProduct = productRepository.save(otherTestProduct);
    }

    @Test
    void getProductsWithPartialName() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(get("/api/products?name=i")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(testProduct.getId()))
                .andExpect(jsonPath("$[1].id").value(otherTestProduct.getId()));
    }

    @Test
    void getProductsWithFullName() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(get("/api/products?name=" + testProduct.getName())
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testProduct.getId()));
    }

    @Test
    void returnsZero() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(get("/api/products?name=notReal")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void cantGetProductsAsAnonymous() throws Exception {
        mockMvc.perform(get("/api/products?name=" + testProduct.getName()))
                .andExpect(status().isForbidden());
    }
}
