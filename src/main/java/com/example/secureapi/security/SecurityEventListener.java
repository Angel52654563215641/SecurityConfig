package com.example.secureapi.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/** Registro de eventos de seguridad (login exitoso / fallido). Logger: SECURITY_AUDIT */
@Component
public class SecurityEventListener {

    private static final Logger AUDIT = LoggerFactory.getLogger("SECURITY_AUDIT");

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        AUDIT.info("LOGIN_SUCCESS user={}", event.getAuthentication().getName());
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        AUDIT.warn("LOGIN_FAILURE user={} reason={}", event.getAuthentication().getName(),
                event.getException().getClass().getSimpleName());
    }
}
