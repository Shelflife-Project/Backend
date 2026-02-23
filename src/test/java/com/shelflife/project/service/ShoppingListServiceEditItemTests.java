package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import com.shelflife.project.dto.shopping.EditShoppingItemRequest;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.ShoppingListItem;
import com.shelflife.project.model.Storage;
import com.shelflife.project.repository.ShoppingListItemRepository;
import com.shelflife.project.repository.StorageRepository;

@ExtendWith(MockitoExtension.class)
public class ShoppingListServiceEditItemTests {
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

    @Spy
    @InjectMocks
    private ShoppingListService shoppingListService;

    private Authentication auth;

    @Test
    void throwsNotFound() {
        Storage storage = new Storage();
        storage.setId(1);

        ShoppingListItem item = new ShoppingListItem();
        item.setId(1);
        item.setStorage(storage);

        doThrow(ItemNotFoundException.class).when(shoppingListService).getItem(1);
        assertThrows(ItemNotFoundException.class, () -> shoppingListService.editItem(1, request(5), auth));

        verifyNoInteractions(shoppingListItemRepository);
    }

    @Test
    void throwsAccessDenied() {
        Storage storage = new Storage();
        storage.setId(1);

        ShoppingListItem item = new ShoppingListItem();
        item.setId(1);
        item.setStorage(storage);

        doReturn(item).when(shoppingListService).getItem(1);
        when(storageAccessService.canAccessStorage(1, auth)).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> shoppingListService.editItem(1, request(5), auth));

        verifyNoInteractions(shoppingListItemRepository);
    }

    @Test
    void throwsIllegalAmount() {
        Storage storage = new Storage();
        storage.setId(1);

        ShoppingListItem item = new ShoppingListItem();
        item.setId(1);
        item.setStorage(storage);

        doReturn(item).when(shoppingListService).getItem(1);
        when(storageAccessService.canAccessStorage(1, auth)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> shoppingListService.editItem(1, request(-5), auth));

        verifyNoInteractions(shoppingListItemRepository);
    }

    @Test
    void successfulEdit() {
        Storage storage = new Storage();
        storage.setId(1);

        ShoppingListItem item = new ShoppingListItem();
        item.setId(1);
        item.setStorage(storage);

        doReturn(item).when(shoppingListService).getItem(1);
        when(storageAccessService.canAccessStorage(1, auth)).thenReturn(true);
        assertDoesNotThrow(() -> shoppingListService.editItem(1, request(5), auth));

        verify(shoppingListItemRepository).save(item);
    }

    private EditShoppingItemRequest request(int amountToBuy) {
        EditShoppingItemRequest request = new EditShoppingItemRequest();
        request.setAmountToBuy(amountToBuy);

        return request;
    }
}
