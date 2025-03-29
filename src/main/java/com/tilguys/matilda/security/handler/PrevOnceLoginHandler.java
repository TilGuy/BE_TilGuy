package com.tilguys.matilda.security.handler;

import com.tilguys.matilda.config.jwt.JwtTokenFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class PrevOnceLoginHandler extends OncePerRequestFilter {

    private final JwtTokenFactory jwtTokenFactory;
    @Value("${oauth2.redirect.url}")
    private final String redirectUrl;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String userIdentifier = jwtTokenFactory.resolveJwtToken(request.getCookies());
        System.out.println(userIdentifier);
    }
}
