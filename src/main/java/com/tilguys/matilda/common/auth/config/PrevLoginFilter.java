package com.tilguys.matilda.common.auth.config;

import com.tilguys.matilda.common.auth.Jwt;
import com.tilguys.matilda.common.auth.service.UserService;
import com.tilguys.matilda.user.TilUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class PrevLoginFilter extends OncePerRequestFilter {

    private final Jwt jwt;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();
        String token = jwt.getTokenFromCookie(cookies);

        try {
            jwt.validateToken(token);
            String identifier = jwt.getPrincipleFromToken(token);
            Optional<TilUser> userByIdentifier = userService.findUserByIdentifier(identifier);
            if (userByIdentifier.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }

            Authentication authentication = createAuthentication(userByIdentifier.get());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (RuntimeException ignore) {
        }
        filterChain.doFilter(request, response);
    }

    private Authentication createAuthentication(TilUser tilUser) {
        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(tilUser.getRole().getKey()));
        return new UsernamePasswordAuthenticationToken(tilUser.getIdentifier(), "", authorities);
    }
}
