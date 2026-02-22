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
import org.springframework.security.core.Authentication;

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

    private Authentication auth;

    @Test
    void throwsAccessDenied() {
        when(storageAccessService.canAccessStorage(1, auth)).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> shoppingListService.getForStorage(1, auth));

        verifyNoInteractions(shoppingListItemRepository);
    }

    @Test
    void returnsShoppingList() {
        when(storageAccessService.canAccessStorage(1, auth)).thenReturn(true);
        assertDoesNotThrow(() -> shoppingListService.getForStorage(1, auth));

        verify(shoppingListItemRepository).findByStorageId(1);
    }
}
