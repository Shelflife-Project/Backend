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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;

import com.shelflife.project.dto.shopping.CreateShoppingItemRequest;
import com.shelflife.project.dto.shopping.EditShoppingItemRequest;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.exception.ShoppingItemExistsException;
import com.shelflife.project.model.ShoppingListItem;
import com.shelflife.project.model.User;
import com.shelflife.project.service.ShoppingListService;
import com.shelflife.project.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/storages/{storageId}/shoppinglist")
@Tag(name = "Shopping List", description = "Manage shopping list items in a storage")
public class ShoppingListController {
    @Autowired
    private ShoppingListService service;

    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(summary = "Get shopping list items for a storage", description = "Returns shopping list items for the given storage if the user has access")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<ShoppingListItem>> getForStorage(
            @Parameter(description = "Storage id") @PathVariable long storageId, Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            return ResponseEntity.ok(service.getForStorage(storageId, user));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping
    @Operation(summary = "Create a shopping list item", description = "Add a product to the shopping list for the specified storage")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Storage or product not found")
    })
    public ResponseEntity<?> create(@Parameter(description = "Storage id") @PathVariable long storageId,
            @Valid @RequestBody CreateShoppingItemRequest request,
            Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            return ResponseEntity.ok(service.createItem(storageId, request, user));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ShoppingItemExistsException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("productId", "This product is already added to the shopping list"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(e.getMessage(), "Invalid value"));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edit a shopping list item", description = "Update the amount to buy for an existing shopping list entry")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ResponseEntity<?> edit(@Parameter(description = "Shopping list item id") @PathVariable long id,
            @Valid @RequestBody EditShoppingItemRequest request,
            Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            return ResponseEntity.ok(service.editItem(id, request, user));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(e.getMessage(), "Invalid value"));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a shopping list item", description = "Remove an item from the shopping list")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ResponseEntity<Void> delete(@Parameter(description = "Shopping list item id") @PathVariable long id,
            Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            service.deleteItem(id, user);
            return ResponseEntity.ok().build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
