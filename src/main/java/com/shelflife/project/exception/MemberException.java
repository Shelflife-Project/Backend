package com.shelflife.project.exception;

import lombok.Getter;

@Getter
public class MemberException extends RuntimeException {
    private boolean isMember;

    public MemberException(boolean isMember) {
        this.isMember = isMember;
    }
}
