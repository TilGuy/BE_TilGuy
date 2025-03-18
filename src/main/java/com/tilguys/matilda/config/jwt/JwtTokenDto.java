package com.tilguys.matilda.config.jwt;

import lombok.Builder;

@Builder
public record JwtTokenDto(String grantType, String accessToken, Long tokenExpiresIn, String refreshToken,
                          Long refreshTokenExpiresIn) {
}

