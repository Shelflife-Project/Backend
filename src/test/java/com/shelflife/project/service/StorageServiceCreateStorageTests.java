package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

import com.shelflife.project.dto.storage.CreateStorageRequest;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageRepository;

@ExtendWith(MockitoExtension.class)
public class StorageServiceCreateStorageTests {
    @Mock
    private StorageRepository storageRepository;

    @Spy
    @InjectMocks
    private StorageService storageService;

    @Test
    void successfulCreation() {
        User user = new User();
        CreateStorageRequest request = new CreateStorageRequest();
        request.setName("test");

        when(storageRepository.save(any(Storage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Storage storage = storageService.createStorage(request, user);
        assertNotNull(storage);
        assertEquals(user, storage.getOwner());
        assertEquals("test", storage.getName());

        verify(storageRepository).save(any(Storage.class));
    }

    @Test
    void emptyNameErrorForNull() {
        User user = new User();

        CreateStorageRequest request = new CreateStorageRequest();
        request.setName(null);

        assertThrows(IllegalArgumentException.class, () -> storageService.createStorage(request, user));
        verifyNoInteractions(storageRepository);
    }

    @Test
    void emptyNameErrorForBlank() {
        User user = new User();

        CreateStorageRequest request = new CreateStorageRequest();
        request.setName("");

        assertThrows(IllegalArgumentException.class, () -> storageService.createStorage(request, user));
        verifyNoInteractions(storageRepository);
    }

    @Test
    void throwsAccessDeniedWithNull() {
        CreateStorageRequest request = new CreateStorageRequest();
        request.setName("test");

        assertThrows(AccessDeniedException.class, () -> storageService.createStorage(request, null));
        verifyNoInteractions(storageRepository);
    }
}
