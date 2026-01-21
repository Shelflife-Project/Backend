package com.shelflife.project.exception;

import lombok.Getter;

@Getter
public class RunningLowExistsException extends RuntimeException {
    public RunningLowExistsException(String msg) {
        super(msg);
    }
}
