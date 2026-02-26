package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageRepository;

@ExtendWith(MockitoExtension.class)
public class StorageServiceDeleteStorageRequestTests {
    @Mock
    private UserService userService;

    @Mock
    private StorageRepository storageRepository;

    @Mock
    private StorageGetterService storageGetterService;

    @Mock
    private StorageMemberService storageMemberService;

    @Spy
    @InjectMocks
    private StorageService storageService;

    Authentication auth;

    @Test
    void deletesStorageAsAdmin() {
        User admin = new User();
        admin.setId(1);
        admin.setAdmin(true);

        Storage storage = new Storage();

        doReturn(admin).when(userService).getUserByAuth(auth);
        doReturn(storage).when(storageGetterService).getStorage(1);

        assertDoesNotThrow(() -> storageService.deleteStorageRequest(1, auth));
        verify(storageRepository).deleteById(1L);
    }

    @Test
    void deletesStorageAsOwner() {
        User owner = new User();
        owner.setId(1);
        owner.setAdmin(false);

        Storage storage = new Storage();
        storage.setOwner(owner);

        doReturn(owner).when(userService).getUserByAuth(auth);
        doReturn(storage).when(storageGetterService).getStorage(1);

        assertDoesNotThrow(() -> storageService.deleteStorageRequest(1, auth));
        verify(storageRepository).deleteById(1L);
    }

    @Test
    void leavesStorageAsMember() {
        User owner = new User();
        owner.setId(1);
        owner.setAdmin(false);

        User member = new User();
        member.setId(2);
        member.setAdmin(false);

        Storage storage = new Storage();
        storage.setOwner(owner);

        doReturn(member).when(userService).getUserByAuth(auth);
        doReturn(storage).when(storageGetterService).getStorage(1);
        doReturn(true).when(storageMemberService).isMemberOfStorage(1, 2);

        assertDoesNotThrow(() -> storageService.deleteStorageRequest(1, auth));
        verify(storageMemberService).removeMemberFromStorage(1, 2);
        verifyNoInteractions(storageRepository);
    }

    @Test
    void accessDeniedAsNonMember() {
        User owner = new User();
        owner.setId(1);
        owner.setAdmin(false);

        User user = new User();
        user.setId(2);

        Storage storage = new Storage();
        storage.setOwner(owner);

        doReturn(user).when(userService).getUserByAuth(auth);
        doReturn(storage).when(storageGetterService).getStorage(1);

        assertThrows(AccessDeniedException.class, () -> storageService.deleteStorageRequest(1, auth));
        verify(storageMemberService, only()).isMemberOfStorage(1, 2);
        verifyNoInteractions(storageRepository);
    }

    @Test
    void accessDeniedAsAnonymous() {
        doThrow(AccessDeniedException.class).when(userService).getUserByAuth(auth);

        assertThrows(AccessDeniedException.class, () -> storageService.deleteStorageRequest(1, auth));
        verifyNoInteractions(storageMemberService);
        verifyNoInteractions(storageRepository);
    }
}
