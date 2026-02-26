package com.shelflife.project.userservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import com.shelflife.project.model.User;
import com.shelflife.project.repository.UserRepository;
import com.shelflife.project.service.UserService;

@ExtendWith(MockitoExtension.class)
public class UserServiceGetUserByAuthTests {

    @Mock
    UserRepository repo;

    @InjectMocks
    UserService service;

    @Mock
    Authentication authentication;

    @Test
    void returnsEmptyWhenAuthIsNull() {
        assertThrows(AccessDeniedException.class, () -> service.getUserByAuth(null));
        verifyNoInteractions(repo);
    }

    @Test
    void returnsEmptyWhenAuthIsNotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.getUserByAuth(authentication));
        verifyNoInteractions(repo);
    }

    @Test
    void returnsEmptyWhenNotInDB() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@test.test");

        assertThrows(AccessDeniedException.class, () -> service.getUserByAuth(authentication));
    }

    @Test
    void returnsUserWhenAuthenticated() {
        String email = "test@test.test";

        User user = new User();
        user.setEmail(email);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(email);
        when(repo.findByEmail(email))
                .thenReturn(Optional.of(user));

        User result = service.getUserByAuth(authentication);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }
}
