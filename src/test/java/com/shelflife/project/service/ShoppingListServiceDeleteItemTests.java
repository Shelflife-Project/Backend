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

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.ShoppingListItem;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ShoppingListItemRepository;
import com.shelflife.project.repository.StorageRepository;

@ExtendWith(MockitoExtension.class)
public class ShoppingListServiceDeleteItemTests {
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

    private User user = new User();

    @Test
    void throwsNotFound() {
        Storage storage = new Storage();
        storage.setId(1);

        ShoppingListItem item = new ShoppingListItem();
        item.setId(1);
        item.setStorage(storage);

        doThrow(ItemNotFoundException.class).when(shoppingListService).getItem(1);
        assertThrows(ItemNotFoundException.class, () -> shoppingListService.deleteItem(1, user));

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
        when(storageAccessService.canAccessStorage(1, user)).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> shoppingListService.deleteItem(1, user));

        verifyNoInteractions(shoppingListItemRepository);
    }

    @Test
    void successfulDelete() {
        Storage storage = new Storage();
        storage.setId(1);

        ShoppingListItem item = new ShoppingListItem();
        item.setId(1);
        item.setStorage(storage);

        doReturn(item).when(shoppingListService).getItem(1);
        when(storageAccessService.canAccessStorage(1, user)).thenReturn(true);
        assertDoesNotThrow(() -> shoppingListService.deleteItem(1, user));

        verify(shoppingListItemRepository).delete(item);
    }
}
