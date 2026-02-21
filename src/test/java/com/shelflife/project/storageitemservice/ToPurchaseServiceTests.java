package com.shelflife.project.storageitemservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

import com.shelflife.project.dto.purchase.ToPurchaseItem;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.StorageItem;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.StorageItemRepository;
import com.shelflife.project.repository.StorageRepository;
import com.shelflife.project.service.StorageItemService;
import com.shelflife.project.service.UserService;

public class ToPurchaseServiceTests {

    @Mock
    private StorageItemRepository storageItemRepository;

    @Mock
    private StorageRepository storageRepository;

    @Mock
    private UserService userService;

    @Mock
    private Authentication auth;

    @InjectMocks
    private StorageItemService storageItemService;

    private User user;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(42L);
    }

    @Test
    public void getToPurchaseForUser_aggregatesAmountsAcrossStorages() {
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

        StorageItem si1 = new StorageItem();
        si1.setProduct(p1);
        si1.setAmountToBuy(2);
        si1.setStorage(s1);

        StorageItem si2 = new StorageItem();
        si2.setProduct(p1);
        si2.setAmountToBuy(3);
        si2.setStorage(s2);

        StorageItem si3 = new StorageItem();
        si3.setProduct(p2);
        si3.setAmountToBuy(1);
        si3.setStorage(s1);

        when(storageItemRepository.findByStorageIdIn(Arrays.asList(1L, 2L)))
                .thenReturn(Arrays.asList(si1, si2, si3));

        List<ToPurchaseItem> result = storageItemService.getToPurchaseForUser(auth);

        assertEquals(2, result.size());

        ToPurchaseItem itemP1 = result.stream().filter(i -> i.getProductId() == 10L).findFirst().get();
        ToPurchaseItem itemP2 = result.stream().filter(i -> i.getProductId() == 11L).findFirst().get();

        assertEquals(5, itemP1.getAmountToBuy());
        assertEquals("Apples", itemP1.getProductName());
        assertEquals(1, itemP2.getAmountToBuy());
        assertEquals("Bananas", itemP2.getProductName());
    }
}
