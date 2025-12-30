package com.shelflife.project.storageservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
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

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageMemberRepository;
import com.shelflife.project.repository.StorageRepository;
import com.shelflife.project.service.StorageService;
import com.shelflife.project.service.UserService;

@ExtendWith(MockitoExtension.class)
public class GetStorageTests {
    @Mock
    private UserService userService;

    @Mock
    private StorageMemberRepository storageMemberRepository;

    @Mock
    private StorageRepository storageRepository;

    @InjectMocks
    private StorageService storageService;

    Authentication auth;

    @Test
    void noAuth_throwsNotFound() {
        when(storageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> storageService.getStorage(1));
    }

    @Test
    void noAuth_returnsStorage() {
        Storage storage = new Storage();
        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));

        assertDoesNotThrow(() -> storageService.getStorage(1));
        assertEquals(storage, storageService.getStorage(1));
    }

    @Test
    void auth_throwsAccessDeniedAsAnonymous() {
        doThrow(AccessDeniedException.class).when(userService).getUserByAuth(auth);
        assertThrows(AccessDeniedException.class, () -> storageService.getStorage(auth, 1L));
        verifyNoInteractions(storageRepository);
    }

    @Test
    void auth_throwsAccessDeniedAsNonMember() {
        User user = new User();
        user.setId(1);
        user.setAdmin(false);

        User owner = new User();
        owner.setId(2);
        owner.setAdmin(false);

        Storage storage = new Storage();
        storage.setId(1);
        storage.setOwner(owner);

        doReturn(user).when(userService).getUserByAuth(auth);
        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));
        when(userService.getUserById(1L)).thenReturn(user);

        assertThrows(AccessDeniedException.class, () -> storageService.getStorage(auth, 1));
        verify(storageMemberRepository).existsByStorageIdAndUserId(1, 1);
    }

    @Test
    void auth_returnsStorageAsOwner() {
        User owner = new User();
        owner.setId(1);
        owner.setAdmin(false);

        Storage storage = new Storage();
        storage.setId(1);
        storage.setOwner(owner);

        doReturn(owner).when(userService).getUserByAuth(auth);
        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));
        when(userService.getUserById(1L)).thenReturn(owner);

        assertDoesNotThrow(() -> storageService.getStorage(auth, 1));
        assertEquals(storage, storageService.getStorage(1));

        verifyNoInteractions(storageMemberRepository);
    }

    @Test
    void auth_returnsStorageAsMember() {
        User user = new User();
        user.setId(1);
        user.setAdmin(false);

        User owner = new User();
        owner.setId(2);
        owner.setAdmin(false);

        Storage storage = new Storage();
        storage.setId(1);
        storage.setOwner(owner);

        doReturn(user).when(userService).getUserByAuth(auth);
        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));
        when(userService.getUserById(1L)).thenReturn(user);
        when(storageMemberRepository.existsByStorageIdAndUserId(1L, 1L)).thenReturn(true);

        assertDoesNotThrow(() -> storageService.getStorage(auth, 1));
        assertEquals(storage, storageService.getStorage(1));

        verify(storageMemberRepository).existsByStorageIdAndUserId(1, 1);
    }

    @Test
    void auth_returnsStorageAsAdmin() {
        User admin = new User();
        admin.setId(1);
        admin.setAdmin(true);

        User owner = new User();
        owner.setId(2);
        owner.setAdmin(false);

        Storage storage = new Storage();
        storage.setId(1);
        storage.setOwner(owner);

        doReturn(admin).when(userService).getUserByAuth(auth);
        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));
        when(userService.getUserById(1L)).thenReturn(admin);

        assertDoesNotThrow(() -> storageService.getStorage(auth, 1));
        assertEquals(storage, storageService.getStorage(1));

        verifyNoInteractions(storageMemberRepository);
    }
}
