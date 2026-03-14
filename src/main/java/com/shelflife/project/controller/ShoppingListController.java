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

import com.shelflife.project.docs.ShoppingListControllerDocs;
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
public class ShoppingListController implements ShoppingListControllerDocs {
    @Autowired
    private ShoppingListService service;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<ShoppingListItem>> getForStorage(
            @PathVariable long storageId, Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            return ResponseEntity.ok(service.getForStorage(storageId, user));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@PathVariable long storageId,
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
    public ResponseEntity<?> edit(@PathVariable long id,
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
    public ResponseEntity<Void> delete(@PathVariable long id,
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
