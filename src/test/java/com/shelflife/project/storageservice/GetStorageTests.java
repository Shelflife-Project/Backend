package com.shelflife.project.storageservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Storage;
import com.shelflife.project.repository.StorageRepository;
import com.shelflife.project.service.StorageService;
import com.shelflife.project.service.UserService;

@ExtendWith(MockitoExtension.class)
public class GetStorageTests {
    @Mock
    private UserService userService;

    @Mock
    private StorageRepository storageRepository;

    @InjectMocks
    private StorageService storageService;

    Authentication auth;

    @Test
    void throwsNotFound() {
        when(storageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> storageService.getStorage(1));
    }

    @Test
    void returnsStorage() {
        Storage storage = new Storage();
        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));

        assertDoesNotThrow(() -> storageService.getStorage(1));
        assertEquals(storage, storageService.getStorage(1));
    }
}
