package com.kitly.mail.config;

import com.kitly.mail.filter.ApiKeyAuthFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;

@TestConfiguration
@Profile("test")
public class TestSecurityConfig {

    @Bean
    @Primary
    public ApiKeyAuthFilter testApiKeyAuthFilter() {
        return new ApiKeyAuthFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                // Authentifiziere automatisch für Tests
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken("test-user", null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
            }
        };
    }
}

