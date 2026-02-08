package com.superdupermart.shopping.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Autowired
    public JwtFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("JwtFilter: Processing " + request.getMethod() + " " + request.getRequestURI());
        Optional<AuthUserDetail> authUserDetailOptional = jwtProvider.resolveToken(request);

        if (authUserDetailOptional.isPresent()) {
            AuthUserDetail authUserDetail = authUserDetailOptional.get();
            System.out.println("JwtFilter: Token resolved for User: " + authUserDetail.getUsername());
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    authUserDetail,
                    null,
                    authUserDetail.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("JwtFilter: SecurityContext set.");
        } else {
            System.out.println("JwtFilter: No token resolved.");
        }

        filterChain.doFilter(request, response);
    }
}
