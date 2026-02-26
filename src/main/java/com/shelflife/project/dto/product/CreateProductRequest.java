package com.shelflife.project.dto.product;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CreateProductRequest {
    @NotBlank(message = "Name cannot be empty")
    @NotNull(message = "Name cannot be empty")
    @Length(max = 40, message = "The name can only be 40 characters long")
    private String name;

    @Length(max = 255, message = "The description can only be 255 characters long")
    private String description;

    @NotBlank(message = "Category cannot be empty")
    @NotNull(message = "Category cannot be empty")
    @Length(max = 40, message = "The category can only be 40 characters long")
    private String category;

    @Length(max = 40, message = "The barcode can only be 40 characters long")
    private String barcode;

    @Min(value = 1, message = "Expiration delta must be larger than 0")
    private int expirationDaysDelta;
}