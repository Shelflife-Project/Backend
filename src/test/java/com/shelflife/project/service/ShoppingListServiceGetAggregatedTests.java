package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.shelflife.project.model.ShoppingListItem;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ShoppingListItemRepository;

@ExtendWith(MockitoExtension.class)
public class ShoppingListServiceGetAggregatedTests {

    @Mock
    private ShoppingListItemRepository repository;

    @Mock
    private StorageAccessService storageAccessService;

    @Mock
    private StorageGetterService storageGetterService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ShoppingListService shoppingListService;

    private User testUser;
    private Storage storage1;
    private Storage storage2;
    private Storage storage3;
    private ShoppingListItem item1;
    private ShoppingListItem item2;
    private ShoppingListItem item3;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        storage1 = new Storage();
        storage1.setId(1L);
        storage2 = new Storage();
        storage2.setId(2L);
        storage3 = new Storage();
        storage3.setId(3L);

        item1 = new ShoppingListItem();
        item1.setId(1L);
        item1.setStorage(storage1);

        item2 = new ShoppingListItem();
        item2.setId(2L);
        item2.setStorage(storage1);

        item3 = new ShoppingListItem();
        item3.setId(3L);
        item3.setStorage(storage2);
    }

    @Test
    void successfulGet() {
        Page<Storage> storagesPage = new PageImpl<>(Arrays.asList(storage1, storage2));
        
        when(storageGetterService.getStorages(testUser, "", Pageable.unpaged())).thenReturn(storagesPage);
        when(storageAccessService.canAccessStorage(1L, testUser)).thenReturn(true);
        when(storageAccessService.canAccessStorage(2L, testUser)).thenReturn(true);
        when(repository.findByStorageId(1L)).thenReturn(Arrays.asList(item1, item2));
        when(repository.findByStorageId(2L)).thenReturn(Arrays.asList(item3));

        List<ShoppingListItem> result = assertDoesNotThrow(() -> 
            shoppingListService.getShoppingListItemsAggregated(testUser));

        assertEquals(3, result.size());
    }

    @Test
    void testGetShoppingListItemsAggregated_NoStorages() {
        Page<Storage> emptyStoragesPage = new PageImpl<>(new ArrayList<>());
        
        when(storageGetterService.getStorages(testUser, "", Pageable.unpaged())).thenReturn(emptyStoragesPage);

        List<ShoppingListItem> result = assertDoesNotThrow(() -> 
            shoppingListService.getShoppingListItemsAggregated(testUser));

        assertEquals(0, result.size());
    }
}
