package com.shelflife.project.dto.storage;

import com.shelflife.project.model.User;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Lightweight storage projection with aggregate counts (items, shopping items, accepted members)")
public class StorageSummary {
    @Schema(description = "Unique identifier of the storage", example = "1")
    private long id;

    @Schema(description = "The name of the storage", example = "Kitchen Pantry")
    private String name;

    @Schema(description = "The owner of the storage")
    private User owner;

    @Schema(description = "Number of inventory items currently in the storage", example = "42")
    private long itemCount;

    @Schema(description = "Number of shopping list items linked to the storage", example = "5")
    private long shoppingItemCount;

    @Schema(description = "Number of accepted members of the storage (excluding the owner)", example = "3")
    private long memberCount;
}
