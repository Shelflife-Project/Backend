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
@Schema(description = "Request payload for creating a new running low setting")
public class CreateSettingRequest {
    @Min(value = 1, message = "Product ID must be larger than 0")
    @Schema(
        description = "The unique identifier of the product to set a running low threshold for",
        example = "5",
        minimum = "1"
    )
    private long productId;

    @Min(value = 0, message = "Running low value must be a positive integer")
    @Schema(
        description = "The quantity threshold at which the product is considered running low",
        example = "10",
        minimum = "0"
    )
    private int runningLow;
}
