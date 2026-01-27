package com.shelflife.project.dto.runninglow;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class EditSettingRequest {
    @Min(value = 0, message = "Running low value must be a positive integer")
    private int runningLow;
}
