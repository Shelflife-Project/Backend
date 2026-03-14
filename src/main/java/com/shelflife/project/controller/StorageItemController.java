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

import com.shelflife.project.docs.StorageItemControllerDocs;
import com.shelflife.project.dto.runninglow.RunningLowNotification;
import com.shelflife.project.dto.storage.AddItemRequest;
import com.shelflife.project.dto.storage.EditItemRequest;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.StorageItem;
import com.shelflife.project.model.User;
import com.shelflife.project.service.RunningLowService;
import com.shelflife.project.service.StorageItemService;
import com.shelflife.project.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/storages/{storageId}")
public class StorageItemController implements StorageItemControllerDocs {
    @Autowired
    private StorageItemService storageItemService;

    @Autowired
    private RunningLowService runningLowService;

    @Autowired
    private UserService userService;

    @GetMapping("/items")
    public ResponseEntity<List<StorageItem>> getStorageItems(@PathVariable long storageId, Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            return ResponseEntity.ok(storageItemService.getItemsInStorage(storageId, user));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/items")
    public ResponseEntity<?> addItem(@PathVariable long storageId, @Valid @RequestBody AddItemRequest request,
            Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            return ResponseEntity
                    .ok(storageItemService.addItemToStorage(storageId, request, user));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(e.getField(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(e.getMessage(), "Invalid value"));
        }
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<?> editItem(@PathVariable long itemId, @Valid @RequestBody EditItemRequest request,
            Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            return ResponseEntity.ok(storageItemService.editItem(itemId, request, user));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(e.getMessage(), "Illegal argument"));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/expired")
    public ResponseEntity<List<StorageItem>> getExpired(@PathVariable long storageId, Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            return ResponseEntity.ok(storageItemService.getExpiredItemsInStorage(storageId, user));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/abouttoexpire")
    public ResponseEntity<List<StorageItem>> getAboutToExpire(@PathVariable long storageId, Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            return ResponseEntity.ok(storageItemService.getItemsAboutToExpire(storageId, user));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/runninglow")
    public ResponseEntity<List<RunningLowNotification>> getRunningLow(@PathVariable long storageId,
            Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            return ResponseEntity.ok(runningLowService.getRunningLowInStorage(storageId, user));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable long storageId, @PathVariable long itemId,
            Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            storageItemService.removeItemFromStorage(itemId, user);
            return ResponseEntity.ok().build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
