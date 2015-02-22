/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.web.config;

import com.ysoft.tools.antiintruder.serviceapi.dto.LoginDetailsDto;
import com.ysoft.tools.antiintruder.serviceapi.dto.PersonDto;
import com.ysoft.tools.antiintruder.serviceapi.dto.PersonRole;
import com.ysoft.tools.antiintruder.serviceapi.service.PersonService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
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
public class PersonDetailsService implements UserDetailsService{
    
    @Autowired
    private PersonService personService;
    
    @PostConstruct
    protected void initialize() {
        PersonDto admin = new PersonDto();
        admin.setUsername("admin");
        admin.setDisplayName("Administrator");
        admin.setDescription("The administrator");
        admin.setRole(PersonRole.ADMIN);
        admin.setStateId(1L);
        StandardPasswordEncoder encoder = new StandardPasswordEncoder();
        personService.register(admin, encoder.encode("admin"));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<LoginDetailsDto> optDetails = personService.getLoginDetails(username);
        if (!optDetails.isPresent()) throw new UsernameNotFoundException("User with username " + username + " was not found.");      
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(optDetails.get().getRole().name()));        
        return new User(username, optDetails.get().getPassword(), authorities);
    }
    
}
