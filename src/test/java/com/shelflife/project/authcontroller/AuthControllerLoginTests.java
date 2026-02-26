package com.shelflife.project.authcontroller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.UserRepository;

import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerLoginTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = new User();
        testUser.setEmail("test@test.test");
        testUser.setUsername("test");
        testUser.setPassword(encoder.encode("test123"));
        userRepository.save(testUser);
    }

    @Test
    void loginSuccessful() throws Exception {
        String token = loginAndGetToken(testUser.getEmail(), "test123");
        Cookie jwtCookie = new Cookie("jwt", token);

        mockMvc.perform(get("/api/auth/me")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.username").value(testUser.getUsername()))
                .andExpect(jsonPath("$.admin").value(testUser.isAdmin()));
    }

    @Test
    void loginInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getLoginJson("test1@est.test", "test123")))
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist("jwt"))
                                .andExpect(jsonPath("$.token").doesNotExist())
                .andReturn();

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginInvalidPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getLoginJson("test@test.test", "test1234")))
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist("jwt"))
                                .andExpect(jsonPath("$.token").doesNotExist())
                .andReturn();

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist("jwt"))
                                .andExpect(jsonPath("$.token").doesNotExist())
                .andReturn();

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cantLoginMultipleTimes() throws Exception {
        String token = loginAndGetToken("test@test.test", "test123");
        Cookie jwtCookie = new Cookie("jwt", token);

        mockMvc.perform(get("/api/auth/me")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.username").value(testUser.getUsername()))
                .andExpect(jsonPath("$.admin").value(testUser.isAdmin()));

        mockMvc.perform(post("/api/auth/login")
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getLoginJson("test@test.test", "test123")))
                .andExpect(status().isForbidden());
    }

        private String loginAndGetToken(String email, String password) throws Exception {
                MvcResult result = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(getLoginJson(email, password)))
                                .andExpect(status().isOk())
                                .andExpect(cookie().exists("jwt"))
                                .andExpect(jsonPath("$.token").exists())
                                .andReturn();

                Map<String, String> response = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Map.class);
                return response.get("token");
        }

    String getLoginJson(String email, String password) {
                return "{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}";
    }
}
