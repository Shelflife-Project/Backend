package com.shelflife.project.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class ChangeStorageNameRequest {
    @NotEmpty
    @Length(max = 40, message = "The name can only be 40 characters long")
    private String name;
}
