package com.shelflife.project.docs;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.shelflife.project.dto.storage.InviteMemberRequest;
import com.shelflife.project.model.StorageMember;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface StorageMemberControllerDocs {
    @Operation(summary = "Get the members of a storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved members"),
            @ApiResponse(responseCode = "403", description = "You can't access this storage", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "404", description = "Storage with this ID was not found", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<List<StorageMember>> getStorageMembers(long storageId, Authentication auth);

    @Operation(summary = "Invite a member to the storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved members", content = {
                    @Content(schema = @Schema(implementation = StorageMember.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad request", content = {
                    @Content(schema = @Schema(example = "{ \"email\": \"User is already a member or was already invited\" }"))
            }),
            @ApiResponse(responseCode = "403", description = "You can't access this storage", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "404", description = "Storage with this ID was not found or User with the email was not found", content = {
                    @Content(schema = @Schema(example = "{ \"email\": \"User with this email was not found\" }"))
            })
    })
    public ResponseEntity<?> inviteMember(long storageId, InviteMemberRequest request, Authentication auth);

    @Operation(summary = "Kick a member from the storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved members"),
            @ApiResponse(responseCode = "403", description = "You can't access this storage", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "404", description = "Storage or user with this ID was not found", content = {
                    @Content(schema = @Schema(example = "{ \"member\": \"Member was not found\" }"))
            })
    })
    public ResponseEntity<?> deleteMember(long storageId, long userId, Authentication auth);
}
