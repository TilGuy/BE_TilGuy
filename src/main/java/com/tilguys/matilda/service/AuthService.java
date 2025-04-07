package com.tilguys.matilda.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    public Authentication createAuthenticationFromName(String identifier) {
        try {
            Optional<com.tilguys.matilda.user.entity.User> userByIdentifier = userService.findUserByIdentifier(
                    identifier);
            if (userByIdentifier.isEmpty()) {
                userService.signup(identifier);
                userByIdentifier = userService.findUserByIdentifier(identifier);
            }

            Collection<? extends GrantedAuthority> authorities =
                    Arrays.stream(userByIdentifier.get().getRole().toString().split(","))
                            .map(SimpleGrantedAuthority::new)
                            .toList();

            UserDetails principal = new User(userByIdentifier.get().getIdentifier(), "", authorities);
            return new UsernamePasswordAuthenticationToken(principal, "", authorities);
        } catch (RuntimeException e) {

        }
        return null;
    }
}
