package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import com.shelflife.project.dto.shopping.CreateShoppingItemRequest;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.exception.ShoppingItemExistsException;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.Storage;
import com.shelflife.project.repository.ShoppingListItemRepository;
import com.shelflife.project.repository.StorageRepository;

@ExtendWith(MockitoExtension.class)
public class ShoppingListServiceCreateItemTests {
    @Mock
    private StorageAccessService storageAccessService;

    @Mock
    private StorageGetterService storageGetterService;

    @Mock
    private ProductService productService;

    @Mock
    private UserService userService;

    @Mock
    private StorageRepository storageRepository;

    @Mock
    private ShoppingListItemRepository shoppingListItemRepository;

    @InjectMocks
    private ShoppingListService shoppingListService;

    private Authentication auth;

    @Test
    void throwsAccessDenied() {
        when(storageAccessService.canAccessStorage(1, auth)).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> shoppingListService.createItem(1, request(1, 10), auth));

        verifyNoInteractions(shoppingListItemRepository);
    }

    @Test
    void throwsStorageNotFound() {
        when(storageAccessService.canAccessStorage(1, auth)).thenReturn(true);
        when(storageGetterService.getStorage(1)).thenThrow(ItemNotFoundException.class);
        assertThrows(ItemNotFoundException.class, () -> shoppingListService.createItem(1, request(1, 10), auth));

        verifyNoInteractions(shoppingListItemRepository);
    }

    @Test
    void throwsProductNotFound() {
        when(storageAccessService.canAccessStorage(1, auth)).thenReturn(true);
        when(storageGetterService.getStorage(1)).thenReturn(new Storage());
        when(productService.getProductByID(1)).thenThrow(ItemNotFoundException.class);
        assertThrows(ItemNotFoundException.class, () -> shoppingListService.createItem(1, request(1, 10), auth));

        verifyNoInteractions(shoppingListItemRepository);
    }

    @Test
    void throwsItemExists() {
        Product product = new Product();
        product.setId(1);

        Storage storage = new Storage();
        storage.setId(1);

        when(storageAccessService.canAccessStorage(1, auth)).thenReturn(true);
        when(storageGetterService.getStorage(1)).thenReturn(storage);
        when(productService.getProductByID(1)).thenReturn(product);
        when(shoppingListItemRepository.existsByProductIdAndStorageId(1, 1)).thenReturn(true);

        assertThrows(ShoppingItemExistsException.class, () -> shoppingListService.createItem(1, request(1, 10), auth));

        verify(shoppingListItemRepository, never()).save(any());
    }

    @Test
    void throwsIllegalAmount() {
        Product product = new Product();
        product.setId(1);

        Storage storage = new Storage();
        storage.setId(1);

        when(storageAccessService.canAccessStorage(1, auth)).thenReturn(true);
        when(storageGetterService.getStorage(1)).thenReturn(storage);
        when(productService.getProductByID(1)).thenReturn(product);
        when(shoppingListItemRepository.existsByProductIdAndStorageId(1, 1)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> shoppingListService.createItem(1, request(1, -10), auth));

        verify(shoppingListItemRepository, never()).save(any());
    }

    @Test
    void success() {
        Product product = new Product();
        product.setId(1);

        Storage storage = new Storage();
        storage.setId(1);

        when(storageAccessService.canAccessStorage(1, auth)).thenReturn(true);
        when(storageGetterService.getStorage(1)).thenReturn(storage);
        when(productService.getProductByID(1)).thenReturn(product);
        when(shoppingListItemRepository.existsByProductIdAndStorageId(1, 1)).thenReturn(false);

        assertDoesNotThrow(() -> shoppingListService.createItem(1, request(1, 10), auth));
    }

    private CreateShoppingItemRequest request(long productId, int amountToBuy) {
        CreateShoppingItemRequest request = new CreateShoppingItemRequest();
        request.setProductId(productId);
        request.setAmountToBuy(amountToBuy);

        return request;
    }
}
