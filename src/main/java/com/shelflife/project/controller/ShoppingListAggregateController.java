package com.shelflife.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shelflife.project.docs.ShoppingListAggregateControllerDocs;
import com.shelflife.project.model.ShoppingListItem;
import com.shelflife.project.model.User;
import com.shelflife.project.service.ShoppingListService;
import com.shelflife.project.service.UserService;

@RestController
@RequestMapping("/api/shoppinglist")
public class ShoppingListAggregateController implements ShoppingListAggregateControllerDocs {
    @Autowired
    private ShoppingListService service;

    @Autowired
    private UserService userService;

    @GetMapping("")
    public ResponseEntity<List<ShoppingListItem>> getAggregated(Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            return ResponseEntity.ok(service.getShoppingListItemsAggregated(user));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
