/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.frontend.config;

import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.repository.config.EnableLdapRepositories;
import org.springframework.security.ldap.server.ApacheDSContainer;

/**
 *
 * @author bryndza
 */
@Configuration
@EnableLdapRepositories
public class LdapConfig {
    
    private static final Logger log = LoggerFactory.getLogger(LdapConfig.class);

    public static final String PROPERTY_LDAP_URL = "ldap.url";
    public static final String DEFAULT_PROPERTY_LDAP_URL = "ldap://localhost:33899";

    public static final String PROPERTY_LDAP_LDIF_FILE = "ldap.ldif";
    public static final String DEFAULT_LDAP_LDIF_FILE = "classpath:test-server.ldif";

    public static final String PROPERTY_LDAP_USER_DN = "ldap.userDn";
    public static final String DEFAULT_PROPERTY_LDAP_USER_DN = "LOGIN";

    public static final String PROPERTY_LDAP_PASSWORD = "ldap.password";
    public static final String DEFAULT_PROPERTY_LDAP_PASSWORD = "pass";

    public static final String PROPERTY_LDAP_BASE = "ldap.base";
    public static final String DEFAULT_LDAP_BASE = "dc=corp,dc=mykeys,dc=com";

    public static final String PROPERTY_LDAP_USER_BASE = "ldap.userbase";
    public static final String DEFAULT_LDAP_USER_BASE = "OU=AnyOffice";
    
    public static final String PROPERTY_LDAP_USER_ID = "ldap.userid";
    public static final String DEFAULT_LDAP_USER_ID = "UID";

    @Resource
    Environment environment;

    @Bean
    BaseLdapPathContextSource contextSource() throws Exception {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(environment.getProperty(PROPERTY_LDAP_URL, DEFAULT_PROPERTY_LDAP_URL));
        contextSource.setBase(environment.getProperty(PROPERTY_LDAP_BASE, DEFAULT_LDAP_BASE));
        contextSource.setUserDn(environment.getProperty(PROPERTY_LDAP_USER_DN, DEFAULT_PROPERTY_LDAP_USER_DN));
        contextSource.setPassword(environment.getProperty(PROPERTY_LDAP_PASSWORD, DEFAULT_PROPERTY_LDAP_PASSWORD));
        contextSource.setAnonymousReadOnly(true);

        if (environment.getProperty(PROPERTY_LDAP_URL, DEFAULT_PROPERTY_LDAP_URL).contains("ldap://localhost")) {
            ldapServer();
        }
        
        contextSource.afterPropertiesSet();
        
        return contextSource;
    }

    @Bean
    LdapTemplate ldapTemplate() throws Exception {
        log.debug("LdapTemplate initialized");
        LdapTemplate template = new LdapTemplate(contextSource());
//        template.afterPropertiesSet();
//        Attributes attrs = new BasicAttributes();
//        attrs.put("uid", "testUID");
//        attrs.put("cn", "testCN");
//        template.bind("ou=anyoffice", null, attrs);
        return template;
    }

    @Bean
    @Lazy
    public ApacheDSContainer ldapServer() throws Exception {
        ApacheDSContainer apacheDSContainer = new ApacheDSContainer(environment.getProperty(PROPERTY_LDAP_BASE, DEFAULT_LDAP_BASE), environment.getProperty(PROPERTY_LDAP_LDIF_FILE, DEFAULT_LDAP_LDIF_FILE));
        apacheDSContainer.setPort(Integer.valueOf(DEFAULT_PROPERTY_LDAP_URL.substring(DEFAULT_PROPERTY_LDAP_URL.lastIndexOf(":") + 1)));

        return apacheDSContainer;
    }

    @Bean
    public String userSearchBase() {
        return environment.getProperty(PROPERTY_LDAP_USER_BASE, DEFAULT_LDAP_USER_BASE);
    }

}
