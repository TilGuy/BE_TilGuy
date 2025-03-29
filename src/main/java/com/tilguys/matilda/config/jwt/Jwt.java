package com.tilguys.matilda.config.jwt;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class Jwt {

    private static final String INVALID_AUTH_TOKEN = "유효하지 않은 인증 토큰입니다.";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String REFRESH_COOKIE_HEADER = "refreshToken";

    private final JwtTokenFactory jwtTokenFactory;

    public static Long getCurrentUserId() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Security Context에 인증 정보가 없습니다.");
        }
        return Long.parseLong(authentication.getName());
    }

    public String getUsernameFromRequest(HttpServletRequest request) {
        String token = resolveToken(request);
//        String token = getTokenFromCookie(request);
        return jwtTokenFactory.getUsernameFromToken(token);
    }

    private String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return findValidToken(cookies);
        }
        return "";
    }

    private static String findValidToken(Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            if ("token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return "";
    }

    public String getPrincipleFromToken(HttpServletRequest request) {
        String token = resolveToken(request);
        return jwtTokenFactory.getPrincipleFromToken(token);
    }

    public String getPrincipleFromToken(String token) {
        return jwtTokenFactory.getPrincipleFromToken(token);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return bearerToken;
    }

    public String resolveRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REFRESH_COOKIE_HEADER.equals(cookie.getName())) {
                    String token = cookie.getValue();
                    // 쿠키값을 확인하고 필요한 작업 수행
                    return token;
                }
            }
        }
        return null;
    }
//
//    public String getNewAccessCode(HttpServletRequest request)
//    {
//        String refreshToken = resolveRefreshToken(request);
//        if (StringUtils.hasText(refreshToken) && jwtTokenFactory.validateToken(refreshToken)) {
//            Authentication authentication = jwtTokenFactory.getAuthentication(refreshToken);
//            return jwtTokenFactory.generateAccessToken(authentication);
//        }
//        throw new MatildaException(INVALID_AUTH_TOKEN);
//    }
//
//    public void deleteRefreshCookie(HttpServletResponse response)
//    {
//        Cookie deletedRefreshTokenCookie = new Cookie("refreshToken", null);
//        deletedRefreshTokenCookie.setMaxAge(0);
//        deletedRefreshTokenCookie.setPath("/"); // 쿠키의 경로를 설정합니다. 필요에 따라 경로를 조정하세요.
//        response.addCookie(deletedRefreshTokenCookie);
//    }
//
//    public static String generateRandomPassword() {
//        int minLength = 8;
//        int maxLength = 20;
//
//        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+=<>?";
//
//        SecureRandom random = new SecureRandom();
//        StringBuilder password = new StringBuilder();
//
//        int passwordLength = minLength + random.nextInt(maxLength - minLength + 1);
//
//        for (int i = 0; i < passwordLength; i++) {
//            int randomIndex = random.nextInt(characters.length());
//            char randomChar = characters.charAt(randomIndex);
//
//            password.append(randomChar);
//        }
//
//        return password.toString();
//    }
//
//    public String getUsernameFromToken(String token) {
//        return jwtTokenFactory.getUsernameFromToken(token);
//    }
}

