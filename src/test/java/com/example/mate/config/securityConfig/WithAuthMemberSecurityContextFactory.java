package com.example.mate.config.securityConfig;

import com.example.mate.common.security.auth.AuthMember;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithAuthMemberSecurityContextFactory implements WithSecurityContextFactory<WithAuthMember> {
    @Override
    public SecurityContext createSecurityContext(WithAuthMember annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        AuthMember authMember = new AuthMember(annotation.userId(), annotation.memberId());
        List<SimpleGrantedAuthority> authorities = Arrays.stream(annotation.roles())
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        Authentication auth = new UsernamePasswordAuthenticationToken(authMember, null, authorities);
        context.setAuthentication(auth);
        return context;
    }
}
