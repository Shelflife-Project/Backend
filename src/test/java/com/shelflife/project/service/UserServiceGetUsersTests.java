package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.shelflife.project.model.User;
import com.shelflife.project.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceGetUsersTests {

    @Mock
    UserRepository repo;

    @InjectMocks
    UserService service;

    @Test
    void returnsUsersAsAdmin() {
        User user = new User();
        user.setAdmin(true);

        assertDoesNotThrow(() -> {
            service.getUsers(user);
        });

        verify(repo).findAll();
    }

    @Test
    void throwsAccessDeniedAsUser() {
        User user = new User();
        user.setAdmin(false);

        assertThrows(AccessDeniedException.class, () -> {
            service.getUsers(user);
        });

        verifyNoInteractions(repo);
    }

    @Test
    void throwsAccessDeniedAsAnonymous() {
        User user = new User();
        user.setAdmin(false);

        assertThrows(AccessDeniedException.class, () -> {
            service.getUsers(user);
        });

        verifyNoInteractions(repo);
    }

    @Test
    void throwsAccessDeniedWithNull() {
        assertThrows(AccessDeniedException.class, () -> {
            service.getUsers(null);
        });

        verifyNoInteractions(repo);
    }
}
