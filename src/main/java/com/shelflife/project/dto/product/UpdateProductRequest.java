package com.shelflife.project.dto.product;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UpdateProductRequest {
    @Length(max = 40, message = "The name can only be 40 characters long")
    private String name;

    @Length(max = 255, message = "The description can only be 255 characters long")
    private String description;

    @Length(max = 40, message = "The category can only be 40 characters long")
    private String category;

    @Length(max = 40, message = "The barcode can only be 40 characters long")
    private String barcode;

    @Min(value = 1, message = "Expiration delta must be larger than 0")
    private Integer expirationDaysDelta;
}
