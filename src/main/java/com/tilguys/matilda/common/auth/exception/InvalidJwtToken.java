package com.tilguys.matilda.common.auth.exception;

import io.jsonwebtoken.JwtException;

public class InvalidJwtToken extends JwtException {

    public InvalidJwtToken(String message) {
        super(message);
    }
}
