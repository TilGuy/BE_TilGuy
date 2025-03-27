package com.tilguys.matilda.security.handler;

import com.tilguys.matilda.exception.MatildaException;
import com.tilguys.matilda.user.entity.User;
import com.tilguys.matilda.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Value("${oauth2.redirect-url}")
    private String redirectURL;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException, IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String identifier = oAuth2User.getName();

        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new MatildaException("유저를 찾을 수 없습니다."));

        String redirectUri = UriComponentsBuilder.fromUriString(redirectURL).build().toString();

        getRedirectStrategy().sendRedirect(request, response, redirectUri); // TODO 수정 필요
    }
}
