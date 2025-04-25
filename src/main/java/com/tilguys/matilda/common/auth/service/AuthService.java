package com.tilguys.matilda.common.auth.service;

import com.tilguys.matilda.common.auth.GithubUserInfo;
import com.tilguys.matilda.common.auth.exception.DoesNotExistUserException;
import com.tilguys.matilda.common.auth.exception.NotExistUserException;
import com.tilguys.matilda.user.Role;
import com.tilguys.matilda.user.TilUser;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    public void setAuthenticationFromUser(String identifier) {
        TilUser user = userService.findUserByIdentifier(identifier).orElseThrow(DoesNotExistUserException::new);

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(user.getRole().toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .toList();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user.getId(), "", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public void signup(String identifier) {
        userService.signup(identifier);
    }

    public void loginProcessByGithubInfo(GithubUserInfo gitHubUserInfo) {
        updateUserProfile(gitHubUserInfo);
        signup(gitHubUserInfo.identifier());
        setAuthenticationFromUser(gitHubUserInfo.identifier());
    }

    private void updateUserProfile(GithubUserInfo gitHubUserInfo) {
        userService.updateUserInfo(gitHubUserInfo);
    }

    public GithubUserInfo getGithubInfoById(Long id) {
        TilUser tilUser = userService.findById(id).orElseThrow(DoesNotExistUserException::new);
        String avatarUrl = tilUser.getAvatarUrl();
        String identifier = tilUser.getIdentifier();
        String nickname = tilUser.getNickname();
        return new GithubUserInfo(identifier, avatarUrl, nickname);
    }

    public List<SimpleGrantedAuthority> createAuthorities(List<Role> roles) {
        return roles.stream()
                .map(Enum::toString)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    public Authentication createAuthenticationFromId(Long id) {
        Optional<TilUser> user = userService.findById(id);
        user.orElseThrow(NotExistUserException::new);
        String identifier = user.get().getIdentifier();
        return login(identifier);
    }

    public Authentication login(String identifier) {
        Optional<TilUser> userByIdentifier = userService.findUserByIdentifier(
                identifier);
        if (userByIdentifier.isEmpty()) {
            userService.signup(identifier);
            userByIdentifier = userService.findUserByIdentifier(identifier);
        }

        Collection<? extends GrantedAuthority> authorities =
                createAuthorities(List.of(userByIdentifier.get().getRole()));

        return new UsernamePasswordAuthenticationToken(userByIdentifier.get().getId(), "", authorities);
    }
}
