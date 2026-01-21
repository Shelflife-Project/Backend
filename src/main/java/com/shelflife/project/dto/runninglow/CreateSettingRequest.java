package com.shelflife.project.dto.runninglow;

import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CreateSettingRequest {
    @Min(value = 1, message = "Product ID must be larger than 0")
    private long productId;

    @Min(value = 0, message = "Running low value must be a positive integer")
    private int runningLow;
}
