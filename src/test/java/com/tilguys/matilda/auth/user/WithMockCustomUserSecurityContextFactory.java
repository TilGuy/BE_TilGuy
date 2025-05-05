package com.tilguys.matilda.auth.user;

import com.tilguys.matilda.common.auth.SimpleUserInfo;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        SimpleUserInfo simpleUserInfo = new SimpleUserInfo(customUser.identifier(), customUser.nickname());

        Authentication authentication = new UsernamePasswordAuthenticationToken(simpleUserInfo, null,
                List.of(new SimpleGrantedAuthority(customUser.role().toString())));
        context.setAuthentication(authentication);
        return context;
    }
}
