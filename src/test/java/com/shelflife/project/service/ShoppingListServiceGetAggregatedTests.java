package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.shelflife.project.model.User;
import com.shelflife.project.repository.ShoppingListItemRepository;
import com.shelflife.project.repository.StorageRepository;

@ExtendWith(MockitoExtension.class)
public class ShoppingListServiceGetAggregatedTests {
    @Mock
    private StorageRepository storageRepository;

    @Mock
    private ShoppingListItemRepository shoppingListItemRepository;

    @Spy
    @InjectMocks
    private ShoppingListService shoppingListService;

    @Test
    void throwsAccessDeniedWithNull() {
        assertThrows(AccessDeniedException.class, () -> shoppingListService.getAggregatedForUser(null));
    }

    @Test
    void returnsEmpty() {
        User user = new User();
        user.setId(1);

        doReturn(null).when(storageRepository).findAccessibleStorages(1);

        assertEquals(0, shoppingListService.getAggregatedForUser(user).size());
    }
}
