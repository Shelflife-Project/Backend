package com.shelflife.project.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.StorageItem;
import com.shelflife.project.model.StorageMember;
import com.shelflife.project.service.StorageService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
    public ResponseEntity<Storage> getStorage(@PathVariable long id, Authentication auth) {
        try {
            return ResponseEntity.ok(storageService.getStorage(auth, id));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<StorageItem>> getStorageItems(@PathVariable long id, Authentication auth) {
        try {
            return ResponseEntity.ok(storageService.getItemsInStorage(id, auth));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<StorageMember>> getStorageMembers(@PathVariable long id, Authentication auth) {
        try {
            return ResponseEntity.ok(storageService.getStorageMembers(id, auth));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

}
