package com.shelflife.project.dto.shopping;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "Request payload for editing a shopping list entry")
public class EditShoppingItemRequest {
    @Min(value = 0, message = "Amount to buy must be 0 or greater")
    @Schema(description = "Updated amount to buy for this product in the storage", example = "2", minimum = "0")
    private int amountToBuy;
}
