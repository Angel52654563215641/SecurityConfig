package com.example.secureapi.security;

import com.example.secureapi.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Devuelve 403 con cuerpo JSON cuando el usuario esta autenticado pero no tiene el rol requerido. */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger AUDIT = LoggerFactory.getLogger("SECURITY_AUDIT");
    private final ObjectMapper mapper;

    public RestAccessDeniedHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AUDIT.warn("FORBIDDEN user={} ip={} method={} uri={}",
                auth != null ? auth.getName() : "anonymous", request.getRemoteAddr(),
                request.getMethod(), request.getRequestURI());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getOutputStream(),
                ErrorResponse.of(403, "Forbidden", "No tiene permisos para acceder a este recurso",
                        request.getRequestURI()));
    }
}
