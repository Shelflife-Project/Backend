package com.shelflife.project.service;

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

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.exception.MemberException;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.StorageMember;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageMemberRepository;
import com.shelflife.project.repository.StorageRepository;

@ExtendWith(MockitoExtension.class)
public class StorageMemberServiceInviteMemberTests {
    @Mock
    private UserService userService;

    @Mock
    private StorageMemberRepository storageMemberRepository;

    @Mock
    private StorageRepository storageRepository;

    @Spy
    @InjectMocks
    private StorageMemberService service;

    private User user = new User();

    @Test
    void throwsItemNotFoundForStorage() {
        doThrow(ItemNotFoundException.class).when(service).getStorage(1L);
        assertThrows(ItemNotFoundException.class, () -> service.inviteMemberToStorage(1, "test", user));
    }

    @Test
    void throwsItemNotFoundForTarget() {
        Storage storage = new Storage();
        storage.setId(1);

        doReturn(storage).when(service).getStorage(1L);
        doThrow(ItemNotFoundException.class).when(userService).getUserByEmail("test");

        assertThrows(ItemNotFoundException.class, () -> service.inviteMemberToStorage(1, "test", user));
    }

    @Test
    void throwsAccessDeniedWithNull() {
        Storage storage = new Storage();
        storage.setId(1);

        User target = new User();
        target.setId(1);

        assertThrows(AccessDeniedException.class, () -> service.inviteMemberToStorage(1, "test", null));
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

        doReturn(storage).when(service).getStorage(1L);
        doReturn(target).when(userService).getUserByEmail("test");

        assertThrows(AccessDeniedException.class, () -> service.inviteMemberToStorage(1, "test", other));
    }

    @Test
    void successfulAddAsAdmin() {
        User owner = new User();
        owner.setId(1);
        owner.setAdmin(false);

        Storage storage = new Storage();
        storage.setId(1);
        storage.setOwner(owner);

        User target = new User();
        target.setId(2);

        User admin = new User();
        admin.setId(3);
        admin.setAdmin(true);

        doReturn(storage).when(service).getStorage(1L);
        doReturn(target).when(userService).getUserByEmail("test");

        when(storageMemberRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        StorageMember result = service.inviteMemberToStorage(1, "test", admin);
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
        doReturn(storage).when(service).getStorage(1);
        doReturn(target).when(userService).getUserByEmail("test");

        when(storageMemberRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        StorageMember result = service.inviteMemberToStorage(1, "test", owner);
        assertNotNull(result);
        assertEquals(storage, result.getStorage());
        assertEquals(target, result.getUser());
    }

    @Test
    void throwsMemberExceptionForDuplicateAdd() {
        User owner = new User();
        owner.setId(1);

        Storage storage = new Storage();
        storage.setId(1);
        storage.setOwner(owner);

        User target = new User();
        target.setId(2);

        doReturn(storage).when(service).getStorage(1L);
        doReturn(target).when(userService).getUserByEmail("test");
        when(storageMemberRepository.existsByStorageIdAndUserId(1, 2)).thenReturn(true);

        assertThrows(MemberException.class, () -> service.inviteMemberToStorage(1, "test", owner));
        verify(storageMemberRepository, never()).save(any(StorageMember.class));
    }

    @Test
    void throwsMemberExceptionForSelfAddAsOwner() {
        User owner = new User();
        owner.setId(1);
        owner.setAdmin(false);
        
        Storage storage = new Storage();
        storage.setId(1);
        storage.setOwner(owner);

        User target = new User();
        target.setId(1);

        doReturn(storage).when(service).getStorage(1L);
        doReturn(target).when(userService).getUserByEmail("test");

        assertThrows(MemberException.class, () -> service.inviteMemberToStorage(1, "test", owner));
        verify(storageMemberRepository, never()).save(any(StorageMember.class));
    }

    @Test
    void throwsMemberExceptionForAdmins() {
        User user = new User();
        user.setId(1);
        user.setAdmin(true);
        
        Storage storage = new Storage();
        storage.setId(1);

        doReturn(storage).when(service).getStorage(1L);
        doReturn(user).when(userService).getUserByEmail("test");

        assertThrows(MemberException.class, () -> service.inviteMemberToStorage(1, "test", user));
        verify(storageMemberRepository, never()).save(any(StorageMember.class));
    }
}
