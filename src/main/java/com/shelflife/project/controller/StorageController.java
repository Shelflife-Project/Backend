package com.shelflife.project.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shelflife.project.dto.storage.ChangeStorageNameRequest;
import com.shelflife.project.dto.storage.CreateStorageRequest;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Storage;
import com.shelflife.project.service.StorageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/storages")
public class StorageController {
    @Autowired
    private StorageService storageService;

    @GetMapping()
    @Operation(summary = "Get owned and member storages")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved storages"),
            @ApiResponse(responseCode = "403", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<List<Storage>> getStorages(Authentication auth) {
        try {
            return ResponseEntity.ok(storageService.getStorages(auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a storage by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Succssfully retrieved storage", content = {
                    @Content(schema = @Schema(implementation = Storage.class))
            }),
            @ApiResponse(responseCode = "403", description = "Access denied", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "404", description = "Storage with this ID was not found", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<?> getStorage(@PathVariable long id, Authentication auth) {
        try {
            return ResponseEntity.ok(storageService.getStorage(auth, id));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(e.getField(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping()
    @Operation(summary = "Create a storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created storage", content = {
                    @Content(schema = @Schema(implementation = Storage.class))
            }),
            @ApiResponse(responseCode = "403", description = "Access denied", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid name", content = {
                    @Content(schema = @Schema(example = "{ \"name\": \"Name cannot be empty\" }"))
            })
    })
    public ResponseEntity<?> createStorage(@Valid @RequestBody CreateStorageRequest request,
            Authentication auth) {
        try {
            return ResponseEntity.ok(storageService.createStorage(request, auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("name", "Name cannot be empty"));
        }
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Edit a storages name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Storage updated successfully", content = {
                    @Content(schema = @Schema(implementation = Storage.class))
            }),
            @ApiResponse(responseCode = "403", description = "Access denied", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid name", content = {
                    @Content(schema = @Schema(example = "{ \"name\": \"Name cannot be empty\" }"))
            }),
            @ApiResponse(responseCode = "404", description = "Storage with this ID was not found", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<?> changeName(@PathVariable long id, @Valid @RequestBody ChangeStorageNameRequest request,
            Authentication auth) {
        try {
            return ResponseEntity.ok(storageService.changeName(id, request, auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(e.getMessage(), "Name cannot be empty"));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Storage deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Storage with this ID was not found")
    })
    public ResponseEntity<Void> deleteStorage(@PathVariable long id, Authentication auth) {
        try {
            storageService.deleteStorageRequest(id, auth);
            return ResponseEntity.ok().build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
