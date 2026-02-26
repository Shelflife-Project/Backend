package com.shelflife.project.service;

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

import com.shelflife.project.dto.storage.CreateStorageRequest;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageRepository;

@ExtendWith(MockitoExtension.class)
public class StorageServiceCreateStorageTests {
    @Mock
    private UserService userService;

    @Mock
    private StorageRepository storageRepository;

    @Spy
    @InjectMocks
    private StorageService storageService;

    Authentication auth;

    @Test
    void successfulCreation() {
        User user = new User();
        CreateStorageRequest request = new CreateStorageRequest();
        request.setName("test");

        doReturn(user).when(userService).getUserByAuth(auth);
        when(storageRepository.save(any(Storage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Storage storage = storageService.createStorage(request, auth);
        assertNotNull(storage);
        assertEquals(user, storage.getOwner());
        assertEquals("test", storage.getName());

        verify(storageRepository).save(any(Storage.class));
    }

    @Test
    void emptyNameErrorForNull() {
        CreateStorageRequest request = new CreateStorageRequest();
        request.setName(null);

        assertThrows(IllegalArgumentException.class, () -> storageService.createStorage(request, auth));
        verifyNoInteractions(storageRepository);
    }

    @Test
    void emptyNameErrorForBlank() {
        CreateStorageRequest request = new CreateStorageRequest();
        request.setName("");

        assertThrows(IllegalArgumentException.class, () -> storageService.createStorage(request, auth));
        verifyNoInteractions(storageRepository);
    }

    @Test
    void throwsAccessDeniedForAnonymous() {
        CreateStorageRequest request = new CreateStorageRequest();
        request.setName("test");

        doThrow(AccessDeniedException.class).when(userService).getUserByAuth(auth);

        assertThrows(AccessDeniedException.class, () -> storageService.createStorage(request, auth));
        verifyNoInteractions(storageRepository);
    }
}
