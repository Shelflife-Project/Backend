package com.shelflife.project.docs;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.shelflife.project.dto.runninglow.RunningLowNotification;
import com.shelflife.project.dto.storage.AddItemRequest;
import com.shelflife.project.dto.storage.EditItemRequest;
import com.shelflife.project.model.StorageItem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface StorageItemControllerDocs {
    @Operation(summary = "Get items in a storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved items"),
            @ApiResponse(responseCode = "404", description = "Storage with this id was not found", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "403", description = "You can't access this storage", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<List<StorageItem>> getStorageItems(long storageId, Authentication auth);

    @Operation(summary = "Add item to a storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully added item"),
            @ApiResponse(responseCode = "404", description = "Storage with this id was not found", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "403", description = "You can't access this storage", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid argument", content = {
                    @Content(schema = @Schema(example = "{\"productId\": \"Invalid ID\"}"))
            })
    })
    public ResponseEntity<?> addItem(long storageId, AddItemRequest request,
            Authentication auth);

    @Operation(summary = "Edit a field of an item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful edit", content = {
                    @Content(schema = @Schema(implementation = StorageItem.class), mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "403", description = "You can't access this item", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "404", description = "Item not found", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "400", description = "Illegal argument", content = {
                    @Content(schema = @Schema(example = "{\"expiresAt\": \"Invalid date\"}"))
            })
    })
    public ResponseEntity<?> editItem(long itemId, EditItemRequest request,
            Authentication auth);

    @Operation(summary = "Get items that are running low in a storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of items"),
            @ApiResponse(responseCode = "403", description = "You can't access this storage", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<List<RunningLowNotification>> getRunningLow(long storageId,
            Authentication auth);

    @Operation(summary = "Remove an item from a storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful removal"),
            @ApiResponse(responseCode = "403", description = "You can't access this storage"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ResponseEntity<Void> deleteItem(long storageId, long itemId,
            Authentication auth);
}
