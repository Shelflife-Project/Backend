package com.shelflife.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class InviteMemberRequest {
    @Email(message = "Invalid format")
    @NotEmpty(message = "User Email must not be empty")
    private String email;
}
