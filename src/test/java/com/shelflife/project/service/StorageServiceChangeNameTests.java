package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
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

import com.shelflife.project.dto.storage.ChangeStorageNameRequest;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageRepository;

@ExtendWith(MockitoExtension.class)
public class StorageServiceChangeNameTests {
    @Mock
    private StorageRepository storageRepository;

    @Mock
    private StorageGetterService storageGetterService;

    @Spy
    @InjectMocks
    private StorageService storageService;

    @Test
    void successfulAsOwner() {
        User owner = new User();
        owner.setId(1);

        Storage testStorage = new Storage();
        testStorage.setName("old");
        testStorage.setOwner(owner);

        ChangeStorageNameRequest request = new ChangeStorageNameRequest();
        request.setName("new");

        doReturn(testStorage).when(storageGetterService).getStorage(1);
        when(storageRepository.save(any(Storage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Storage storage = storageService.changeName(1, request, owner);
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

        doReturn(testStorage).when(storageGetterService).getStorage(1);
        when(storageRepository.save(any(Storage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Storage storage = storageService.changeName(1, request, admin);
        assertNotNull(storage);
        assertEquals("new", storage.getName());

        verify(storageRepository).save(any(Storage.class));
    }

    @Test
    void emptyNameErrorForNull() {
        User user = new User();

        ChangeStorageNameRequest request = new ChangeStorageNameRequest();
        request.setName(null);

        assertThrows(IllegalArgumentException.class, () -> storageService.changeName(1, request, user));
        verifyNoInteractions(storageRepository);
    }

    @Test
    void emptyNameErrorForBlank() {
        User user = new User();

        ChangeStorageNameRequest request = new ChangeStorageNameRequest();
        request.setName("");

        assertThrows(IllegalArgumentException.class, () -> storageService.changeName(1, request, user));
        verifyNoInteractions(storageRepository);
    }

    @Test
    void throwsAccessDeniedWithNull() {
        ChangeStorageNameRequest request = new ChangeStorageNameRequest();
        request.setName("test");

        assertThrows(AccessDeniedException.class, () -> storageService.changeName(1, request, null));
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

        doReturn(testStorage).when(storageGetterService).getStorage(1);

        assertThrows(AccessDeniedException.class, () -> storageService.changeName(1, request, user));
        verifyNoInteractions(storageRepository);
    }
}
