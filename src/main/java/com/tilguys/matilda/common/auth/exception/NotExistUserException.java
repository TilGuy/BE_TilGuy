package com.tilguys.matilda.common.auth.exception;

public class NotExistUserException extends MatildaException {
    
    public NotExistUserException() {
        super("유저를 찾을 수 없습니다.");
    }
}
