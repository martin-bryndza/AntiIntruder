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
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

/**
 *
 * @author Bato
 */
public class PersonDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(PersonDetailsService.class);

    private static final StandardPasswordEncoder encoder = new StandardPasswordEncoder();

    @Autowired
    private PersonService personService;

    @Autowired
    private Environment environment;

    @PostConstruct
    protected void initialize() {

        if (!personService.isPresent("adminAnyOffice")) {
            PersonDto sampleUser = new PersonDto();
            sampleUser.setUsername("adminAnyOffice");
            sampleUser.setDisplayName("Administrator");
            sampleUser.setRole(PersonRole.ROLE_ADMIN);
            personService.register(sampleUser, encoder.encode(environment.getProperty("auth.admin.password", "1234")));
        }

        if (environment.getProperty("auth.type", "DB").equals("LDAP")) {
            return;
        }

        if (!personService.isPresent("bato")) {
            PersonDto sampleUser = new PersonDto();
            sampleUser.setUsername("bato");
            sampleUser.setDisplayName("Martin Bryndza");
            sampleUser.setDescription("QA Engineer ETNA");
            sampleUser.setLocation("R&D Open Space");
            sampleUser.setRole(PersonRole.ROLE_USER);
            personService.register(sampleUser, encoder.encode("bato"));
        }

        if (!personService.isPresent("olda")) {
            PersonDto sampleUser = new PersonDto();
            sampleUser.setUsername("olda");
            sampleUser.setDisplayName("Michal Ordelt");
            sampleUser.setDescription("Developer in ETNA (KM)");
            sampleUser.setLocation("R&D Open Space");
            sampleUser.setRole(PersonRole.ROLE_USER);
            personService.register(sampleUser, encoder.encode("olda"));
        }

        if (!personService.isPresent("myska")) {
            PersonDto sampleUser = new PersonDto();
            sampleUser.setUsername("myska");
            sampleUser.setDisplayName("Ondrej Myska");
            sampleUser.setDescription("Developer in ETNA (FX)");
            sampleUser.setLocation("R&D Open Space");
            sampleUser.setRole(PersonRole.ROLE_USER);
            personService.register(sampleUser, encoder.encode("myska"));
        }

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Authenticating: " + username);
        Optional<LoginDetailsDto> optDetails = personService.getLoginDetails(username);
        if (!optDetails.isPresent()) {
            throw new UsernameNotFoundException("User with username " + username + " was not found.");
        }
        optDetails = personService.getLoginDetails(username);
        if (!optDetails.isPresent()) {
            throw new UsernameNotFoundException("User with username " + username + " was not found.");
        }
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(optDetails.get().getRole().name()));
        return new User(username, optDetails.get().getPassword(), authorities);

    }

}
