package com.shelflife.project.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AddItemRequest {
    @Min(value = 0, message = "Product ID must be larger than 0")
    private long productId;
}
