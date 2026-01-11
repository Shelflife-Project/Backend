package com.shelflife.project.storagememberservice;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
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
import org.springframework.security.core.Authentication;

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageMemberRepository;
import com.shelflife.project.repository.StorageRepository;
import com.shelflife.project.service.StorageMemberService;
import com.shelflife.project.service.UserService;

@ExtendWith(MockitoExtension.class)
public class CanAccessStorageTests {
    @Mock
    private UserService userService;

    @Mock
    private StorageMemberRepository storageMemberRepository;

    @Mock
    private StorageRepository storageRepository;

    @Spy
    @InjectMocks
    private StorageMemberService service;

    Authentication auth;

    @Test
    void noauth_returnsFalseAsAnonymous() {
        when(userService.getUserById(1)).thenThrow(ItemNotFoundException.class);

        assertFalse(service.canAccessStorage(1L, 1L));
        verifyNoInteractions(storageRepository);
        verifyNoInteractions(storageMemberRepository);
    }

    @Test
    void noauth_returnsFalseAsNonOwner() {
        User user = new User();
        user.setId(1);

        User owner = new User();
        owner.setId(2);

        Storage storage = new Storage();
        storage.setOwner(owner);

        when(userService.getUserById(1)).thenReturn(user);
        when(storageMemberRepository.isMember(1, 1)).thenReturn(false);
        doReturn(storage).when(service).getStorage(1);

        assertFalse(service.canAccessStorage(1L, 1L));
    }

    @Test
    void noauth_returnsTrueAsOwner() {
        User owner = new User();
        owner.setId(1);

        Storage storage = new Storage();
        storage.setOwner(owner);

        when(userService.getUserById(1)).thenReturn(owner);
        doReturn(storage).when(service).getStorage(1);

        assertTrue(service.canAccessStorage(1L, 1L));
        verifyNoInteractions(storageMemberRepository);
    }

    @Test
    void noauth_returnsTrueAsMember() {
        User user = new User();
        user.setId(1);

        User owner = new User();
        owner.setId(2);

        Storage storage = new Storage();
        storage.setOwner(owner);

        when(userService.getUserById(1)).thenReturn(user);
        when(storageMemberRepository.isMember(1L, 1L)).thenReturn(true);
        doReturn(storage).when(service).getStorage(1);

        assertTrue(service.canAccessStorage(1L, 1L));
        verify(storageMemberRepository).isMember(1L, 1L);
    }

    @Test
    void noauth_returnsTrueAsAdmin() {
        User user = new User();
        user.setId(1);
        user.setAdmin(true);

        User owner = new User();
        owner.setId(2);

        Storage storage = new Storage();
        storage.setOwner(owner);

        when(userService.getUserById(1)).thenReturn(user);
        doReturn(storage).when(service).getStorage(1);

        assertTrue(service.canAccessStorage(1L, 1L));
        verifyNoInteractions(storageMemberRepository);
    }

    //auth
    @Test
    void auth_returnsFalseAsAnonymous() {
        doThrow(AccessDeniedException.class).when(userService).getUserByAuth(auth);

        assertFalse(service.canAccessStorage(1L, auth));
        verifyNoInteractions(storageRepository);
        verifyNoInteractions(storageMemberRepository);
    }

    @Test
    void auth_returnsFalseAsNonOwner() {
        User user = new User();
        user.setId(1);

        User owner = new User();
        owner.setId(2);

        Storage storage = new Storage();
        storage.setOwner(owner);

        doReturn(user).when(userService).getUserByAuth(auth);
        when(storageMemberRepository.isMember(1, 1)).thenReturn(false);
        doReturn(storage).when(service).getStorage(1);

        assertFalse(service.canAccessStorage(1L, auth));
    }

    @Test
    void auth_returnsTrueAsOwner() {
        User owner = new User();
        owner.setId(1);

        Storage storage = new Storage();
        storage.setOwner(owner);

        doReturn(owner).when(userService).getUserByAuth(auth);
        doReturn(storage).when(service).getStorage(1);

        assertTrue(service.canAccessStorage(1L, auth));
        verifyNoInteractions(storageMemberRepository);
    }

    @Test
    void auth_returnsTrueAsMember() {
        User user = new User();
        user.setId(1);

        User owner = new User();
        owner.setId(2);

        Storage storage = new Storage();
        storage.setOwner(owner);

        doReturn(user).when(userService).getUserByAuth(auth);
        when(storageMemberRepository.isMember(1L, 1L)).thenReturn(true);
        doReturn(storage).when(service).getStorage(1);

        assertTrue(service.canAccessStorage(1L, auth));
        verify(storageMemberRepository).isMember(1L, 1L);
    }

    @Test
    void auth_returnsTrueAsAdmin() {
        User user = new User();
        user.setId(1);
        user.setAdmin(true);

        User owner = new User();
        owner.setId(2);

        Storage storage = new Storage();
        storage.setOwner(owner);

        doReturn(user).when(userService).getUserByAuth(auth);
        doReturn(storage).when(service).getStorage(1);

        assertTrue(service.canAccessStorage(1L, auth));
        verifyNoInteractions(storageMemberRepository);
    }

}
