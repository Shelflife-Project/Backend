package com.shelflife.project.storageservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageRepository;
import com.shelflife.project.service.StorageService;
import com.shelflife.project.service.UserService;

@ExtendWith(MockitoExtension.class)
public class GetStoragesTests {
    @Mock
    private UserService userService;

    @Mock
    private StorageRepository storageRepository;

    @InjectMocks
    private StorageService storageService;

    Authentication auth;

    @Test
    void throwsAccessDeniedAsAnonymous() {
        doThrow(AccessDeniedException.class).when(userService).getUserByAuth(auth);

        assertThrows(AccessDeniedException.class, () -> storageService.getStorages(auth));
    }

    @Test
    void returnsEveryStorageAsAdmin() {
        User admin = new User();
        admin.setAdmin(true);

        List<Storage> all = List.of(new Storage(), new Storage());

        doReturn(admin).when(userService).getUserByAuth(auth);
        when(storageRepository.findAll()).thenReturn(all);

        List<Storage> result = storageService.getStorages(auth);

        assertEquals(2, result.size());
        verify(storageRepository).findAll();
        verify(storageRepository, never()).findAccessibleStorages(anyLong());
    }

    @Test
    void returnOwnedAndMemberStoragesAsUser() {
        User user = new User();
        user.setId(1);
        user.setAdmin(false);

        List<Storage> accessible = List.of(new Storage());

        doReturn(user).when(userService).getUserByAuth(auth);
        when(storageRepository.findAccessibleStorages(1)).thenReturn(accessible);

        List<Storage> result = storageService.getStorages(auth);

        assertEquals(1, result.size());
        verify(storageRepository).findAccessibleStorages(1L);
        verify(storageRepository, never()).findAll();
    }
}
