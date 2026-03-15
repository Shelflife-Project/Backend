package com.shelflife.project.docs;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.shelflife.project.model.StorageItem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface ExpireControllerDocs {

    @Operation(summary = "Get expired items in a storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of expired items"),
            @ApiResponse(responseCode = "403", description = "You can't access this storage", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "404", description = "Storage not found", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<List<StorageItem>> getExpiredInStorage(long storageId, Authentication auth);

    @Operation(summary = "Get items that expire tomorrow in a storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of items"),
            @ApiResponse(responseCode = "403", description = "You can't access this storage", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "404", description = "Storage not found", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<List<StorageItem>> getAboutToExpireInStorage(long storageId, Authentication auth);

    @Operation(summary = "Get all expired items aggregated for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of expired items"),
            @ApiResponse(responseCode = "403", description = "Access denied", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<List<StorageItem>> getExpired(Authentication auth);

    @Operation(summary = "Get all items about to expire aggregated for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of items"),
            @ApiResponse(responseCode = "403", description = "Access denied", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<List<StorageItem>> getAboutToExpire(Authentication auth);
}
