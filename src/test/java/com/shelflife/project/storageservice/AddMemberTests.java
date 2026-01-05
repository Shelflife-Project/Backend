package com.shelflife.project.storageservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import com.shelflife.project.exception.MemberException;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.StorageMember;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageMemberRepository;
import com.shelflife.project.repository.StorageRepository;
import com.shelflife.project.service.StorageService;
import com.shelflife.project.service.UserService;

@ExtendWith(MockitoExtension.class)
public class AddMemberTests {
    @Mock
    private UserService userService;

    @Mock
    private StorageMemberRepository storageMemberRepository;

    @Mock
    private StorageRepository storageRepository;

    @Spy
    @InjectMocks
    private StorageService storageService;

    private Authentication auth;

    @Test
    void throwsItemNotFoundForStorage() {
        doThrow(ItemNotFoundException.class).when(storageService).getStorage(1L);
        assertThrows(ItemNotFoundException.class, () -> storageService.addMemberToStorage(1, "test", auth));
    }

    @Test
    void throwsItemNotFoundForTarget() {
        Storage storage = new Storage();
        storage.setId(1);

        doReturn(storage).when(storageService).getStorage(1L);
        doThrow(ItemNotFoundException.class).when(userService).getUserByEmail("test");

        assertThrows(ItemNotFoundException.class, () -> storageService.addMemberToStorage(1, "test", auth));
    }

    @Test
    void throwsAccessDeniedAsAnonymous() {
        Storage storage = new Storage();
        storage.setId(1);

        User target = new User();
        target.setId(1);

        doReturn(storage).when(storageService).getStorage(1L);
        doReturn(target).when(userService).getUserByEmail("test");
        doThrow(AccessDeniedException.class).when(userService).getUserByAuth(auth);

        assertThrows(AccessDeniedException.class, () -> storageService.addMemberToStorage(1, "test", auth));
    }

    @Test
    void throwsAccessDeniedAsNonOwner() {
        Storage storage = new Storage();
        storage.setId(1);

        User target = new User();
        target.setId(1);

        User other = new User();
        other.setId(2);

        User owner = new User();
        owner.setId(3);
        storage.setOwner(owner);

        doReturn(storage).when(storageService).getStorage(1L);
        doReturn(target).when(userService).getUserByEmail("test");
        doReturn(other).when(userService).getUserByAuth(auth);

        assertThrows(AccessDeniedException.class, () -> storageService.addMemberToStorage(1, "test", auth));
    }

    @Test
    void successfulAddAsAdmin() {
        Storage storage = new Storage();
        User target = new User();

        User other = new User();
        other.setId(2);
        other.setAdmin(true);

        doReturn(storage).when(storageService).getStorage(1L);
        doReturn(target).when(userService).getUserByEmail("test");
        doReturn(other).when(userService).getUserByAuth(auth);

        when(storageMemberRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        StorageMember result = storageService.addMemberToStorage(1, "test", auth);
        assertNotNull(result);
        assertEquals(storage, result.getStorage());
        assertEquals(target, result.getUser());
    }

    @Test
    void successfulAddAsOwner() {
        Storage storage = new Storage();
        storage.setId(1);

        User target = new User();
        target.setId(1);

        User owner = new User();
        owner.setId(2);
        storage.setOwner(owner);

        when(storageMemberRepository.existsByStorageIdAndUserId(1, 1)).thenReturn(false);
        doReturn(storage).when(storageService).getStorage(1);
        doReturn(target).when(userService).getUserByEmail("test");
        doReturn(owner).when(userService).getUserByAuth(auth);

        when(storageMemberRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        StorageMember result = storageService.addMemberToStorage(1, "test", auth);
        assertNotNull(result);
        assertEquals(storage, result.getStorage());
        assertEquals(target, result.getUser());
    }

    @Test
    void throwsMemberExceptionForDuplicateAdd() {
        Storage storage = new Storage();
        storage.setId(1);

        User target = new User();
        target.setId(1);

        doReturn(storage).when(storageService).getStorage(1L);
        doReturn(target).when(userService).getUserByEmail("test");
        when(storageMemberRepository.existsByStorageIdAndUserId(1, 1)).thenReturn(true);

        assertThrows(MemberException.class, () -> storageService.addMemberToStorage(1, "test", auth));
        verify(storageMemberRepository, never()).save(any(StorageMember.class));
    }
}
