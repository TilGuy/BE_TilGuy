package com.tilguys.matilda.common.auth.exception;

public class DoesNotExistUserException extends RuntimeException {

    public DoesNotExistUserException() {
        super("존재하지 않는 유저입니다");
    }
}
