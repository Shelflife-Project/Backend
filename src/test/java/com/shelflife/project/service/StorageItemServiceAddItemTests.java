package com.shelflife.project.service;

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

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.shelflife.project.dto.storage.AddItemRequest;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.StorageItem;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageItemRepository;
import com.shelflife.project.repository.StorageRepository;

@ExtendWith(MockitoExtension.class)
public class StorageItemServiceAddItemTests {

    @Mock
    private StorageItemRepository storageItemRepository;

    @Mock
    private StorageRepository storageRepository;

    @Mock
    private ProductService productService;

    @Mock
    private StorageAccessService storageAccessService;

    @Spy
    @InjectMocks
    private StorageItemService storageItemService;

    private User user = new User();

    @Test
    void successfulAdd() {
        Storage storage = new Storage();
        storage.setId(1);

        Product product = new Product();
        product.setId(1);
        product.setExpirationDaysDelta(5);

        doReturn(storage).when(storageItemService).getStorage(1);
        doReturn(product).when(productService).getProductByID(1);
        doReturn(true).when(storageAccessService).canAccessStorage(1, user);
        when(storageItemRepository.save(any(StorageItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StorageItem result = storageItemService.addItemToStorage(1, validRequest(1), user);

        assertNotNull(result);
        assertEquals(storage, result.getStorage());
        assertEquals(product, result.getProduct());
        assertNotNull(result.getExpiresAt());

        verify(storageItemRepository).save(any(StorageItem.class));
    }

    @Test
    void throwsIllegalArgumentForYesterday() {
        Storage storage = new Storage();
        storage.setId(1);

        AddItemRequest request = validRequest(1);
        request.setExpiresAt(LocalDate.now().minusDays(1));

        doReturn(true).when(storageAccessService).canAccessStorage(1, user);

        assertThrows(IllegalArgumentException.class, () -> storageItemService.addItemToStorage(1, request, user));

        verify(storageItemRepository, never()).save(any());
    }

    @Test
    void successfulForToday() {
        Storage storage = new Storage();
        storage.setId(1);

        Product product = new Product();
        product.setId(1);
        product.setExpirationDaysDelta(5);

        doReturn(storage).when(storageItemService).getStorage(1);
        doReturn(product).when(productService).getProductByID(1);
        doReturn(true).when(storageAccessService).canAccessStorage(1, user);
        when(storageItemRepository.save(any(StorageItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddItemRequest request = validRequest(1);
        request.setExpiresAt(LocalDate.now());

        StorageItem result = storageItemService.addItemToStorage(1, request, user);

        assertNotNull(result);
        assertEquals(storage, result.getStorage());
        assertEquals(product, result.getProduct());
        assertNotNull(result.getExpiresAt());

        verify(storageItemRepository).save(any(StorageItem.class));
    }

    @Test
    void throwsAccessDenied() {
        doReturn(false).when(storageAccessService).canAccessStorage(1, user);

        assertThrows(AccessDeniedException.class, () -> storageItemService.addItemToStorage(1, validRequest(1), user));

        verify(storageItemRepository, never()).save(any());
        verify(productService, never()).getProductByID(anyLong());
    }

    @Test
    void throwsItemNotFoundForStorage() {
        doReturn(true).when(storageAccessService).canAccessStorage(1, user);

        doThrow(ItemNotFoundException.class).when(storageItemService).getStorage(1);
        assertThrows(ItemNotFoundException.class, () -> storageItemService.addItemToStorage(1, validRequest(1), user));

        verify(storageItemRepository, never()).save(any());
    }

    @Test
    void throwsItemNotFoundForProduct() {
        Storage storage = new Storage();
        storage.setId(1);

        doReturn(true).when(storageAccessService).canAccessStorage(1, user);
        doReturn(storage).when(storageItemService).getStorage(1);
        doThrow(ItemNotFoundException.class).when(productService).getProductByID(1);

        assertThrows(ItemNotFoundException.class, () -> storageItemService.addItemToStorage(1, validRequest(1), user));

        verify(storageItemRepository, never()).save(any());
    }

    AddItemRequest validRequest(int productId) {
        AddItemRequest request = new AddItemRequest();
        request.setProductId(productId);
        request.setExpiresAt(LocalDate.now().plusDays(2));

        return request;
    }
}
