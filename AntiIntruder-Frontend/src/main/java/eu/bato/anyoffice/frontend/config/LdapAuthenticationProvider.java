/*
 * Copyright (c) 2015, Martin Bryndza
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package eu.bato.anyoffice.frontend.config;

import eu.bato.anyoffice.serviceapi.dto.LoginDetailsDto;
import eu.bato.anyoffice.serviceapi.dto.PersonDto;
import eu.bato.anyoffice.serviceapi.dto.PersonRole;
import eu.bato.anyoffice.serviceapi.service.PersonService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;

/**
 *
 * @author bryndza
 */
public class LdapAuthenticationProvider implements AuthenticationProvider {

    private static final String LDAP_DOMAIN = "ldap.domain";
    private static final String LDAP_URL = "ldap.url";
    private static final String LDAP_ROOT_DN = "ldap.rootDn";

    private static final Logger log = LoggerFactory.getLogger(LdapAuthenticationProvider.class);

    private static AuthenticationProvider ap;
    private static final StandardPasswordEncoder encoder = new StandardPasswordEncoder();

    @Autowired
    private PersonService personService;

    @Autowired
    private Environment environment;

    public LdapAuthenticationProvider() {
    }

    public void initialize() {
        String domain = environment.getProperty(LDAP_DOMAIN, "2008r2ad.test");
        String url = environment.getProperty(LDAP_URL, "ldap://10.0.10.170");
        String rootDn = environment.getProperty(LDAP_ROOT_DN, "DC=2008r2ad,DC=test");
        log.debug("Initializing LDAP Auth Provder:" + domain + "; " + url + "; " + rootDn);
        ActiveDirectoryLdapAuthenticationProvider adp = new ActiveDirectoryLdapAuthenticationProvider(domain, url, rootDn);
        adp.setConvertSubErrorCodesToExceptions(true);
        ap = adp;
    }

    @Override
    public Authentication authenticate(Authentication a) throws AuthenticationException {
        String username = a.getName();
        log.info("Authenticating (LDAP): " + username + ":" + a.getCredentials().toString());
        try {
            ap.authenticate(a);
        } catch (AuthenticationException e) {
            if (username.equals("adminAnyOffice") && environment.getProperty("auth.admin.password", "1234").equals(a.getCredentials().toString())) {
                log.warn("Administrator authenticated from: {}", a.getDetails());
            } else {
                log.error(e.getLocalizedMessage());
                throw e;
            }
        }
        if (personService.isPresent(username)) {
            log.info("User {} logged in through AD.", username);
        } else {
            log.info("User with username {} logged in through AD for the first time", username);
            PersonDto dto = new PersonDto();
            dto.setUsername(username);
            dto.setRole(PersonRole.ROLE_USER);
            dto.setDisplayName(username);
            personService.register(dto, encoder.encode(a.getCredentials().toString()));
        }
        Optional<LoginDetailsDto> optDetails = personService.getLoginDetails(username);
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(optDetails.get().getRole().name()));
        return new UsernamePasswordAuthenticationToken(a.getName(), a.getCredentials().toString(), authorities);
    }

    @Override
    public boolean supports(Class<?> type) {
        return ap.supports(type);
    }

}
