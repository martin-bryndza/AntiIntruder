package eu.bato.anyoffice.frontend.config;

import eu.bato.anyoffice.serviceapi.dto.PersonDto;
import eu.bato.anyoffice.serviceapi.dto.PersonRole;
import eu.bato.anyoffice.serviceapi.service.PersonService;
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
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
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
    private PersonService personService;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        if (environment.getProperty("auth.type", "DB").equals("DB")) {
            log.debug("DB authentication");
            auth
                    .userDetailsService(personDetailsService())
                    .passwordEncoder(new StandardPasswordEncoder());
        } else {
            log.debug("LDAP authentication");
//            DefaultSpringSecurityContextSource context = new DefaultSpringSecurityContextSource("ldap://10.0.10.170:389/DC=2008r2ad,DC=test");
//            context.setAnonymousReadOnly(true);
//            context.afterPropertiesSet();
            
//            AuthenticationProvider ap = new ActiveDirectoryLdapAuthenticationProvider("2008r2ad.test", "ldap://10.0.10.170", "DC=2008r2ad,DC=test");
                                                
            auth
                    .authenticationProvider(new MyActiveDirectoryLdapAuthenticationProvider());
                    //.ldapAuthentication()
                    //.contextSource(context)
                    //.userDnPatterns("CN={0},OU=anyoffice")
                    //.userSearchFilter("(uid={0})").userSearchBase("OU=anyoffice")
//                    .ldapAuthoritiesPopulator(new UserDetailsServiceLdapAuthoritiesPopulator(personDetailsService()));
//                    .rolePrefix("");
                    
                    
//                    .userSearchBase("")
//                    .userSearchFilter("sAMAccountName={0}")
//                    .ldapAuthoritiesPopulator(new UserDetailsServiceLdapAuthoritiesPopulator(personDetailsService()))
//                    .contextSource().url("ldap://10.0.10.170:389").port(389).and()
//                    .passwordEncoder(new StandardPasswordEncoder());
            
//                    .userSearchBase(environment.getProperty(LdapConfig.PROPERTY_LDAP_BASE,LdapConfig.DEFAULT_LDAP_BASE))
//                    .contextSource(contextSource);
//                    .userDnPatterns("uid={0},ou=people")
//                    .groupSearchBase("ou=groups")
//                    .contextSource().ldif("classpath:test-server.ldif");
        }
    }
    
    private class MyActiveDirectoryLdapAuthenticationProvider implements AuthenticationProvider{

        AuthenticationProvider ap = new ActiveDirectoryLdapAuthenticationProvider("2008r2ad.test", "ldap://10.0.10.170", "DC=2008r2ad,DC=test");
        
        @Override
        public Authentication authenticate(Authentication a) throws AuthenticationException {
            Authentication au = ap.authenticate(a);
            String username = ((UserDetails)au.getPrincipal()).getUsername();
            if (personService.isPresent(username)){
                log.info("User {} logged in through AD.", username);
                return au;
            }
            log.info("User with username {} logged in through AD for the first time", username);
            PersonDto dto = new PersonDto();
            dto.setUsername(username);
            dto.setRole(PersonRole.USER);
            dto.setDisplayName(username);
            personService.register(dto, new StandardPasswordEncoder().encode("%DU)FöfI8/°"+username+"LDAP%DU)FöfI8/°"));
            return au;
        }

        @Override
        public boolean supports(Class<?> type) {
            return ap.supports(type);
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
