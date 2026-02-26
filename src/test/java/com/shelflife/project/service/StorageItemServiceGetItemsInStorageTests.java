package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.StorageItem;
import com.shelflife.project.repository.StorageItemRepository;
import com.shelflife.project.repository.StorageRepository;

@ExtendWith(MockitoExtension.class)
public class StorageItemServiceGetItemsInStorageTests {
    @Mock
    private UserService userService;

    @Mock
    private StorageItemRepository storageItemRepository;

    @Mock
    private StorageRepository storageRepository;

    @Mock
    private StorageAccessService storageAccessService;

    @InjectMocks
    private StorageItemService service;

    Authentication auth;

    @Test
    void noauth_throwsNotFound() {
        when(storageRepository.existsById(1L)).thenReturn(false);

        assertThrows(ItemNotFoundException.class, () -> service.getItemsInStorage(1));
    }

    @Test
    void noauth_returnsItems() {
        List<StorageItem> items = new ArrayList<>();
        items.add(new StorageItem());
        items.add(new StorageItem());

        when(storageRepository.existsById(1L)).thenReturn(true);
        when(storageItemRepository.findByStorageId(1L)).thenReturn(items);

        assertDoesNotThrow(() -> service.getItemsInStorage(1));
        assertEquals(items, service.getItemsInStorage(1));
    }

    @Test
    void auth_throwsAccessDenied() {
        doReturn(false).when(storageAccessService).canAccessStorage(1, auth);

        assertThrows(AccessDeniedException.class, () -> service.getItemsInStorage(1, auth));
    }
}
