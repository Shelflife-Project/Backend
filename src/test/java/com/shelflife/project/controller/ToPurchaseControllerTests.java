package com.shelflife.project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shelflife.project.dto.purchase.ToPurchaseItem;
import com.shelflife.project.service.StorageItemService;

@ExtendWith(MockitoExtension.class)
public class ToPurchaseControllerTests {

    @InjectMocks
    private ToPurchaseController controller;

    @Mock
    private StorageItemService storageItemService;

    @Test
    public void getToBuy_returnsAggregatedList() {
        ToPurchaseItem t1 = new ToPurchaseItem();
        t1.setProductId(10L);
        t1.setProductName("Apples");
        t1.setAmountToBuy(5);

        ToPurchaseItem t2 = new ToPurchaseItem();
        t2.setProductId(11L);
        t2.setProductName("Bananas");
        t2.setAmountToBuy(1);

        when(storageItemService.getToPurchaseForUser(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Arrays.asList(t1, t2));

        var resp = controller.getToBuy(null);
        assertEquals(2, resp.getBody().size());
        assertEquals("Apples", resp.getBody().stream().filter(i -> i.getProductId() == 10L).findFirst().get().getProductName());
    }
}
