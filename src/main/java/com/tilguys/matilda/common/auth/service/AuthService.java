package com.tilguys.matilda.common.auth.service;

import com.tilguys.matilda.common.auth.exception.NotExistUserException;
import com.tilguys.matilda.user.TilUser;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    public Authentication login(String identifier) {
        Optional<TilUser> userByIdentifier = userService.findUserByIdentifier(
                identifier);
        if (userByIdentifier.isEmpty()) {
            userService.signup(identifier);
            userByIdentifier = userService.findUserByIdentifier(identifier);
        }

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(userByIdentifier.get().getRole().toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .toList();

        return new UsernamePasswordAuthenticationToken(userByIdentifier.get().getId(), "", authorities);
    }

    public Authentication createAuthenticationFromId(Long id) {
        Optional<TilUser> user = userService.findById(id);
        user.orElseThrow(NotExistUserException::new);
        String identifier = user.get().getIdentifier();
        return login(identifier);
    }
}
