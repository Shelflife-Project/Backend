package com.shelflife.project.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shelflife.project.dto.AddItemRequest;
import com.shelflife.project.dto.CreateStorageRequest;
import com.shelflife.project.dto.InviteMemberRequest;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.exception.MemberException;
import com.shelflife.project.model.Storage;
import com.shelflife.project.service.StorageService;

import jakarta.validation.Valid;

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
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/storages")
public class StorageController {
    @Autowired
    private StorageService storageService;

    @GetMapping
    public ResponseEntity<List<Storage>> getStorages(Authentication auth) {
        try {
            return ResponseEntity.ok(storageService.getStorages(auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStorage(@PathVariable long id, Authentication auth) {
        try {
            return ResponseEntity.ok(storageService.getStorage(auth, id));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(e.getField(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<?> getStorageItems(@PathVariable long id, Authentication auth) {
        try {
            return ResponseEntity.ok(storageService.getItemsInStorage(id, auth));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(e.getField(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<?> getStorageMembers(@PathVariable long id, Authentication auth) {
        try {
            return ResponseEntity.ok(storageService.getStorageMembers(id, auth));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(e.getField(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{id}/expired")
    public ResponseEntity<?> getExpired(@PathVariable long id, Authentication auth) {
        try {
            return ResponseEntity.ok(storageService.getExpiredItemsInStorage(id, auth));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(e.getField(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{id}/abouttoexpire")
    public ResponseEntity<?> getAboutToExpire(@PathVariable long id, Authentication auth) {
        try {
            return ResponseEntity.ok(storageService.getItemsAboutToExpire(id, auth));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(e.getField(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/")
    public ResponseEntity<Storage> createStorage(@Valid @RequestBody CreateStorageRequest request,
            Authentication auth) {
        try {
            return ResponseEntity.ok(storageService.createStorage(request, auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<?> inviteMember(@PathVariable long id, @Valid @RequestBody InviteMemberRequest request,
            Authentication auth) {
        try {
            return ResponseEntity
                    .ok(storageService.addMemberToStorage(id, request.getUserEmail(), auth));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(e.getField(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (MemberException e) {
            return ResponseEntity.badRequest().body(Map.of("email", "User is already a member or was already invited"));
        }
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<?> addItem(@PathVariable long id, @Valid @RequestBody AddItemRequest request,
            Authentication auth) {
        try {
            return ResponseEntity
                    .ok(storageService.addItemToStorage(id, request.getProductId(), auth));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(e.getField(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStorage(@PathVariable long id, Authentication auth) {
        try {
            storageService.deleteStorage(id, auth);
            return ResponseEntity.ok().build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(e.getField(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{storageId}/members/{userId}")
    public ResponseEntity<?> deleteMember(@PathVariable long storageId, @PathVariable long userId,
            Authentication auth) {
        try {
            storageService.removeMemberFromStorage(storageId, userId, auth);
            return ResponseEntity.ok().build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(e.getField(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{storageId}/items/{itemId}")
    public ResponseEntity<?> deleteItem(@PathVariable long storageId, @PathVariable long itemId,
            Authentication auth) {
        try {
            storageService.removeItemFromStorage(itemId, auth);
            return ResponseEntity.ok().build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(e.getField(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
