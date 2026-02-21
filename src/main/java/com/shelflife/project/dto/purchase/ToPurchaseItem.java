package com.shelflife.project.dto.purchase;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Aggregated purchase item containing product and summed amount to buy")
public class ToPurchaseItem {
    @Schema(description = "Product id", example = "10")
    private long productId;

    @Schema(description = "Product name", example = "Apples")
    private String productName;

    @Schema(description = "Total amount to buy across accessible storages", example = "5")
    private int amountToBuy;
}
