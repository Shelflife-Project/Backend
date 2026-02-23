package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import com.shelflife.project.model.User;
import com.shelflife.project.repository.ShoppingListItemRepository;
import com.shelflife.project.repository.StorageRepository;

@ExtendWith(MockitoExtension.class)
public class ShoppingListServiceGetAggregatedTests {
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
    void throwsAccessDenied() {
        doThrow(AccessDeniedException.class).when(userService).getUserByAuth(auth);
        assertThrows(AccessDeniedException.class, () -> shoppingListService.getAggregatedForUser(auth));
    }

    @Test
    void returnsEmpty() {
        User user = new User();
        user.setId(1);

        doReturn(user).when(userService).getUserByAuth(auth);
        doReturn(null).when(storageRepository).findAccessibleStorages(1);

        assertEquals(0, shoppingListService.getAggregatedForUser(auth).size());
    }
}
