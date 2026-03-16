package com.shelflife.project.docs;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.shelflife.project.model.ShoppingListItem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Shopping List Aggregate", description = "Aggregated shopping list across storages accessible to the user")
public interface ShoppingListAggregateControllerDocs {
    @Operation(summary = "Get aggregated shopping list for user", description = "Returns every shopping list item that the user has access to")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<ShoppingListItem>> getAggregated(Authentication auth);
}
