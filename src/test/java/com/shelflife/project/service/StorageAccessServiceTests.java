package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageRepository;

@ExtendWith(MockitoExtension.class)
public class StorageAccessServiceTests {
    @Mock
    private StorageRepository storageRepository;

    @Mock
    private UserService userService;

    @Spy
    @InjectMocks
    private StorageAccessService storageAccessService;

    private Authentication auth;

    @Test
    void noAuth_returnsTrue_asAdmin() {
        doReturn(true).when(userService).isAdmin(1);
        assertTrue(storageAccessService.canAccessStorage(1, 1));
    }

    @Test
    void noAuth_returnsTrue_asMember() {
        doReturn(false).when(userService).isAdmin(1);
        doReturn(true).when(storageRepository).isMemberOrOwner(1, 1);

        assertTrue(storageAccessService.canAccessStorage(1, 1));
    }

    @Test
    void noAuth_returnsFalse() {
        doReturn(false).when(userService).isAdmin(1);
        doReturn(false).when(storageRepository).isMemberOrOwner(1, 1);

        assertFalse(storageAccessService.canAccessStorage(1, 1));
    }

    @Test
    void auth_returnsFalseAsAnonymous() {
        doThrow(AccessDeniedException.class).when(userService).getUserByAuth(auth);
        assertFalse(storageAccessService.canAccessStorage(1, auth));
    }

    @Test
    void auth_callsCanAccessStorage() {
        User user = new User();
        user.setId(1);

        doReturn(user).when(userService).getUserByAuth(auth);
        storageAccessService.canAccessStorage(1, auth);

        verify(storageAccessService).canAccessStorage(1, 1);
    }
}
