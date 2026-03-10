package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.shelflife.project.model.User;
import com.shelflife.project.repository.ShoppingListItemRepository;
import com.shelflife.project.repository.StorageRepository;

@ExtendWith(MockitoExtension.class)
public class ShoppingListServiceGetForStorageTests {
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

    private User user = new User();

    @Test
    void throwsAccessDenied() {
        when(storageAccessService.canAccessStorage(1, user)).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> shoppingListService.getForStorage(1, user));

        verifyNoInteractions(shoppingListItemRepository);
    }

    @Test
    void returnsShoppingList() {
        when(storageAccessService.canAccessStorage(1, user)).thenReturn(true);
        assertDoesNotThrow(() -> shoppingListService.getForStorage(1, user));

        verify(shoppingListItemRepository).findByStorageId(1);
    }
}
