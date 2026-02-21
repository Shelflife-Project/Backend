package com.shelflife.project.dto.storage;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AddItemRequest {
    @Min(value = 1, message = "Product ID must be larger than 0")
    private long productId;

    @NotNull
    private LocalDate expiresAt;

    @Min(value = 0, message = "Amount to buy must be 0 or greater")
    private int amountToBuy = 0;
}
