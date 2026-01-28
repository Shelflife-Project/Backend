package com.shelflife.project.dto.runninglow;

import com.shelflife.project.model.Product;
import com.shelflife.project.model.Storage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class RunningLowNotification {
    private Storage storage;
    private Product product;
    private int runningLowAt;
    private long amount;
}
