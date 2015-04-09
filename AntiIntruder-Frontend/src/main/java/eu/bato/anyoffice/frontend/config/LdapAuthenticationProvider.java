/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;

/**
 *
 * @author bryndza
 */
public class LdapAuthenticationProvider implements AuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(LdapAuthenticationProvider.class);

    private static final AuthenticationProvider ap = new ActiveDirectoryLdapAuthenticationProvider("2008r2ad.test", "ldap://10.0.10.170", "DC=2008r2ad,DC=test");
    private static final StandardPasswordEncoder encoder = new StandardPasswordEncoder();
    
    @Autowired
    private PersonService personService;

    @Autowired
    private Environment environment;

    @Override
    public Authentication authenticate(Authentication a) throws AuthenticationException {
        String username = a.getName();
        log.info("Authenticating: " + username);
        try {
            ap.authenticate(a);
        } catch (AuthenticationException e) {
            if (username.equals("adminAnyOffice") && environment.getProperty("auth.admin.password", "1234").equals(a.getCredentials().toString())) {
                log.warn("Administrator authenticated from: {}", a.getDetails());
            } else {
                throw e;
            }
        }
        if (personService.isPresent(username)) {
            log.info("User {} logged in through AD.", username);
        } else {
            log.info("User with username {} logged in through AD for the first time", username);
            PersonDto dto = new PersonDto();
            dto.setUsername(username);
            dto.setRole(PersonRole.USER);
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
