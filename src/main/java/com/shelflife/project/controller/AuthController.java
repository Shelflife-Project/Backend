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

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful login"),
            @ApiResponse(responseCode = "400", description = "Couldn't log in")
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response,
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
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid email or password"));
        }
    }

    @PostMapping("/signup")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful signup", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)) }),
            @ApiResponse(responseCode = "403", description = "Won't sign up, already logged in"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = {
                    @Content(mediaType = "application/json")
            })
    })
    public ResponseEntity<?> signup(@Valid @RequestBody SignUpRequest request, HttpServletResponse response,
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

    @PostMapping("/logout")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful logout"),
            @ApiResponse(responseCode = "400", description = "You are not logged in")
    })
    public ResponseEntity<?> logout(Authentication auth) {
        try {
            userService.logout(auth);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "You are not logged in"));
        }
    }

    @PostMapping("/password/change")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful password change"),
            @ApiResponse(responseCode = "403", description = "You are not logged in"),
            @ApiResponse(responseCode = "400", description = "Invalid old password, or new passwords not matching")
    })
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "You are logged in", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)) }),
            @ApiResponse(responseCode = "403", description = "You are not logged in", content = @Content())
    })
    public ResponseEntity<User> getMe(HttpServletResponse response, Authentication auth) {
        Optional<User> self = userService.getOptionalUserByAuth(auth);

        if (!self.isPresent())
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        return ResponseEntity.ok(self.get());
    }
}
