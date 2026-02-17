package com.shelflife.project.dto.runninglow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Request payload for updating a running low setting")
public class EditSettingRequest {
    @Min(value = 0, message = "Running low value must be a positive integer")
    @Schema(
        description = "The new quantity threshold at which the product is considered running low",
        example = "15",
        minimum = "0"
    )
    private int runningLow;
}
