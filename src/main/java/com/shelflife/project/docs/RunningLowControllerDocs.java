package com.shelflife.project.docs;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.shelflife.project.dto.runninglow.RunningLowNotification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface RunningLowControllerDocs {
    
    @Operation(summary = "Get items that are running low in a storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of items"),
            @ApiResponse(responseCode = "403", description = "You can't access this storage", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<List<RunningLowNotification>> getRunningLow(long storageId,
            Authentication auth);

    @Operation(summary = "Get all items running low aggregated for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of items"),
            @ApiResponse(responseCode = "403", description = "Access denied", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<List<RunningLowNotification>> getAggregatedRunningLow(Authentication auth);
}
