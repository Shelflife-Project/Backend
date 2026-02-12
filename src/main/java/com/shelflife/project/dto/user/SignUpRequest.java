package com.shelflife.project.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Setter;
import lombok.Getter;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Setter
@Getter
@Schema(description = "Sign up request with email, username and password")
public class SignUpRequest {

    @Email(message = "Invalid email")
    @NotBlank(message = "Email cannot be empty")
    @NotNull(message = "Email cannot be empty")
    @Schema(description = "User's email address", example = "newuser@example.com")
    private String email;

    @NotBlank(message = "Username cannot be empty")
    @NotNull(message = "Username cannot be empty")
    @Size(max = 40, message = "The username can only be 40 characters")
    @Schema(description = "Chosen username", example = "new_user")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @NotNull(message = "Password cannot be empty")
    @Size(min = 6, message = "The password should be at least 6 characters")
    @Schema(description = "Password for the new account", example = "strongPassword1")
    private String password;

    @NotBlank(message = "Repeat the password")
    @NotNull(message = "Repeat the password")
    @Schema(description = "Repeat the password for confirmation", example = "strongPassword1")
    private String passwordRepeat;
}
