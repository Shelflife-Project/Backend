package com.shelflife.project.dto.runninglow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class EditSettingRequest {
    private int runningLow;
}
