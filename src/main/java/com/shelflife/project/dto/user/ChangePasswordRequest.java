package com.shelflife.project.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Getter
@Setter
@Schema(description = "Request to change current user's password")
public class ChangePasswordRequest {

    @NotBlank(message = "Old password cannot be empty")
    @NotNull(message = "Old password cannot be empty")
    @Schema(description = "Current password", example = "oldPassword123")
    private String oldPassword;

    @NotBlank(message = "New password cannot be empty")
    @NotNull(message = "New password cannot be empty")
    @Size(min = 6, message = "The new password should be at least 6 characters")
    @Schema(description = "New desired password", example = "newStrongPassword1")
    private String newPassword;

    @NotBlank(message = "Password repeat cannot be empty")
    @NotNull(message = "Password repeat cannot be empty")
    @Schema(description = "Repeat the new password", example = "newStrongPassword1")
    private String newPasswordRepeat;
}
