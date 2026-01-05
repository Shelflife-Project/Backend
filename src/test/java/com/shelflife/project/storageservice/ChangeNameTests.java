package com.shelflife.project.storageservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

import com.shelflife.project.dto.ChangeStorageNameRequest;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageRepository;
import com.shelflife.project.service.StorageService;
import com.shelflife.project.service.UserService;

@ExtendWith(MockitoExtension.class)
public class ChangeNameTests {
    @Mock
    private UserService userService;

    @Mock
    private StorageRepository storageRepository;

    @Spy
    @InjectMocks
    private StorageService storageService;

    Authentication auth;

    @Test
    void successfulAsOwner() {
        User owner = new User();
        owner.setId(1);

        Storage testStorage = new Storage();
        testStorage.setName("old");
        testStorage.setOwner(owner);

        ChangeStorageNameRequest request = new ChangeStorageNameRequest();
        request.setName("new");

        doReturn(owner).when(userService).getUserByAuth(auth);
        doReturn(testStorage).when(storageService).getStorage(1);
        when(storageRepository.save(any(Storage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Storage storage = storageService.changeName(1, request, auth);
        assertNotNull(storage);
        assertEquals("new", storage.getName());

        verify(storageRepository).save(any(Storage.class));
    }

    @Test
    void successfulAsAdmin() {
        User owner = new User();
        owner.setId(1);

        User admin = new User();
        admin.setAdmin(true);
        admin.setId(2);

        Storage testStorage = new Storage();
        testStorage.setName("old");
        testStorage.setOwner(owner);

        ChangeStorageNameRequest request = new ChangeStorageNameRequest();
        request.setName("new");

        doReturn(admin).when(userService).getUserByAuth(auth);
        doReturn(testStorage).when(storageService).getStorage(1);
        when(storageRepository.save(any(Storage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Storage storage = storageService.changeName(1, request, auth);
        assertNotNull(storage);
        assertEquals("new", storage.getName());

        verify(storageRepository).save(any(Storage.class));
    }

    @Test
    void emptyNameErrorForNull() {
        ChangeStorageNameRequest request = new ChangeStorageNameRequest();
        request.setName(null);

        assertThrows(IllegalArgumentException.class, () -> storageService.changeName(1, request, auth));
        verifyNoInteractions(storageRepository);
    }

    @Test
    void emptyNameErrorForBlank() {
        ChangeStorageNameRequest request = new ChangeStorageNameRequest();
        request.setName("");

        assertThrows(IllegalArgumentException.class, () -> storageService.changeName(1, request, auth));
        verifyNoInteractions(storageRepository);
    }

    @Test
    void throwsAccessDeniedForAnonymous() {
        ChangeStorageNameRequest request = new ChangeStorageNameRequest();
        request.setName("test");

        doThrow(AccessDeniedException.class).when(userService).getUserByAuth(auth);

        assertThrows(AccessDeniedException.class, () -> storageService.changeName(1, request, auth));
        verifyNoInteractions(storageRepository);
    }

    @Test
    void throwsAccessDeniedForNonOwner() {
        User owner = new User();
        owner.setId(1);

        User user = new User();
        user.setAdmin(false);
        user.setId(2);

        Storage testStorage = new Storage();
        testStorage.setName("old");
        testStorage.setOwner(owner);

        ChangeStorageNameRequest request = new ChangeStorageNameRequest();
        request.setName("new");

        doReturn(user).when(userService).getUserByAuth(auth);
        doReturn(testStorage).when(storageService).getStorage(1);

        assertThrows(AccessDeniedException.class, () -> storageService.changeName(1, request, auth));
        verifyNoInteractions(storageRepository);
    }
}
