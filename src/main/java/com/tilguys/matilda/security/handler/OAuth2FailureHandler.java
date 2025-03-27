package com.tilguys.matilda.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        // 리다이렉트할 주소 생성, query str//        String redirectUrl = UriComponentsBuilder.fromUriString(REDIRECT_URL)
        ////                .queryParam("error", exception.getLocalizedMessage())
        ////                .build()
        ////                .toUriString();ing에 에러 메세지 추가

        // TODO 실패시 어디로?
        String redirectUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/").build().toString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
