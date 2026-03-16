package com.shelflife.project.docs;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.shelflife.project.dto.shopping.CreateShoppingItemRequest;
import com.shelflife.project.dto.shopping.EditShoppingItemRequest;
import com.shelflife.project.model.ShoppingListItem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Shopping List", description = "Manage shopping list items in a storage")
public interface ShoppingListControllerDocs {
    @Operation(summary = "Get shopping list items for a storage", description = "Returns shopping list items for the given storage if the user has access")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<ShoppingListItem>> getForStorage(
            @Parameter(description = "Storage id") long storageId, Authentication auth);

    @Operation(summary = "Move shopping list items to storage and remove entry", description = "Moves all items from the shopping list entry with the given id to the storage, then removes the shopping list entry.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Items moved and entry removed"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Shopping list item not found")
    })
    public ResponseEntity<Void> addItemsToStorageAndRemove(
            @Parameter(description = "Storage id") long storageId,
            @Parameter(description = "Shopping list item id") long id,
            Authentication auth);

    @Operation(summary = "Create a shopping list item", description = "Add a product to the shopping list for the specified storage")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Storage or product not found")
    })
    public ResponseEntity<?> create(@Parameter(description = "Storage id") long storageId,
            CreateShoppingItemRequest request,
            Authentication auth);

    @Operation(summary = "Edit a shopping list item", description = "Update the amount to buy for an existing shopping list entry")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ResponseEntity<?> edit(@Parameter(description = "Shopping list item id") long id,
            EditShoppingItemRequest request,
            Authentication auth);

    @Operation(summary = "Delete a shopping list item", description = "Remove an item from the shopping list")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ResponseEntity<Void> delete(@Parameter(description = "Shopping list item id") long id,
            Authentication auth);
}
