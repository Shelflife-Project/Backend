package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageRepository;

@ExtendWith(MockitoExtension.class)
public class StorageGetterServiceTests {
    @Mock
    private StorageRepository storageRepository;

    @Mock
    private UserService userService;

    @Spy
    @InjectMocks
    private StorageGetterService storageGetterService;

    private Authentication auth;

    @Test
    void noAuth_getStorage_throwsNotFound() {
        doReturn(Optional.empty()).when(storageRepository).findById(1L);

        assertThrows(ItemNotFoundException.class, () -> {
            storageGetterService.getStorage(1);
        });
    }

    @Test
    void noAuth_getStorage_returnsStorage() {
        doReturn(Optional.of(new Storage())).when(storageRepository).findById(1L);

        assertDoesNotThrow(() -> {
            storageGetterService.getStorage(1);
        });
    }

    @Test
    void auth_getStorage_throwsAccessDeniedForAnonymous() {
        doThrow(AccessDeniedException.class).when(userService).getUserByAuth(auth);

        assertThrows(AccessDeniedException.class, () -> {
            storageGetterService.getStorage(auth, 1);
        });
    }

    @Test
    void auth_getStorage_throwsAccessDeniedForNonMember() {
        User user = new User();
        user.setId(1);
        user.setAdmin(false);

        doReturn(user).when(userService).getUserByAuth(auth);
        doReturn(false).when(storageRepository).isMemberOrOwner(1, 1);

        assertThrows(AccessDeniedException.class, () -> {
            storageGetterService.getStorage(auth, 1);
        });
    }

    @Test
    void auth_getStorage_returnsStorageAsAdmin() {
        User user = new User();
        user.setAdmin(true);

        doReturn(user).when(userService).getUserByAuth(auth);
        doReturn(new Storage()).when(storageGetterService).getStorage(1);

        assertDoesNotThrow(() -> {
            storageGetterService.getStorage(auth, 1);
        });

        verify(storageGetterService).getStorage(1);
    }

    @Test
    void auth_getStorage_returnsStorageAsMember() {
        User user = new User();
        user.setId(1);
        user.setAdmin(false);

        doReturn(user).when(userService).getUserByAuth(auth);
        doReturn(true).when(storageRepository).isMemberOrOwner(1, 1);
        doReturn(new Storage()).when(storageGetterService).getStorage(1);

        assertDoesNotThrow(() -> {
            storageGetterService.getStorage(auth, 1);
        });

        verify(storageGetterService).getStorage(1);
    }

    @Test
    void getStorages_throwsAccessDeniedAsAnonymous() {
        doThrow(AccessDeniedException.class).when(userService).getUserByAuth(auth);

        assertThrows(AccessDeniedException.class, () -> {
            storageGetterService.getStorages(auth);
        });
    }

    @Test
    void getStorages_returnsAllAsAdmin() {
        User user = new User();
        user.setAdmin(true);

        doReturn(user).when(userService).getUserByAuth(auth);

        assertDoesNotThrow(() -> {
            storageGetterService.getStorages(auth);
        });

        verify(storageGetterService).getStorages();
    }

    @Test
    void getStorages_returnsAccessibleAsNonAdmin() {
        User user = new User();
        user.setId(1);
        user.setAdmin(false);

        doReturn(user).when(userService).getUserByAuth(auth);

        assertDoesNotThrow(() -> {
            storageGetterService.getStorages(auth);
        });

        verify(storageRepository).findAccessibleStorages(user.getId());
    }
}
