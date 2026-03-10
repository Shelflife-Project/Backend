package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceRemoveUserTests {
    @Mock
    UserRepository repo;

    @Mock
    PasswordEncoder encoder;

    @Spy
    @InjectMocks
    UserService service;

    @Test
    void throwsAccessDeniedWithNull() {
        assertThrows(AccessDeniedException.class, () -> service.removeUser(1, null));
        verifyNoInteractions(repo);
    }

    @Test
    void throwsAccessDeniedAsUser() {
        User user = new User();
        user.setAdmin(false);

        assertThrows(AccessDeniedException.class, () -> service.removeUser(1, user));
        verifyNoInteractions(repo);
    }

    @Test
    void throwsAccessDeniedAsSameAdmin() {
        User user = new User();
        user.setId(1);
        user.setAdmin(true);

        assertThrows(AccessDeniedException.class, () -> service.removeUser(1, user));
        verifyNoInteractions(repo);
    }

    @Test
    void throwsItemNotFound() {
        User user = new User();
        user.setId(1);
        user.setAdmin(true);

        assertThrows(ItemNotFoundException.class, () -> service.removeUser(2, user));
        verify(repo, never()).deleteById(any());
    }

    @Test
    void successfulDelete() {
        User user = new User();
        user.setId(1);
        user.setAdmin(true);

        User userToDelete = new User();
        userToDelete.setId(2);

        when(repo.existsById(2L)).thenReturn(true);

        assertDoesNotThrow(() -> service.removeUser(2, user));
        verify(repo).deleteById(2L);
    }
}
