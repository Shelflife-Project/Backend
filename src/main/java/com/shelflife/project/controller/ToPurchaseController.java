package com.shelflife.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shelflife.project.dto.purchase.ToPurchaseItem;
import com.shelflife.project.service.StorageItemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/tobuy")
@Tag(name = "ToPurchase", description = "Endpoints for aggregated purchase lists")
public class ToPurchaseController {
    @Autowired
    private StorageItemService storageItemService;

    @GetMapping("")
    @Operation(summary = "Get aggregated to-buy list for current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Aggregated to-buy list returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<ToPurchaseItem>> getToBuy(Authentication auth) {
        return ResponseEntity.ok(storageItemService.getToPurchaseForUser(auth));
    }
}
