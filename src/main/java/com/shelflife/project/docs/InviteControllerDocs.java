package com.shelflife.project.docs;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.shelflife.project.model.StorageMember;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Invite", description = "Endpoints for handling your invites")
public interface InviteControllerDocs {
    @Operation(summary = "Get invites that were not accepted yet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns your invites"),
            @ApiResponse(responseCode = "403", description = "You are not logged in", content = @Content(schema = @Schema(implementation = Void.class)))
    })
    public ResponseEntity<List<StorageMember>> getInvites(Authentication auth);

    @Operation(summary = "Accept an invite")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful accept"),
            @ApiResponse(responseCode = "403", description = "You can't modify this invite", content = @Content(schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "404", description = "Invite with id was not found", content = @Content(schema = @Schema(implementation = Void.class)))
    })
    public ResponseEntity<?> acceptInvite(long id, Authentication auth);

    @Operation(summary = "Decline an invite")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful decline"),
            @ApiResponse(responseCode = "403", description = "You can't modify this invite", content = @Content(schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "404", description = "Invite with id was not found", content = @Content(schema = @Schema(implementation = Void.class)))
    })
    public ResponseEntity<?> declineInvite(long id, Authentication auth);
}
