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

import java.util.LinkedList;
import java.util.List;
import javax.servlet.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.header.writers.frameoptions.RegExpAllowFromStrategy;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

@Configuration
@ComponentScan(value = {"eu.bato.anyoffice"})
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private Environment environment;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        if (environment.getProperty("auth.type", "DB").equals("DB")) {
            log.debug("DB authentication");
            auth
                    .userDetailsService(personDetailsService())
                    .passwordEncoder(new StandardPasswordEncoder());
        } else {
            log.debug("LDAP authentication");
            ldapAuthenticationProvider().initialize();
            auth
                    .authenticationProvider(ldapAuthenticationProvider());
        }
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .headers().frameOptions().and().addHeaderWriter(new XFrameOptionsHeaderWriter(new RegExpAllowFromStrategy("/.*/"))).and()
                .addFilterBefore(authenticationFilter(), LogoutFilter.class)
                .csrf().disable()
                .authorizeRequests()
                //.antMatchers("/**").permitAll()
                .antMatchers("/images/**").permitAll()
                .antMatchers("/css/**").permitAll()
                .antMatchers("/js/**").permitAll()
                .antMatchers("/api/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                .antMatchers(HttpMethod.GET, "/login").permitAll()
                .antMatchers(HttpMethod.POST, "/login").permitAll()
                .antMatchers("/logout").authenticated()
                .antMatchers(HttpMethod.GET, "/").permitAll()
                .antMatchers(HttpMethod.GET, "/colleagues").permitAll()
                .antMatchers(HttpMethod.GET, "/faq").permitAll()
                .antMatchers(HttpMethod.GET, "/otherApp").permitAll()
                .antMatchers("/downloadClient").permitAll()
                .antMatchers(HttpMethod.GET, "/changeState").hasAuthority("ROLE_ADMIN")
                .antMatchers(HttpMethod.POST, "/").hasAuthority("ROLE_ADMIN")
                .antMatchers(HttpMethod.DELETE, "/**").hasAuthority("ROLE_ADMIN")
                .antMatchers(HttpMethod.POST, "/interact").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                .antMatchers(HttpMethod.POST, "/cancelinteract").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                .antMatchers("/personEdit/").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                .antMatchers(HttpMethod.POST, "/personEdit/save").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                .antMatchers(HttpMethod.GET, "/graph").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                .anyRequest().authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(4)
                .expiredUrl("/")
                .and()
                .and()
                .formLogin()
                .loginProcessingUrl("/login")
                .loginPage("/")
                .permitAll()
                .and()
                .logout()
                .permitAll();
    }

    @Bean
    PersonDetailsService personDetailsService() {
        return new PersonDetailsService();
    }

    @Bean
    LdapAuthenticationProvider ldapAuthenticationProvider() {
        LdapAuthenticationProvider provider = new LdapAuthenticationProvider();
        return provider;
    }

    @Bean
    Filter authenticationFilter() {
        BasicAuthenticationFilter basicAuthFilter = new BasicAuthenticationFilter(customAuthenticationManager(), new BasicAuthenticationEntryPoint());
        return basicAuthFilter;
    }

    @Bean
    ProviderManager customAuthenticationManager() {
        List<AuthenticationProvider> providers = new LinkedList<>();
        providers.add(daoAuthPovider());
        providers.add(ldapAuthenticationProvider());
        ProviderManager authenticationManager = new ProviderManager(providers);
        authenticationManager.setEraseCredentialsAfterAuthentication(true);
        return authenticationManager;
    }

    @Bean
    DaoAuthenticationProvider daoAuthPovider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(personDetailsService());
        provider.setPasswordEncoder(new StandardPasswordEncoder());
        return provider;
    }

}
