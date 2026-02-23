package com.shelflife.project.dto.shopping;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "Request payload for creating a shopping list entry")
public class CreateShoppingItemRequest {
    @Min(value = 1, message = "Product ID must be larger than 0")
    @Schema(description = "Product id to add to shopping list", example = "5", minimum = "1")
    private long productId;

    @Min(value = 0, message = "Amount to buy must be 0 or greater")
    @Schema(description = "Amount to buy for this product in the storage", example = "3", minimum = "0")
    private int amountToBuy;
}
