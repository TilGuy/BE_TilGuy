package com.tilguys.matilda.security.config;

import com.tilguys.matilda.config.jwt.Jwt;
import com.tilguys.matilda.config.jwt.JwtTokenFactory;
import com.tilguys.matilda.service.UserService;
import com.tilguys.matilda.user.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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

    private final JwtTokenFactory jwtTokenFactory;
    private final Jwt jwt;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = jwt.getTokenFromCookie(request);
        try {
            String identifier = jwt.getPrincipleFromToken(token);
            Optional<User> userByIdentifier = userService.findUserByIdentifier(identifier);
            if (userByIdentifier.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }

            Authentication authentication = createAuthentication(userByIdentifier.get());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (IllegalArgumentException ignored) {
        }
        filterChain.doFilter(request, response);
    }

    private Authentication createAuthentication(User user) {
        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(user.getRole().getKey()));
        return new UsernamePasswordAuthenticationToken(user.getIdentifier(), "", authorities);
    }
}
