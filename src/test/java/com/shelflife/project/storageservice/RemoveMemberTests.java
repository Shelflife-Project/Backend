package com.shelflife.project.storageservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.shelflife.project.model.StorageMember;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageMemberRepository;
import com.shelflife.project.repository.StorageRepository;
import com.shelflife.project.service.StorageMemberService;
import com.shelflife.project.service.UserService;

@ExtendWith(MockitoExtension.class)
public class RemoveMemberTests {
    @Mock
    private UserService userService;

    @Mock
    private StorageMemberRepository storageMemberRepository;

    @Mock
    private StorageRepository storageRepository;

    @Spy
    @InjectMocks
    private StorageMemberService service;

    private Authentication auth;

    @Test
    void throwsItemNotFoundForMember() {
        when(storageMemberRepository.findByStorageIdAndUserId(1, 1)).thenReturn(Optional.empty());
        assertThrows(ItemNotFoundException.class, () -> service.removeMemberFromStorage(1, 1, auth));
    }

    @Test
    void throwsItemNotFoundForStorage() {
        User user = new User();

        when(storageMemberRepository.findByStorageIdAndUserId(1, 1)).thenReturn(Optional.of(new StorageMember()));
        when(storageRepository.findById(1L)).thenReturn(Optional.empty());
        doReturn(user).when(userService).getUserByAuth(auth);

        assertThrows(ItemNotFoundException.class, () -> service.removeMemberFromStorage(1, 1, auth));
    }

    @Test
    void throwsAccessDeniedAsAnonymous() {
        when(storageMemberRepository.findByStorageIdAndUserId(1, 1)).thenReturn(Optional.of(new StorageMember()));
        doThrow(AccessDeniedException.class).when(userService).getUserByAuth(auth);
        assertThrows(AccessDeniedException.class, () -> service.removeMemberFromStorage(1, 1, auth));
    }

    @Test
    void throwsAccessDeniedAsNonOwner() {
        Storage storage = new Storage();
        storage.setId(1);

        User owner = new User();
        owner.setId(3);
        storage.setOwner(owner);

        User user = new User();
        user.setId(2);

        when(storageMemberRepository.findByStorageIdAndUserId(1, 1)).thenReturn(Optional.of(new StorageMember()));
        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));
        doReturn(user).when(userService).getUserByAuth(auth);

        assertThrows(AccessDeniedException.class, () -> service.removeMemberFromStorage(1, 1, auth));
    }

    @Test
    void successfulRemoveAsAdmin() {
        Storage storage = new Storage();
        User memberUser = new User();
        memberUser.setId(1);

        User owner = new User();
        owner.setId(3);
        storage.setOwner(owner);

        StorageMember member = new StorageMember();
        member.setId(10);
        member.setUser(memberUser);
        member.setStorage(storage);

        User admin = new User();
        admin.setId(2);
        admin.setAdmin(true);

        when(storageMemberRepository.findByStorageIdAndUserId(1, 1)).thenReturn(Optional.of(member));
        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));
        doReturn(admin).when(userService).getUserByAuth(auth);

        assertDoesNotThrow(() -> service.removeMemberFromStorage(1, 1, auth));
        verify(storageMemberRepository).deleteById(10L);
    }

    @Test
    void successfulRemoveAsOwner() {
        Storage storage = new Storage();
        User memberUser = new User();
        memberUser.setId(1);

        StorageMember member = new StorageMember();
        member.setId(10);
        member.setUser(memberUser);
        member.setStorage(storage);

        User owner = new User();
        owner.setId(2);
        owner.setAdmin(true);
        storage.setOwner(owner);

        when(storageMemberRepository.findByStorageIdAndUserId(1, 1)).thenReturn(Optional.of(member));
        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));
        doReturn(owner).when(userService).getUserByAuth(auth);

        assertDoesNotThrow(() -> service.removeMemberFromStorage(1, 1, auth));
        verify(storageMemberRepository).deleteById(10L);
    }
}
