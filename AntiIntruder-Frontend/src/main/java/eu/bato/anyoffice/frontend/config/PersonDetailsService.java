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
        if (!personService.isPresent("adminn")) {
            PersonDto sampleUser = new PersonDto();
            sampleUser.setUsername("adminn");
            sampleUser.setDisplayName("Administrator");
            sampleUser.setRole(PersonRole.ADMIN);
            personService.register(sampleUser, encoder.encode(environment.getProperty("auth.admin.password", "1234")));
        }
        
        if (environment.getProperty("auth.type", "DB").equals("LDAP")){
            return;
        }
        
        if (!personService.isPresent("bato")) {
            PersonDto sampleUser = new PersonDto();
            sampleUser.setUsername("bato");
            sampleUser.setDisplayName("Martin Bryndza");
            sampleUser.setDescription("QA Engineer ETNA");
            sampleUser.setLocation("R&D Open Space");
            sampleUser.setRole(PersonRole.USER);
            personService.register(sampleUser, encoder.encode("bato"));
        }

        if (!personService.isPresent("olda")) {
            PersonDto sampleUser = new PersonDto();
            sampleUser.setUsername("olda");
            sampleUser.setDisplayName("Michal Ordelt");
            sampleUser.setDescription("Developer in ETNA (KM)");
            sampleUser.setLocation("R&D Open Space");
            sampleUser.setRole(PersonRole.USER);
            personService.register(sampleUser, encoder.encode("olda"));
        }
        
        if (!personService.isPresent("myska")) {
            PersonDto sampleUser = new PersonDto();
            sampleUser.setUsername("myska");
            sampleUser.setDisplayName("Ondrej Myska");
            sampleUser.setDescription("Developer in ETNA (FX)");
            sampleUser.setLocation("R&D Open Space");
            sampleUser.setRole(PersonRole.USER);
            personService.register(sampleUser, encoder.encode("myska"));
        }

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username.equals("jarda")) {
            return new User(username, "jarda", null);
        }
        log.info("Authenticating: " + username);
        Optional<LoginDetailsDto> optDetails = personService.getLoginDetails(username);
        if (!optDetails.isPresent()) {
            if (environment.getProperty("auth.type", "DB").equals("DB")) {
                throw new UsernameNotFoundException("User with username " + username + " was not found.");
            } else {
                log.info("User with username {} logged in through LDAP for the first time", username);
                log.info(username);
                PersonDto dto = new PersonDto();
                dto.setUsername(username);
                dto.setRole(PersonRole.USER);
                dto.setDisplayName(username);
                personService.register(dto, encoder.encode("%DU)FöfA8/°LDAP%DU)FöfA8/°")); // because the pass cannot be null
            }
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
