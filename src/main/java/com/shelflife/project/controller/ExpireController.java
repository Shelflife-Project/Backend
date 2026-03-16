package com.shelflife.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shelflife.project.docs.ExpireControllerDocs;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.StorageItem;
import com.shelflife.project.model.User;
import com.shelflife.project.service.ExpirationGetterService;
import com.shelflife.project.service.UserService;

@RestController
@RequestMapping("/api")
public class ExpireController implements ExpireControllerDocs {

    @Autowired
    private UserService userService;

    @Autowired
    private ExpirationGetterService expirationGetterService;

    @GetMapping("/storages/{storageId}/expired")
    public ResponseEntity<List<StorageItem>> getExpiredInStorage(@PathVariable long storageId, Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            return ResponseEntity.ok(expirationGetterService.getExpiredItemsInStorage(storageId, user));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/storages/{storageId}/abouttoexpire")
    public ResponseEntity<List<StorageItem>> getAboutToExpireInStorage(@PathVariable long storageId,
            Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            return ResponseEntity.ok(expirationGetterService.getItemsAboutToExpire(storageId, user));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/expired")
    public ResponseEntity<List<StorageItem>> getExpired(Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            return ResponseEntity.ok(expirationGetterService.getExpiredItemsAggregated(user));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/abouttoexpire")
    public ResponseEntity<List<StorageItem>> getAboutToExpire(Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            return ResponseEntity.ok(expirationGetterService.getItemsAboutToExpireAggregated(user));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
