package com.shelflife.project.exception;

import lombok.Getter;

@Getter
public class ItemNotFoundException extends RuntimeException {
    private String field;

    public ItemNotFoundException(String msg) {
        super(msg);
    }

    public ItemNotFoundException(String field, String msg) {
        super(msg);
        this.field = field;
    }
}
