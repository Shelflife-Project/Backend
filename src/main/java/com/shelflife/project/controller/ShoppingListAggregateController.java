package com.shelflife.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shelflife.project.dto.purchase.ToPurchaseItem;
import com.shelflife.project.service.ShoppingListService;

@RestController
@RequestMapping("/api/tobuy/shopping")
public class ShoppingListAggregateController {
    @Autowired
    private ShoppingListService service;

    @GetMapping("")
    public ResponseEntity<List<ToPurchaseItem>> getAggregated(Authentication auth) {
        return ResponseEntity.ok(service.getAggregatedForUser(auth));
    }
}
