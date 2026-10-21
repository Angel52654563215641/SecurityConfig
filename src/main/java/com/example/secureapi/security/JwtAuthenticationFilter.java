package com.example.secureapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTH_ERROR_ATTR = "auth_error_message";
    private static final Logger AUDIT = LoggerFactory.getLogger("SECURITY_AUDIT");

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtService.parse(token);
                List<?> roles = claims.get("roles", List.class);
                List<SimpleGrantedAuthority> authorities = (roles == null ? List.<Object>of() : roles).stream()
                        .map(r -> new SimpleGrantedAuthority(String.valueOf(r))).toList();

                var auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (ExpiredJwtException e) {
                request.setAttribute(AUTH_ERROR_ATTR, "El token ha expirado");
                AUDIT.warn("TOKEN_EXPIRED ip={} uri={}", request.getRemoteAddr(), request.getRequestURI());
            } catch (JwtException | IllegalArgumentException e) {
                request.setAttribute(AUTH_ERROR_ATTR, "Token invalido");
                AUDIT.warn("TOKEN_INVALID ip={} uri={} reason={}", request.getRemoteAddr(),
                        request.getRequestURI(), e.getClass().getSimpleName());
            }
        }
        chain.doFilter(request, response);
    }
}
