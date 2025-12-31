package com.shelflife.project.storageservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.StorageItem;
import com.shelflife.project.repository.StorageItemRepository;
import com.shelflife.project.repository.StorageRepository;
import com.shelflife.project.service.StorageService;
import com.shelflife.project.service.UserService;

@ExtendWith(MockitoExtension.class)
public class GetItemsInStorageTests {
    @Mock
    private UserService userService;

    @Mock
    private StorageItemRepository storageItemRepository;

    @Mock
    private StorageRepository storageRepository;

    @InjectMocks
    private StorageService storageService;

    Authentication auth;

    @Test
    void throwsNotFound() {
        when(storageRepository.existsById(1L)).thenReturn(false);

        assertThrows(ItemNotFoundException.class, () -> storageService.getItemsInStorage(1));
    }

    @Test
    void returnsItems() {
        List<StorageItem> items = new ArrayList<>();
        items.add(new StorageItem());
        items.add(new StorageItem());

        when(storageRepository.existsById(1L)).thenReturn(true);
        when(storageItemRepository.findByStorageId(1L)).thenReturn(items);

        assertDoesNotThrow(() -> storageService.getItemsInStorage(1));
        assertEquals(items, storageService.getItemsInStorage(1));
    }

}
