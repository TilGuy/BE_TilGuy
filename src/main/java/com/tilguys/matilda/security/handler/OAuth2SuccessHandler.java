package com.tilguys.matilda.security.handler;

import com.tilguys.matilda.config.jwt.JwtTokenFactory;
import com.tilguys.matilda.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String AVATAR_URL = "avatar_url";
    private static final String CALLBACK_REDIRECT_FORMAT = "%s/oauth/callback?username=%s&profileUrl=%s";

    private final UserService userService;
    private final JwtTokenFactory jwtTokenFactory;

    @Value("${oauth2.redirect.url}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication)
            throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String identifier = oAuth2User.getName();
        String avatarUrl = oAuth2User.getAttribute(AVATAR_URL);

        userService.validateExistUser(identifier);
        jwtTokenFactory.createResponseJwtToken(response, authentication);
        String sendRedirectUrl = String.format(CALLBACK_REDIRECT_FORMAT, frontendRedirectUrl, identifier, avatarUrl);
        response.sendRedirect(sendRedirectUrl);
    }
}
