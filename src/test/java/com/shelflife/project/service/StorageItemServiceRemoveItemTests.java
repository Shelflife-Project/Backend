package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
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

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.StorageItem;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageItemRepository;

@ExtendWith(MockitoExtension.class)
public class StorageItemServiceRemoveItemTests {
    @Mock
    private StorageItemRepository storageItemRepository;

    @Mock
    private StorageAccessService storageAccessService;

    @Spy
    @InjectMocks
    private StorageItemService storageItemService;

    @Test
    void successfulRemove() {
        User user = new User();
        user.setId(1);

        Storage storage = new Storage();
        storage.setId(2);

        StorageItem item = new StorageItem();
        item.setId(3);
        item.setStorage(storage);

        when(storageItemRepository.findById(3L)).thenReturn(Optional.of(item));

        doReturn(true).when(storageAccessService).canAccessStorage(2, user);

        storageItemService.removeItemFromStorage(3, user);

        verify(storageItemRepository).deleteById(3L);
    }

    @Test
    void throwsItemNotFound() {
        when(storageItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> storageItemService.removeItemFromStorage(1L, new User()));

        verify(storageItemRepository, never()).deleteById(anyLong());
    }

    @Test
    void throwsAccessDenied() {
        User user = new User();
        user.setId(1);

        Storage storage = new Storage();
        storage.setId(2);

        StorageItem item = new StorageItem();
        item.setId(3);
        item.setStorage(storage);

        when(storageItemRepository.findById(3L)).thenReturn(Optional.of(item));
        doReturn(false).when(storageAccessService).canAccessStorage(2, user);

        assertThrows(AccessDeniedException.class, () -> storageItemService.removeItemFromStorage(3, user));
        verify(storageItemRepository, never()).deleteById(anyLong());
    }

}
