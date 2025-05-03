package com.tilguys.matilda.auth.user;

import com.tilguys.matilda.user.Role;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {
    long identifier() default 0L;

    Role role() default Role.USER;

    String nickname() default "test";
}
