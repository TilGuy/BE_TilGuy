package com.tilguys.matilda.auth;

import com.tilguys.matilda.user.entity.User;
import java.util.Collection;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        User user = User.builder()
                .role(customUser.role())
                .identifier(customUser.identifier())
                .nickname(null)
                .build();
        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(user.getRole().getKey()));
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
        context.setAuthentication(authentication);
        return context;
    }
}
