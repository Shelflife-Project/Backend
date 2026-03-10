package com.shelflife.project.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.shelflife.project.dto.user.ChangeUserDataRequest;
import com.shelflife.project.exception.EmailExistsException;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Image;
import com.shelflife.project.model.User;
import com.shelflife.project.service.ImageService;
import com.shelflife.project.service.JwtService;
import com.shelflife.project.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService service;

    @Autowired
    private ImageService imageService;

    @GetMapping()
    @Operation(summary = "Get all users", description = "Only for admins")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
            @ApiResponse(responseCode = "403", description = "Access denied", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<List<User>> getUsers(Authentication auth) {
        try {
            List<User> users = service.getUsers(service.getUserByAuth(auth));
            return ResponseEntity.ok(users);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a user by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user"),
            @ApiResponse(responseCode = "404", description = "User not found", content = {
                    @Content(schema = @Schema(implementation = Void.class))
            })
    })
    public ResponseEntity<User> getUser(@PathVariable long id, Authentication auth) {
        try {
            return ResponseEntity.ok(service.getUserById(id, auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/pfp")
    @Operation(summary = "Get the icon of a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "image/*")
            }, description = "Returns an image with it's own mime type header. If there is no uploaded image, a placeholder svg will be returned"),
    })
    public ResponseEntity<Resource> getPfp(@PathVariable long id) {
        String filename = id + "_user";
        Resource resource = imageService.loadImage(filename, "classpath:avatar-default.svg");

        try {
            Image image = imageService.getImage(filename);

            if (!resource.getFilename().equals(filename))
                throw new ItemNotFoundException("image", "Image file was not found");

            return ResponseEntity.ok().header("Content-Type", image.getMimetype()).body(resource);

        } catch (ItemNotFoundException e) {
            return ResponseEntity.ok().header("Content-Type", "image/svg+xml").body(resource);
        }
    }

    @PostMapping("/{id}/pfp")
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
    public ResponseEntity<?> uploadPfp(@PathVariable long id, @RequestParam("pfp") MultipartFile file,
            Authentication auth) {
        try {
            User user = service.getUserByAuth(auth);
            if (user.getId() != id)
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

            imageService.uploadImage(file, user.getId() + "_user");
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        } catch (InvalidMimeTypeException e) {
            return ResponseEntity.badRequest().body(Map.of("pfp", "Invalid mime type"));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful removal"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable long id, Authentication auth) {
        try {
            service.removeUser(id, service.getUserByAuth(auth));
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}")
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
    public ResponseEntity<?> updateUserData(@PathVariable long id, Authentication auth,
            @Valid @RequestBody ChangeUserDataRequest request, HttpServletRequest httpRequest,
            HttpServletResponse response) {

        try {
            long selfId = service.getUserByAuth(auth).getId();
            User updated = service.updateUser(id, request, service.getUserByAuth(auth));

            if (request.getEmail() != null && updated.getId() == selfId) {
                jwtService.invalidateToken((String) auth.getCredentials());

                final Cookie cookie = new Cookie("jwt", jwtService.generateToken(updated.getEmail()));
                cookie.setSecure(true);
                cookie.setHttpOnly(true);
                cookie.setMaxAge(24 * 60 * 60);
                cookie.setPath("/");

                response.addCookie(cookie);
            }

            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(e.getMessage(), "Invalid input"));
        } catch (EmailExistsException e) {
            return ResponseEntity.badRequest().body(Map.of("email", "This email is already used"));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
