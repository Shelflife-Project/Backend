package com.shelflife.project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.shelflife.project.dto.purchase.ToPurchaseItem;
import com.shelflife.project.model.ShoppingListItem;
import com.shelflife.project.service.ShoppingListService;

@ExtendWith(MockitoExtension.class)
public class ShoppingListControllerTests {

    @InjectMocks
    private ShoppingListAggregateController controller;

    @Mock
    private ShoppingListService service;

    @Mock
    private Authentication auth;

    @Test
    public void aggregateEndpoint_returnsSummedList() {
        ToPurchaseItem t1 = new ToPurchaseItem();
        t1.setProductId(10L);
        t1.setProductName("Apples");
        t1.setAmountToBuy(4);

        ToPurchaseItem t2 = new ToPurchaseItem();
        t2.setProductId(11L);
        t2.setProductName("Bananas");
        t2.setAmountToBuy(2);

        when(service.getAggregatedForUser(auth)).thenReturn(Arrays.asList(t1, t2));

        ResponseEntity<java.util.List<ToPurchaseItem>> resp = controller.getAggregated(auth);

        assertEquals(2, resp.getBody().size());
        assertEquals("Apples", resp.getBody().stream().filter(i -> i.getProductId() == 10L).findFirst().get().getProductName());
    }
}
