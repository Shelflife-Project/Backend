package com.shelflife.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.shelflife.project.dto.purchase.ToPurchaseItem;
import com.shelflife.project.service.ShoppingListService;

@RestController
@RequestMapping("/api/tobuy/shopping")
@Tag(name = "Shopping List Aggregate", description = "Aggregated shopping list across storages accessible to the user")
public class ShoppingListAggregateController {
    @Autowired
    private ShoppingListService service;

    @GetMapping("")
    @Operation(summary = "Get aggregated shopping list for user", description = "Returns aggregated products and total amount to buy across all storages the user has access to")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<ToPurchaseItem>> getAggregated(Authentication auth) {
        return ResponseEntity.ok(service.getAggregatedForUser(auth));
    }
}
