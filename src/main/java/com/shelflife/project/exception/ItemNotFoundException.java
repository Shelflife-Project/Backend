package com.shelflife.project.exception;

public class ItemNotFoundException extends RuntimeException {
    private String field;

    public ItemNotFoundException(String msg) {
        super(msg);
    }

    public ItemNotFoundException(String field, String msg) {
        super(msg);
        this.field = field;
    }

    public String getField() {
        if (field == null)
            return "unnamed field";

        return field;
    }
}
