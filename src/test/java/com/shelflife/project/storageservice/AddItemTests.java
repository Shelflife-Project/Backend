package com.shelflife.project.storageservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import com.shelflife.project.model.Product;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.StorageItem;
import com.shelflife.project.repository.StorageItemRepository;
import com.shelflife.project.repository.StorageRepository;
import com.shelflife.project.service.ProductService;
import com.shelflife.project.service.StorageService;

@ExtendWith(MockitoExtension.class)
public class AddItemTests {

    @Spy
    @InjectMocks
    private StorageService storageService;

    @Mock
    private StorageItemRepository storageItemRepository;

    @Mock
    private StorageRepository storageRepository;

    @Mock
    private ProductService productService;

    private Authentication auth;

    @Test
    void successfulAdd() {
        Storage storage = new Storage();
        storage.setId(1);

        Product product = new Product();
        product.setId(1);
        product.setExpirationDaysDelta(5);

        doReturn(storage).when(storageService).getStorage(1);
        doReturn(product).when(productService).getProductByID(1);
        doReturn(true).when(storageService).canAccessStorage(1, auth);
        when(storageItemRepository.save(any(StorageItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StorageItem result = storageService.addItemToStorage(1, 1, auth);

        assertNotNull(result);
        assertEquals(storage, result.getStorage());
        assertEquals(product, result.getProduct());
        assertNotNull(result.getExpiresAt());

        verify(storageItemRepository).save(any(StorageItem.class));
    }

    @Test
    void throwsAccessDenied() {
        doReturn(false).when(storageService).canAccessStorage(1, auth);

        assertThrows(AccessDeniedException.class, () -> storageService.addItemToStorage(1, 1, auth));

        verify(storageItemRepository, never()).save(any());
        verify(productService, never()).getProductByID(anyLong());
    }

    @Test
    void throwsItemNotFoundForStorage() {
        doReturn(true).when(storageService).canAccessStorage(1, auth);

        doThrow(ItemNotFoundException.class).when(storageService).getStorage(1);
        assertThrows(ItemNotFoundException.class, () -> storageService.addItemToStorage(1, 1, auth));

        verify(storageItemRepository, never()).save(any());
    }

    @Test
    void throwsItemNotFoundForProduct() {
        Storage storage = new Storage();
        storage.setId(1);

        doReturn(true).when(storageService).canAccessStorage(1, auth);
        doReturn(storage).when(storageService).getStorage(1);
        doThrow(ItemNotFoundException.class).when(productService).getProductByID(1);

        assertThrows(ItemNotFoundException.class, () -> storageService.addItemToStorage(1, 1, auth));

        verify(storageItemRepository, never()).save(any());
    }

}
