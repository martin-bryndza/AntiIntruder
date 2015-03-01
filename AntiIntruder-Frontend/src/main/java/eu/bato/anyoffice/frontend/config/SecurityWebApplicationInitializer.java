package eu.bato.anyoffice.frontend.config;

import org.springframework.security.web.context.*;

/**
 *
 * @author Bato
 */
public class SecurityWebApplicationInitializer extends AbstractSecurityWebApplicationInitializer {

    public SecurityWebApplicationInitializer() {
        super(SecurityConfig.class);
    }
}