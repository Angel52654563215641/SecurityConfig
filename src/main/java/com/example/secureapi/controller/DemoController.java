package com.example.secureapi.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DemoController {

    /** Publico: sirve para probar el handshake TLS sin token. */
    @GetMapping("/api/public/ping")
    public Map<String, String> ping() {
        return Map.of("status", "UP", "message", "Conexion segura establecida");
    }

    /** Requiere token valido (cualquier rol). */
    @GetMapping("/api/user/profile")
    public Map<String, Object> profile(Authentication auth) {
        return Map.of("username", auth.getName(),
                "authorities", auth.getAuthorities().stream().map(Object::toString).toList());
    }

    /** Requiere ROLE_ADMIN -> con un usuario normal devuelve 403. */
    @GetMapping("/api/admin/dashboard")
    public Map<String, String> admin(Authentication auth) {
        return Map.of("message", "Area de administracion", "admin", auth.getName());
    }
}
