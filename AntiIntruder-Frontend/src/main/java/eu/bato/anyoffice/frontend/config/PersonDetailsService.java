package eu.bato.anyoffice.frontend.config;

import eu.bato.anyoffice.serviceapi.dto.LoginDetailsDto;
import eu.bato.anyoffice.serviceapi.dto.PersonDto;
import eu.bato.anyoffice.serviceapi.dto.PersonRole;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import eu.bato.anyoffice.serviceapi.service.PersonService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
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
public class PersonDetailsService implements UserDetailsService{
    
    private static final StandardPasswordEncoder encoder = new StandardPasswordEncoder();
    
    @Autowired
    private PersonService personService;
    
    @Autowired
    private Environment environment;
    
    @PostConstruct
    protected void initialize() {
        if (personService.findOneByUsername("bato").isPresent()){
            return;
        }
        PersonDto sampleUser = new PersonDto();
        sampleUser.setUsername("bato");
        sampleUser.setDisplayName("User");
        sampleUser.setDescription("A user");
        sampleUser.setRole(PersonRole.USER);
        sampleUser.setState(PersonState.AVAILABLE);
        personService.register(sampleUser, encoder.encode("1111"));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if ("admin".equals(username)){
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(PersonRole.ADMIN.name()));
            return new User(username, encoder.encode(environment.getProperty("auth.admin.password", "1234")), authorities);
        }
        Optional<LoginDetailsDto> optDetails = personService.getLoginDetails(username);
        if (!optDetails.isPresent()) throw new UsernameNotFoundException("User with username " + username + " was not found.");      
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(optDetails.get().getRole().name()));        
        return new User(username, optDetails.get().getPassword(), authorities);
    }
    
}
