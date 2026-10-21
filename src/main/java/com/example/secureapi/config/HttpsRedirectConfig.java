package com.example.secureapi.config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Abre un conector HTTP y redirige automaticamente (302) todo el trafico al puerto HTTPS.
 * Si se desea rechazar HTTP en lugar de redirigir, poner app.http-redirect.enabled=false
 * (entonces solo existira el puerto HTTPS y las peticiones HTTP son rechazadas).
 */
@Configuration
public class HttpsRedirectConfig {

    @Value("${app.http-redirect.enabled:true}")
    private boolean redirectEnabled;

    @Value("${app.http-redirect.http-port:8080}")
    private int httpPort;

    @Value("${server.port:8443}")
    private int httpsPort;

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                if (!redirectEnabled) {
                    return;
                }
                SecurityConstraint constraint = new SecurityConstraint();
                constraint.setUserConstraint("CONFIDENTIAL"); // fuerza canal cifrado
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                constraint.addCollection(collection);
                context.addConstraint(constraint);
            }
        };
        if (redirectEnabled) {
            tomcat.addAdditionalTomcatConnectors(httpConnector());
        }
        return tomcat;
    }

    private Connector httpConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(httpPort);
        connector.setSecure(false);
        connector.setRedirectPort(httpsPort);
        return connector;
    }
}
