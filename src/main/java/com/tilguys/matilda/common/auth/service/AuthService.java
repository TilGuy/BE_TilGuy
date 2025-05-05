package com.tilguys.matilda.common.auth.service;

import com.tilguys.matilda.common.auth.GithubUserInfo;
import com.tilguys.matilda.common.auth.SimpleUserInfo;
import com.tilguys.matilda.common.auth.exception.DoesNotExistUserException;
import com.tilguys.matilda.common.auth.exception.OAuthFailException;
import com.tilguys.matilda.user.Role;
import com.tilguys.matilda.user.TilUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    public void signup(String identifier) {
        userService.signup(identifier);
    }

    public void loginProcessByGithubInfo(GithubUserInfo gitHubUserInfo) {
        updateUserProfile(gitHubUserInfo);
        signup(gitHubUserInfo.identifier());
        
        TilUser userByIdentifier = userService.findUserByIdentifier(gitHubUserInfo.identifier())
                .orElseThrow(() -> new OAuthFailException("존재하지 않는 유저입니다"));

        Authentication authentication = createAuthentication(userByIdentifier);
        SecurityContextHolder.getContext().setAuthentication(authentication);
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
        TilUser user = userService.findById(id).orElseThrow(DoesNotExistUserException::new);
        return createAuthentication(user);
    }

    public Authentication createAuthentication(TilUser tilUser) {
        List<SimpleGrantedAuthority> authorities = createAuthorities(List.of(tilUser.getRole()));
        SimpleUserInfo simpleUserInfo = new SimpleUserInfo(tilUser.getId(), tilUser.getNickname());
        return new UsernamePasswordAuthenticationToken(simpleUserInfo, "", authorities);
    }
}
