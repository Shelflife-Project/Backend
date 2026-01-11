package com.shelflife.project.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shelflife.project.model.StorageMember;
import com.shelflife.project.service.StorageMemberService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/storages/invites")
public class InviteController {
    @Autowired
    private StorageMemberService service;

    @GetMapping
    public ResponseEntity<List<StorageMember>> getInvites(Authentication auth) {
        try {
            return ResponseEntity.ok(service.getInvites(auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

}
