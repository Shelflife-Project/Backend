package com.shelflife.project.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Getter
@Setter
@Schema(description = "Login request containing user email and password")
public class LoginRequest {
    @Email(message = "Invalid email")
    @NotBlank(message = "Email cannot be empty")
    @NotNull(message = "Email cannot be empty")
    @Schema(description = "User's email address", example = "user@example.com")
    private String email;

    @Schema(description = "User's password", example = "password123")
    private String password;
}
