package com.ysoft.tools.antiintruder.web.config;

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