package com.tilguys.matilda.common.auth.strategy;

import jakarta.servlet.http.Cookie;
import org.springframework.security.core.Authentication;

public interface JwtCookieCreateStrategy {
    Cookie createCookie(Authentication authentication);
}
