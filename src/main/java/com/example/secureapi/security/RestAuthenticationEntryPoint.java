package com.example.secureapi.security;

import com.example.secureapi.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Devuelve 401 con cuerpo JSON cuando falta el token o es invalido. */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger AUDIT = LoggerFactory.getLogger("SECURITY_AUDIT");
    private final ObjectMapper mapper;

    public RestAuthenticationEntryPoint(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        String message = (String) request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR_ATTR);
        if (message == null) {
            message = "Se requiere autenticacion: envie un token en el header 'Authorization: Bearer <token>'";
        }
        AUDIT.warn("UNAUTHORIZED ip={} method={} uri={}", request.getRemoteAddr(),
                request.getMethod(), request.getRequestURI());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("WWW-Authenticate", "Bearer");
        mapper.writeValue(response.getOutputStream(),
                ErrorResponse.of(401, "Unauthorized", message, request.getRequestURI()));
    }
}
