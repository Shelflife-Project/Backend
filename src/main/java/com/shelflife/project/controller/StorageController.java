package com.shelflife.project.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shelflife.project.docs.StorageControllerDocs;
import com.shelflife.project.dto.PaginatedResponse;
import com.shelflife.project.dto.storage.ChangeStorageNameRequest;
import com.shelflife.project.dto.storage.CreateStorageRequest;
import com.shelflife.project.dto.storage.StorageSummary;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.service.StorageGetterService;
import com.shelflife.project.service.StorageService;
import com.shelflife.project.service.UserService;

import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

@RestController
@RequestMapping("/api/storages")
public class StorageController implements StorageControllerDocs {
    @Autowired
    private StorageService storageService;

    @Autowired
    private StorageGetterService storageGetterService;

    @Autowired
    private UserService userService;

    @GetMapping()
    public ResponseEntity<PaginatedResponse<Storage>> getStorages(Authentication auth,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending) {
        try {
            Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

            Pageable pageable;

            if (size == null) {
                pageable = Pageable.unpaged();
            } else {
                pageable = PageRequest.of(page, size, sort);
            }

            User user = userService.getUserByAuth(auth);
            Page<Storage> res = storageGetterService.getStorages(user, search, pageable);

            return ResponseEntity.ok(new PaginatedResponse<Storage>(res));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<PaginatedResponse<StorageSummary>> getStorageSummaries(Authentication auth,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending) {
        try {
            Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

            Pageable pageable;

            if (size == null) {
                pageable = Pageable.unpaged();
            } else {
                pageable = PageRequest.of(page, size, sort);
            }

            User user = userService.getUserByAuth(auth);
            Page<StorageSummary> res = storageGetterService.getStorageSummaries(user, search, pageable);

            return ResponseEntity.ok(new PaginatedResponse<StorageSummary>(res));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStorage(
            @PathVariable long id,
            Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            return ResponseEntity.ok(storageGetterService.getStorage(user, id));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping()
    public ResponseEntity<?> createStorage(
            @Valid @RequestBody CreateStorageRequest request,
            Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            return ResponseEntity.ok(storageService.createStorage(request, user));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("name", "Name cannot be empty"));
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> changeName(
            @PathVariable long id,
            @Valid @RequestBody ChangeStorageNameRequest request,
            Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            return ResponseEntity.ok(storageService.changeName(id, request, user));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(e.getMessage(), "Name cannot be empty"));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStorage(
            @PathVariable long id,
            Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            storageService.deleteStorageRequest(id, user);
            return ResponseEntity.ok().build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
