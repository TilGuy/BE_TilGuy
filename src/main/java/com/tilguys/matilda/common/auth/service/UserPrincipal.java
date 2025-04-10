package com.tilguys.matilda.common.auth.service;

import com.tilguys.matilda.user.TilUser;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
public class UserPrincipal implements UserDetails, OAuth2User {

    private TilUser tilUser;
    private String nameAttributeKey;
    private Map<String, Object> attributes;
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(TilUser tilUser) {
        this.tilUser = tilUser;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(tilUser.getRole().getKey()));
    }

    public UserPrincipal(TilUser tilUser, Map<String, Object> attributes, String nameAttributeKey) {
        this.tilUser = tilUser;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(tilUser.getRole().getKey()));
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
    }

    /**
     * OAuth2User method implements
     */
    @Override
    public String getName() {
        return tilUser.getIdentifier();
    }

    /**
     * UserDetails method implements
     */
    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return tilUser.getIdentifier();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
