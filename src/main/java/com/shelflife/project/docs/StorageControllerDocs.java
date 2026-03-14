package com.shelflife.project.docs;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.shelflife.project.dto.storage.ChangeStorageNameRequest;
import com.shelflife.project.dto.storage.CreateStorageRequest;
import com.shelflife.project.model.Storage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Storage Management", description = "APIs for managing storage resources. All endpoints require authentication.")
@SecurityRequirement(name = "bearerAuth")
public interface StorageControllerDocs {
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
            @RequestParam(defaultValue = "true") boolean ascending);

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
            @Parameter(description = "The unique identifier of the storage", example = "1") long id,
            Authentication auth);

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
    public ResponseEntity<?> createStorage(CreateStorageRequest request, Authentication auth);

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
            @Parameter(description = "The unique identifier of the storage", example = "1") long id,
            ChangeStorageNameRequest request,
            Authentication auth);

    @Operation(summary = "Delete a storage", description = "Deletes a storage and all its associated items and members. Only the storage owner can perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Storage deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Only owner can delete storage"),
            @ApiResponse(responseCode = "404", description = "Storage with this ID was not found")
    })
    public ResponseEntity<Void> deleteStorage(
            @Parameter(description = "The unique identifier of the storage", example = "1") long id,
            Authentication auth);
}
