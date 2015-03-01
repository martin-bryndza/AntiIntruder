package eu.bato.anyoffice.frontend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.bato.anyoffice.frontend.rest.Versions;
import eu.bato.anyoffice.frontend.security.HeaderAuthenticationFilter;
import eu.bato.anyoffice.frontend.security.HeaderUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebMvcSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String ACCESS_DENIED_JSON = "{\"message\":\"You are not privileged to request this resource.\", \"access-denied\":true,\"cause\":\"AUTHORIZATION_FAILURE\"}";
    private static final String UNAUTHORIZED_JSON = "{\"message\":\"Full authentication is required to access this resource.\", \"access-denied\":true,\"cause\":\"NOT AUTHENTICATED\"}";

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(personDetailsService()).passwordEncoder(new StandardPasswordEncoder());
    }

    @Autowired
    private HeaderUtil headerUtil;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CustomAuthenticationSuccessHandler successHandler = new CustomAuthenticationSuccessHandler();
        successHandler.headerUtil(headerUtil);

        http.
                //addFilterBefore(authenticationFilter(), LogoutFilter.class).
                csrf().disable().
                formLogin().successHandler(successHandler).
                loginProcessingUrl("/login").
                and().
                logout().
                logoutSuccessUrl("/login?logout").
                and().
                sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).
                and().
                exceptionHandling().
                accessDeniedHandler(new CustomAccessDeniedHandler()).
                authenticationEntryPoint(new CustomAuthenticationEntryPoint()).
                and().
                authorizeRequests().
                antMatchers("/resources/**").permitAll().
                antMatchers(HttpMethod.GET, "/login").permitAll().
                antMatchers(HttpMethod.POST, "/login").permitAll().
                antMatchers(HttpMethod.POST, "/logout").authenticated().
                antMatchers(HttpMethod.GET, "/rest/**").hasAnyAuthority("USER", "ADMIN").
                antMatchers(HttpMethod.POST, "/rest/**").hasAnyAuthority("USER", "ADMIN").
                antMatchers(HttpMethod.DELETE, "/rest/**").hasAuthority("ADMIN").
                anyRequest().authenticated();
    }

    @Bean
    public PersonDetailsService personDetailsService() {
        return new PersonDetailsService();
    }

    private Filter authenticationFilter() {
        HeaderAuthenticationFilter headerAuthenticationFilter = new HeaderAuthenticationFilter();
        headerAuthenticationFilter.userDetailsService(personDetailsService());
        headerAuthenticationFilter.headerUtil(headerUtil);
        return headerAuthenticationFilter;
    }

    private static class CustomAccessDeniedHandler implements AccessDeniedHandler {

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
            if (request.getRequestURI().startsWith("/rest/")) {
                response.setContentType(Versions.V1_0);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                try (PrintWriter out = response.getWriter()) {
                    out.print(ACCESS_DENIED_JSON);
                    out.flush();
                }
            } else {
                response.sendRedirect("/login?failure");
            }

        }
    }

    private static class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
            if (request.getRequestURI().startsWith("/rest/")) {
                response.setContentType(Versions.V1_0);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                try (PrintWriter out = response.getWriter()) {
                    out.print(UNAUTHORIZED_JSON);
                    out.flush();
                }
            } else {
                response.sendRedirect("/login");
            }
        }
    }

    private static class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

        private HeaderUtil headerUtil;

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                Authentication authentication) throws ServletException, IOException {
            
            String token;
            try {
                token = headerUtil.createAuthToken(((User) authentication.getPrincipal()).getUsername());
            } catch (GeneralSecurityException e) {
                throw new ServletException("Unable to create the auth token", e);
            }
            
            if (request.getRequestURI().startsWith("/rest/")) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode node = mapper.createObjectNode().put("token", token);
                try (PrintWriter out = response.getWriter()) {
                    out.print(node.toString());
                    out.flush();
                }
                clearAuthenticationAttributes(request);
            } else {
                super.onAuthenticationSuccess(request, response, authentication);
            }
            
        }

        private void headerUtil(HeaderUtil headerUtil) {
            this.headerUtil = headerUtil;
        }
    }

}
