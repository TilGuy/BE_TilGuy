package com.tilguys.matilda.security.handler;

import com.tilguys.matilda.config.jwt.JwtTokenFactory;
import com.tilguys.matilda.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class PrevOnceLoginHandler extends OncePerRequestFilter {
    private final JwtTokenFactory jwtTokenFactory;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = jwtTokenFactory.resolveJwtToken(request.getCookies());
        Authentication authentication = jwtTokenFactory.getAuthentication(token);
        String identifier = authentication.getName();
        userService.validateExistUser(identifier);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
