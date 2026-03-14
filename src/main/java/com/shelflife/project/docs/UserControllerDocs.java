package com.shelflife.project.docs;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import com.shelflife.project.dto.user.ChangeUserDataRequest;
import com.shelflife.project.model.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserControllerDocs {
    @Operation(summary = "Get all users", description = "Only for admins")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
            @ApiResponse(responseCode = "403", description = "Access denied", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<List<User>> getUsers(Authentication auth);

    @Operation(summary = "Get user by ID", description = "Retrieve a user by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user"),
            @ApiResponse(responseCode = "404", description = "User not found", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<User> getUser(long id, Authentication auth);

    @Operation(summary = "Get the icon of a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "image/*")
            }, description = "Returns an image with it's own mime type header. If there is no uploaded image, a placeholder svg will be returned"),
    })
    public ResponseEntity<Resource> getPfp(long id);

    @Operation(summary = "Get optimized 64x64 icon of a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "image/*")
            }, description = "Returns an optimized image (max 64x64). If there is no uploaded image, a placeholder will be returned"),
    })
    public ResponseEntity<byte[]> getSmallPfp(long id);

    @Operation(summary = "Upload an icon for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "500", description = "An IO Exception occured", content = {
                    @Content(schema = @Schema(example = "{ \"error\": \"Couldn't upload image\" }"))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid mime type", content = {
                    @Content(schema = @Schema(example = "{ \"pfp\": \"Invalid mime type\" }"))
            }),
    })
    public ResponseEntity<?> uploadPfp(long id, MultipartFile file, Authentication auth);

    @Operation(summary = "Delete a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful removal"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteUser(long id, Authentication auth);

    @Operation(summary = "Update user data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful update", content = {
                    @Content(schema = @Schema(implementation = User.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid argument", content = {
                    @Content(schema = @Schema(example = "{ \"email\": \"Email already exists\" }"))
            }),
            @ApiResponse(responseCode = "403", description = "You can't edit this user", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            }),
            @ApiResponse(responseCode = "404", description = "User not found", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<?> updateUserData(long id, Authentication auth,
            ChangeUserDataRequest request, HttpServletRequest httpRequest,
            HttpServletResponse response);
}
