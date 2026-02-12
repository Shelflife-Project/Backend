package com.shelflife.project.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shelflife.project.dto.user.ChangePasswordRequest;
import com.shelflife.project.dto.user.LoginRequest;
import com.shelflife.project.dto.user.SignUpRequest;
import com.shelflife.project.exception.EmailExistsException;
import com.shelflife.project.exception.InvalidPasswordException;
import com.shelflife.project.exception.PasswordsDontMatchException;
import com.shelflife.project.model.User;
import com.shelflife.project.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and profile")
public class AuthController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Log in with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful login", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "403", description = "You are already logged in", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "400", description = "Couldn't log in", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Void.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @Parameter(description = "Login Data, including email and password", schema = @Schema(implementation = LoginRequest.class), examples = @ExampleObject(value = "{\"email\": \"user@example.com\", \"password\": \"password123\"}")) @RequestBody LoginRequest request, HttpServletResponse response,
            Authentication auth) {
        try {
            String token = userService.login(request, auth);

            final Cookie cookie = new Cookie("jwt", token);
            cookie.setSecure(true);
            cookie.setHttpOnly(true);
            cookie.setMaxAge(24 * 60 * 60);
            cookie.setPath("/");
            response.addCookie(cookie);

            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You are already logged in"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid email or password"));
        }
    }

    @Operation(summary = "Sign up with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful signup", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = User.class)) }),
            @ApiResponse(responseCode = "403", description = "Won't sign up, already logged in", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = {@Content(mediaType = "application/json")})
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @Parameter(description = "Sign up Data, including email, password and password repeat") @RequestBody SignUpRequest request, HttpServletResponse response,
            Authentication auth) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(userService.signUp(request, auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Already logged in"));
        } catch (EmailExistsException e) {
            return ResponseEntity.badRequest().body(Map.of("email", "Email already exists"));
        } catch (PasswordsDontMatchException e) {
            return ResponseEntity.badRequest().body(Map.of("passwordRepeat", "The passwords are not the same"));
        }
    }

    @Operation(summary = "Log out of the current session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful logout"),
            @ApiResponse(responseCode = "403", description = "You are not logged in"),  
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Parameter(description = "Authentication information of the user") Authentication auth, HttpServletResponse response) {
        final Cookie cookie = new Cookie("jwt", null);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        try {
            userService.logout(auth);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You are not logged in"));
        }
    }
 
    @Operation(summary = "Change the password of the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful password change"),
            @ApiResponse(responseCode = "403", description = "You are not logged in"),
            @ApiResponse(responseCode = "400", description = "Invalid old password, or new passwords not matching")
    })
    @PostMapping("/password/change")
    public ResponseEntity<?> changePassword(Authentication auth, @Valid @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(request, auth);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (InvalidPasswordException e) {
            return ResponseEntity.badRequest().body(Map.of("oldPassword", "Invalid old password"));
        } catch (PasswordsDontMatchException e) {
            return ResponseEntity.badRequest().body(Map.of("newPasswordRepeat", "The passwords are not the same"));
        }
    }

    @GetMapping("/me")
        @Operation(summary = "Get current user", description = "Retrieve profile of the authenticated user")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "You are logged in", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class), examples = @ExampleObject(value = "{\"id\": 1, \"username\": \"jane_doe\", \"admin\": false}"))),
            @ApiResponse(responseCode = "403", description = "You are not logged in", content = @Content(schema = @Schema(implementation = Void.class)))
        })
        public ResponseEntity<User> getMe(HttpServletResponse response, Authentication auth) {
        Optional<User> self = userService.getOptionalUserByAuth(auth);

        if (!self.isPresent())
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        return ResponseEntity.ok(self.get());
    }
}
