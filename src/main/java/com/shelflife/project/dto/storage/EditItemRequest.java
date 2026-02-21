package com.shelflife.project.dto.storage;

import java.time.LocalDate;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class EditItemRequest {
    private LocalDate expiresAt;
    private Integer amountToBuy;
}
