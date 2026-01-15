package com.shelflife.project.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shelflife.project.dto.storage.AddItemRequest;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.service.StorageItemService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/storages/{storageId}")
public class StorageItemController {
    @Autowired
    private StorageItemService storageItemService;

    @GetMapping("/items")
    public ResponseEntity<?> getStorageItems(@PathVariable long storageId, Authentication auth) {
        try {
            return ResponseEntity.ok(storageItemService.getItemsInStorage(storageId, auth));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(e.getField(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/items")
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

    @GetMapping("/expired")
    public ResponseEntity<?> getExpired(@PathVariable long storageId, Authentication auth) {
        try {
            return ResponseEntity.ok(storageItemService.getExpiredItemsInStorage(storageId, auth));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(e.getField(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/abouttoexpire")
    public ResponseEntity<?> getAboutToExpire(@PathVariable long storageId, Authentication auth) {
        try {
            return ResponseEntity.ok(storageItemService.getItemsAboutToExpire(storageId, auth));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(e.getField(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> deleteItem(@PathVariable long storageId, @PathVariable long itemId,
            Authentication auth) {
        try {
            storageItemService.removeItemFromStorage(itemId, auth);
            return ResponseEntity.ok().build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(e.getField(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
