package com.shelflife.project.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shelflife.project.dto.storage.ChangeStorageNameRequest;
import com.shelflife.project.dto.storage.CreateStorageRequest;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Storage;
import com.shelflife.project.model.User;
import com.shelflife.project.service.StorageGetterService;
import com.shelflife.project.service.StorageService;
import com.shelflife.project.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
@Tag(name = "Storage Management", description = "APIs for managing storage resources. All endpoints require authentication.")
@SecurityRequirement(name = "bearerAuth")
public class StorageController {
    @Autowired
    private StorageService storageService;

    @Autowired
    private StorageGetterService storageGetterService;

    @Autowired
    private UserService userService;

    @GetMapping()
    @Operation(summary = "Get owned and member storages", description = "Retrieves all storages owned by the authenticated user and storages where the user is a member.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved storages", content = {
                    @Content(schema = @Schema(implementation = java.util.List.class, example = "[{\"id\": 1, \"name\": \"Kitchen\", \"owner\": {...}}]"))
            }),
            @ApiResponse(responseCode = "403", description = "Access denied - User must be authenticated", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<List<Storage>> getStorages(Authentication auth,
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
            return ResponseEntity.ok(storageGetterService.getStorages(auth, search, pageable));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a storage by ID", description = "Retrieves detailed information about a specific storage. User must be the owner or a member of the storage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved storage", content = {
                    @Content(schema = @Schema(implementation = Storage.class))
            }),
            @ApiResponse(responseCode = "403", description = "Access denied - User is not the owner or member", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "404", description = "Storage with this ID was not found", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<?> getStorage(
            @Parameter(description = "The unique identifier of the storage", example = "1") @PathVariable long id,
            Authentication auth) {
        try {
            return ResponseEntity.ok(storageGetterService.getStorage(auth, id));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping()
    @Operation(summary = "Create a storage", description = "Creates a new storage with the given name. The authenticated user becomes the owner of the storage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created storage", content = {
                    @Content(schema = @Schema(implementation = Storage.class))
            }),
            @ApiResponse(responseCode = "403", description = "Access denied - User must be authenticated", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid name - Name cannot be empty or exceed 40 characters", content = {
                    @Content(schema = @Schema(example = "{ \"name\": \"Name cannot be empty\" }"))
            })
    })
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
    @Operation(summary = "Edit a storage's name", description = "Updates the name of an existing storage. Only the storage owner can perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Storage updated successfully", content = {
                    @Content(schema = @Schema(implementation = Storage.class))
            }),
            @ApiResponse(responseCode = "403", description = "Access denied - Only owner can edit storage name", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid name - Name cannot be empty or exceed 40 characters", content = {
                    @Content(schema = @Schema(example = "{ \"name\": \"Name cannot be empty\" }"))
            }),
            @ApiResponse(responseCode = "404", description = "Storage with this ID was not found", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<?> changeName(
            @Parameter(description = "The unique identifier of the storage", example = "1") @PathVariable long id,
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
    @Operation(summary = "Delete a storage", description = "Deletes a storage and all its associated items and members. Only the storage owner can perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Storage deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Only owner can delete storage"),
            @ApiResponse(responseCode = "404", description = "Storage with this ID was not found")
    })
    public ResponseEntity<Void> deleteStorage(
            @Parameter(description = "The unique identifier of the storage", example = "1") @PathVariable long id,
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
