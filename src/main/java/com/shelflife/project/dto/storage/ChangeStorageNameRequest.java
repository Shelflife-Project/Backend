package com.shelflife.project.dto.storage;

import org.hibernate.validator.constraints.Length;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
@Schema(description = "Request payload for updating a storage's name")
public class ChangeStorageNameRequest {
    @NotEmpty
    @Length(max = 40, message = "The name can only be 40 characters long")
    @Schema(
        description = "The new name for the storage",
        example = "Updated Kitchen Pantry",
        minLength = 1,
        maxLength = 40
    )
    private String name;
}
