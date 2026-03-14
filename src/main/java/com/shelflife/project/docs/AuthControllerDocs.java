package com.shelflife.project.docs;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;

import com.shelflife.project.docs.AuthControllerDocs;
import com.shelflife.project.dto.user.ChangePasswordRequest;
import com.shelflife.project.dto.user.LoginRequest;
import com.shelflife.project.dto.user.SignUpRequest;
import com.shelflife.project.model.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Tag(name = "Authentication", description = "Endpoints for user authentication and profile")
public interface AuthControllerDocs {

	@Operation(summary = "Log in with email and password")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful login", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
			@ApiResponse(responseCode = "403", description = "You are already logged in", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Void.class))),
			@ApiResponse(responseCode = "400", description = "Couldn't log in", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Void.class)))
	})
	public ResponseEntity<?> login(
			@Valid @Parameter(description = "Login Data, including email and password", schema = @Schema(implementation = LoginRequest.class), examples = @ExampleObject(value = "{\"email\": \"user@example.com\", \"password\": \"password123\"}")) @RequestBody LoginRequest request,
			HttpServletResponse response,
			Authentication auth);

	@Operation(summary = "Sign up with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful signup", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)) }),
            @ApiResponse(responseCode = "403", description = "Won't sign up, already logged in", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = {
                    @Content(mediaType = "application/json") })
    })
    public ResponseEntity<?> signup(
            @Valid @Parameter(description = "Sign up Data, including email, password and password repeat") @RequestBody SignUpRequest request,
            HttpServletResponse response,
            Authentication auth);

	@Operation(summary = "Log out of the current session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful logout"),
            @ApiResponse(responseCode = "403", description = "You are not logged in"),
    })
    public ResponseEntity<?> logout(
            @Parameter(description = "Authentication information of the user") Authentication auth,
            HttpServletResponse response);

	@Operation(summary = "Change the password of the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful password change"),
            @ApiResponse(responseCode = "403", description = "You are not logged in"),
            @ApiResponse(responseCode = "400", description = "Invalid old password, or new passwords not matching")
    })
    public ResponseEntity<?> changePassword(Authentication auth, @Valid @RequestBody ChangePasswordRequest request);

    @Operation(summary = "Get current user", description = "Retrieve profile of the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "You are logged in", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class), examples = @ExampleObject(value = "{\"id\": 1, \"username\": \"jane_doe\", \"admin\": false}"))),
            @ApiResponse(responseCode = "403", description = "You are not logged in", content = @Content(schema = @Schema(implementation = Void.class)))
    })
    public ResponseEntity<User> getMe(HttpServletResponse response, Authentication auth);
}