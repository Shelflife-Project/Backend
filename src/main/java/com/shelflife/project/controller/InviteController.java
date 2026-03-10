package com.shelflife.project.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.StorageMember;
import com.shelflife.project.model.User;
import com.shelflife.project.service.StorageMemberService;
import com.shelflife.project.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/storages/invites")
public class InviteController {
    @Autowired
    private StorageMemberService service;

    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(summary = "Get invites that were not accepted yet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns your invites"),
            @ApiResponse(responseCode = "403", description = "You are not logged in", content = @Content(schema = @Schema(implementation = Void.class)))
    })
    public ResponseEntity<List<StorageMember>> getInvites(Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            return ResponseEntity.ok(service.getInvites(user));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/{id}")
    @Operation(summary = "Accept an invite")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful accept"),
            @ApiResponse(responseCode = "403", description = "You can't modify this invite", content = @Content(schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "404", description = "Invite with id was not found", content = @Content(schema = @Schema(implementation = Void.class)))
    })
    public ResponseEntity<?> acceptInvite(@PathVariable long id, Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            service.acceptInvite(id, user);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Decline an invite")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful decline"),
            @ApiResponse(responseCode = "403", description = "You can't modify this invite", content = @Content(schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "404", description = "Invite with id was not found", content = @Content(schema = @Schema(implementation = Void.class)))
    })
    public ResponseEntity<?> declineInvite(@PathVariable long id, Authentication auth) {
        try {
            User user = userService.getUserByAuth(auth);
            service.declineInvite(id, user);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
