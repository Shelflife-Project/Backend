package com.shelflife.project.dto.storage;

import org.hibernate.validator.constraints.Length;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
@Schema(description = "Request payload for creating a new storage")
public class CreateStorageRequest {
    @NotBlank
    @Length(max = 40, message = "The name can only be 40 characters long")
    @Schema(
        description = "The name of the storage to be created",
        example = "Kitchen Pantry",
        minLength = 1,
        maxLength = 40
    )
    private String name;
}
