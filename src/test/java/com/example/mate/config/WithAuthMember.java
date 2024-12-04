package com.example.mate.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithAuthMemberSecurityContextFactory.class)
public @interface WithAuthMember {
    String userId() default "testUser";

    long memberId() default 1L;

    String[] roles() default {"USER"};
}
