package com.shelflife.project.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shelflife.project.dto.runninglow.RunningLowNotification;
import com.shelflife.project.dto.storage.AddItemRequest;
import com.shelflife.project.dto.storage.EditItemRequest;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.StorageItem;
import com.shelflife.project.service.RunningLowService;
import com.shelflife.project.service.StorageItemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/storages/{storageId}")
public class StorageItemController {
    @Autowired
    private StorageItemService storageItemService;

    @Autowired
    private RunningLowService runningLowService;

    @GetMapping("/items")
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
    public ResponseEntity<List<StorageItem>> getStorageItems(@PathVariable long storageId, Authentication auth) {
        try {
            return ResponseEntity.ok(storageItemService.getItemsInStorage(storageId, auth));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/items")
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
    public ResponseEntity<?> addItem(@PathVariable long storageId, @Valid @RequestBody AddItemRequest request,
            Authentication auth) {
        try {
            return ResponseEntity
                    .ok(storageItemService.addItemToStorage(storageId, request, auth));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(e.getField(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(e.getMessage(), "Invalid value"));
        }
    }

    @PatchMapping("/items/{itemId}")
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
    public ResponseEntity<?> editItem(@PathVariable long itemId, @Valid @RequestBody EditItemRequest request,
            Authentication auth) {
        try {
            return ResponseEntity.ok(storageItemService.editItem(itemId, request, auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(e.getMessage(), "Illegal argument"));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/expired")
    @Operation(summary = "Get expired items in a storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of expired items"),
            @ApiResponse(responseCode = "403", description = "You can't access this storage", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "404", description = "Storage not found", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<List<StorageItem>> getExpired(@PathVariable long storageId, Authentication auth) {
        try {
            return ResponseEntity.ok(storageItemService.getExpiredItemsInStorage(storageId, auth));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/abouttoexpire")
    @Operation(summary = "Get items that expire tomorrow in a storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of items"),
            @ApiResponse(responseCode = "403", description = "You can't access this storage", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "404", description = "Storage not found", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<List<StorageItem>> getAboutToExpire(@PathVariable long storageId, Authentication auth) {
        try {
            return ResponseEntity.ok(storageItemService.getItemsAboutToExpire(storageId, auth));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/runninglow")
    @Operation(summary = "Get items that are running low in a storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of items"),
            @ApiResponse(responseCode = "403", description = "You can't access this storage", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<List<RunningLowNotification>> getRunningLow(@PathVariable long storageId,
            Authentication auth) {
        try {
            return ResponseEntity.ok(runningLowService.getRunningLowInStorage(storageId, auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove an item from a storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful removal"),
            @ApiResponse(responseCode = "403", description = "You can't access this storage"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ResponseEntity<Void> deleteItem(@PathVariable long storageId, @PathVariable long itemId,
            Authentication auth) {
        try {
            storageItemService.removeItemFromStorage(itemId, auth);
            return ResponseEntity.ok().build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
