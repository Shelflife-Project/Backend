package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

import com.shelflife.project.dto.purchase.ToPurchaseItem;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.ShoppingListItem;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ShoppingListItemRepository;
import com.shelflife.project.repository.StorageRepository;

public class ShoppingListServiceTests {

    @Mock
    private ShoppingListItemRepository shoppingListRepository;

    @Mock
    private StorageRepository storageRepository;

    @Mock
    private UserService userService;

    @Mock
    private Authentication auth;

    @InjectMocks
    private ShoppingListService shoppingListService;

    private User user;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(42L);
    }

    @Test
    public void getAggregatedForUser_sumsAmountsAcrossStorages() {
        when(userService.getUserByAuth(auth)).thenReturn(user);

        Storage s1 = new Storage();
        s1.setId(1L);
        Storage s2 = new Storage();
        s2.setId(2L);

        when(storageRepository.findAccessibleStorages(user.getId())).thenReturn(Arrays.asList(s1, s2));

        Product p1 = new Product();
        p1.setId(10L);
        p1.setName("Apples");

        Product p2 = new Product();
        p2.setId(11L);
        p2.setName("Bananas");

        ShoppingListItem si1 = new ShoppingListItem();
        si1.setProduct(p1);
        si1.setAmountToBuy(3);
        si1.setStorage(s1);

        ShoppingListItem si2 = new ShoppingListItem();
        si2.setProduct(p1);
        si2.setAmountToBuy(1);
        si2.setStorage(s2);

        ShoppingListItem si3 = new ShoppingListItem();
        si3.setProduct(p2);
        si3.setAmountToBuy(2);
        si3.setStorage(s1);

        when(shoppingListRepository.findByStorageIdIn(Arrays.asList(1L, 2L)))
                .thenReturn(Arrays.asList(si1, si2, si3));

        List<ToPurchaseItem> result = shoppingListService.getAggregatedForUser(auth);

        assertEquals(2, result.size());

        ToPurchaseItem itemP1 = result.stream().filter(i -> i.getProductId() == 10L).findFirst().get();
        ToPurchaseItem itemP2 = result.stream().filter(i -> i.getProductId() == 11L).findFirst().get();

        assertEquals(4, itemP1.getAmountToBuy());
        assertEquals("Apples", itemP1.getProductName());
        assertEquals(2, itemP2.getAmountToBuy());
        assertEquals("Bananas", itemP2.getProductName());
    }
}
