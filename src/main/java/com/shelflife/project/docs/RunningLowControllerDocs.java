package com.shelflife.project.docs;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.shelflife.project.dto.runninglow.CreateSettingRequest;
import com.shelflife.project.dto.runninglow.EditSettingRequest;
import com.shelflife.project.model.RunningLowSetting;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Running Low Settings", description = "APIs for managing running low thresholds for products in storages. All endpoints require authentication and storage access.")
@SecurityRequirement(name = "bearerAuth")
public interface RunningLowControllerDocs {
    @Operation(summary = "Get running low settings", description = "Retrieves all running low settings for a specific storage. User must have access to the storage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved settings", content = {
                    @Content(schema = @Schema(example = "[{\"id\": 1, \"storage\": {\"id\": 1, \"name\": \"Kitchen\"}, \"product\": {\"id\": 5, \"name\": \"Milk\"}, \"runningLow\": 2}, {\"id\": 2, \"storage\": {\"id\": 1, \"name\": \"Kitchen\"}, \"product\": {\"id\": 7, \"name\": \"Eggs\"}, \"runningLow\": 6}]"))
            }),
            @ApiResponse(responseCode = "403", description = "Access denied - User must be storage owner or member", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<List<RunningLowSetting>> getSettings(
            @Parameter(description = "The unique identifier of the storage", example = "1") long storageId,
            Authentication auth);

    @Operation(summary = "Create a running low setting", description = "Creates a new running low alert threshold for a product in the specified storage. One running low setting per product per storage is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Setting created successfully", content = {
                    @Content(schema = @Schema(implementation = RunningLowSetting.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid input or setting already exists for this product", content = {
                    @Content(schema = @Schema(example = "{ \"productId\": \"A setting for this product already exists in this storage\" }"))
            }),
            @ApiResponse(responseCode = "403", description = "Access denied - User must be storage owner or member", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "404", description = "Storage or product not found", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<?> createSetting(
            @Parameter(description = "The unique identifier of the storage", example = "1") long storageId,
            CreateSettingRequest request,
            Authentication auth);

    @Operation(summary = "Edit a running low setting", description = "Updates the running low threshold value for an existing setting. User must have access to the storage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Setting updated successfully", content = {
                    @Content(schema = @Schema(implementation = RunningLowSetting.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid input - Running low value must be positive", content = {
                    @Content(schema = @Schema(example = "{ \"runningLow\": \"Running low should be a positive number\" }"))
            }),
            @ApiResponse(responseCode = "403", description = "Access denied - User must be storage owner or member", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "404", description = "Setting with this ID was not found", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<?> editSetting(
            @Parameter(description = "The unique identifier of the running low setting", example = "1") long id,
            EditSettingRequest request,
            Authentication auth);

    @Operation(summary = "Delete a running low setting", description = "Deletes a running low setting. User must have access to the storage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Setting deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - User must be storage owner or member"),
            @ApiResponse(responseCode = "404", description = "Setting with this ID was not found")
    })
    public ResponseEntity<Void> deleteSetting(
            @Parameter(description = "The unique identifier of the running low setting", example = "1") long id,
            Authentication auth);
}
