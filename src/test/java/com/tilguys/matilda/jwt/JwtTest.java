package com.tilguys.matilda.jwt;


import com.tilguys.matilda.config.jwt.JwtTokenFactory;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class JwtTest {
    final JwtTokenFactory jwtTokenFactory;

    @Test
    @WithMockUser
    void 유저는_로그인시에_jwt_토큰을_받는다() {
        jwtTokenFactory.createJwtCookieWithIdentifier(identifier);
    }

    @Test
    void jwt토큰이_만료되면_예외가_발생한다() {

    }

    @Test
    void jwt토큰으로_유저임을_증명한다() {

    }
}


