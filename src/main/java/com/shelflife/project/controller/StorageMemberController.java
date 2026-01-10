package com.shelflife.project.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shelflife.project.dto.InviteMemberRequest;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.exception.MemberException;
import com.shelflife.project.service.StorageMemberService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/storages/{storageId}/members")
public class StorageMemberController {
    @Autowired
    private StorageMemberService storageMemberService;

    @GetMapping
    public ResponseEntity<?> getStorageMembers(@PathVariable long storageId, Authentication auth) {
        try {
            return ResponseEntity.ok(storageMemberService.getStorageMembers(storageId, auth));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(e.getField(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> inviteMember(@PathVariable long storageId, @Valid @RequestBody InviteMemberRequest request,
            Authentication auth) {
        try {
            return ResponseEntity
                    .ok(storageMemberService.inviteMemberToStorage(storageId, request.getEmail(), auth));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(e.getField(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (MemberException e) {
            return ResponseEntity.badRequest().body(Map.of("email", "User is already a member or was already invited"));
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteMember(@PathVariable long storageId, @PathVariable long userId,
            Authentication auth) {
        try {
            storageMemberService.removeMemberFromStorage(storageId, userId, auth);
            return ResponseEntity.ok().build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(e.getField(), e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
