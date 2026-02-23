package com.shelflife.project.exception;

public class ShoppingItemExistsException extends IllegalArgumentException {
    public ShoppingItemExistsException(String msg)
    {
        super(msg);
    }
}
