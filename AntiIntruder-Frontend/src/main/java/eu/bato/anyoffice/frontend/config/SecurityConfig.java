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
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.ldap.authentication.UserDetailsServiceLdapAuthoritiesPopulator;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@ComponentScan(value = {"eu.bato.anyoffice"})
@EnableWebMvcSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private Environment environment;
    
    @Autowired
    BaseLdapPathContextSource contextSource;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        if (environment.getProperty("auth.type", "DB").equals("DB")) {
            log.debug("DB authentication");
            auth
                    .userDetailsService(personDetailsService())
                    .passwordEncoder(new StandardPasswordEncoder());
        } else {
            log.debug("LDAP authentication");
            auth
                    .ldapAuthentication()
                    .userSearchFilter("uid={0}, ou=anyoffice")
                    .ldapAuthoritiesPopulator(new UserDetailsServiceLdapAuthoritiesPopulator(personDetailsService()))
                    .userSearchBase(environment.getProperty(LdapConfig.PROPERTY_LDAP_BASE,LdapConfig.DEFAULT_LDAP_BASE))
                    .contextSource(contextSource);
//                    .userDnPatterns("uid={0},ou=people")
//                    .groupSearchBase("ou=groups")
//                    .contextSource().ldif("classpath:test-server.ldif");
        }
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(authenticationFilter(), LogoutFilter.class)
                .csrf().disable()
                .authorizeRequests()
                //.antMatchers("/**").permitAll()
                .antMatchers("/resources/**").permitAll()
                .antMatchers("/api/**").hasAnyAuthority("USER", "ADMIN")
                .antMatchers(HttpMethod.GET, "/login").permitAll()
                .antMatchers(HttpMethod.POST, "/login").permitAll()
                .antMatchers("/logout").authenticated()
                .antMatchers(HttpMethod.GET, "/**").permitAll()
                .antMatchers(HttpMethod.POST, "/**").hasAuthority("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/**").hasAuthority("ADMIN")
                .antMatchers(HttpMethod.POST, "/**").hasAnyAuthority("ADMIN", "USER")
                .anyRequest().authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .expiredUrl("/")
                .and()
                .and()
                .formLogin()
                .loginPage("/login")
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
    Filter authenticationFilter() {
        BasicAuthenticationFilter basicAuthFilter = new BasicAuthenticationFilter(customAuthenticationManager(), new BasicAuthenticationEntryPoint());
        return basicAuthFilter;
    }

    @Bean
    ProviderManager customAuthenticationManager() {
        List<AuthenticationProvider> providers = new LinkedList<>();
        providers.add(daoAuthPovider());
        ProviderManager authenticationManager = new ProviderManager(providers);
        authenticationManager.setEraseCredentialsAfterAuthentication(true);
        return authenticationManager;
    }

    @Bean
    DaoAuthenticationProvider daoAuthPovider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(personDetailsService());
        provider.setPasswordEncoder(new StandardPasswordEncoder());
        //TODO: add salt
        return provider;
    }

//    @Configuration
//    protected static class AuthenticationConfiguration extends
//            GlobalAuthenticationConfigurerAdapter {
//
//        @Override
//        public void init(AuthenticationManagerBuilder auth) throws Exception {
//            auth
//                    .ldapAuthentication()
//                    .userDnPatterns("uid={0},ou=people")
//                    .groupSearchBase("ou=groups")
//                    .contextSource().ldif("classpath:test-server.ldif");
//        }
//    }
}
