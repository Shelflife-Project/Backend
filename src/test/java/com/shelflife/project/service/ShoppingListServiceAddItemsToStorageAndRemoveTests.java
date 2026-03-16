package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
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

import com.shelflife.project.dto.storage.AddItemRequest;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.ShoppingListItem;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ShoppingListItemRepository;
import com.shelflife.project.repository.StorageRepository;

@ExtendWith(MockitoExtension.class)
public class ShoppingListServiceAddItemsToStorageAndRemoveTests {
    @Mock
    private StorageAccessService storageAccessService;

    @Mock
    private StorageItemService storageItemService;

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
        doThrow(ItemNotFoundException.class).when(shoppingListService).getItem(1L);
        assertThrows(ItemNotFoundException.class, () -> shoppingListService.addItemsToStorageAndRemove(1L, 2L, user));

        verifyNoInteractions(storageItemService);
        verifyNoInteractions(shoppingListItemRepository);
    }

    @Test
    void throwsNotFoundWhenDifferentStorageIds() {
        ShoppingListItem item = shoppingListItem(1, 2, 3);

        doReturn(item).when(shoppingListService).getItem(1);

        assertThrows(ItemNotFoundException.class, () -> shoppingListService.addItemsToStorageAndRemove(1L, 3L, user));

        verifyNoInteractions(storageItemService);
        verifyNoInteractions(shoppingListItemRepository);
    }

    @Test
    void throwsAccessDenied() throws ItemNotFoundException {
        long shoppingItemId = 1L;
        long storageId = 2L;

        ShoppingListItem item = shoppingListItem(shoppingItemId, storageId, 1);

        doReturn(item).when(shoppingListService).getItem(shoppingItemId);
        doReturn(false).when(storageAccessService).canAccessStorage(storageId, user);

        assertThrows(AccessDeniedException.class,
                () -> shoppingListService.addItemsToStorageAndRemove(shoppingItemId, storageId, user));

        verifyNoInteractions(storageItemService);
        verifyNoInteractions(shoppingListItemRepository);
    }

    @Test
    void addsItemsAndRemovesShoppingListItem() throws ItemNotFoundException {
        ShoppingListItem item = shoppingListItem(1, 2, 3);

        doReturn(item).when(shoppingListService).getItem(1);
        when(storageAccessService.canAccessStorage(2, user)).thenReturn(true);
        
        assertDoesNotThrow(() -> shoppingListService.addItemsToStorageAndRemove(1, 2, user));
        
        verify(storageItemService, times(3)).addItemToStorage(any(Long.class), any(AddItemRequest.class), any(User.class));
        verify(shoppingListItemRepository).delete(item);
    }

    private ShoppingListItem shoppingListItem(long id, long storageId, int amountToBuy) {
        Storage storage = new Storage();
        storage.setId(storageId);

        Product product = new Product();
        product.setId(5);

        ShoppingListItem item = new ShoppingListItem();
        item.setId(id);
        item.setStorage(storage);
        item.setAmountToBuy(amountToBuy);
        item.setProduct(product);

        return item;
    }
}
