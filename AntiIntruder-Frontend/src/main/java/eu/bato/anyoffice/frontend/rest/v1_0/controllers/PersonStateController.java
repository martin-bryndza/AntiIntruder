package eu.bato.anyoffice.frontend.rest.v1_0.controllers;

import eu.bato.anyoffice.frontend.rest.Versions;
import eu.bato.anyoffice.frontend.rest.v1_0.data.PersonStateJson;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import eu.bato.anyoffice.serviceapi.service.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Bato
 */
@Transactional
@RestController("PersonStateResourceV1")
@RequestMapping(value = "/api/v1", 
produces = Versions.V1_0, 
consumes = Versions.V1_0)
public class PersonStateController{
    
    private static final Logger log = LoggerFactory.getLogger(PersonStateController.class);
    
    @Autowired
    PersonService personService;
    
    @RequestMapping(value = "login", method = GET)
    @ResponseStatus(HttpStatus.OK)
    public void validateCredentials(Authentication authentication) {
        log.info("User " + authentication.getName() + " connected.");
    }
    
    @RequestMapping(value="state", method = GET)
    @ResponseBody
    public String getCurrentState(Authentication authentication) {
        return personService.getState(authentication.getName()).toString();
    }
    
    @RequestMapping(value = "state", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    public void setCurrentState(@RequestBody PersonStateJson newState, Authentication authentication) {
        log.info("Setting new state: " + newState);
        personService.setState(authentication.getName(), PersonState.valueOf(newState.name()));
    }
    
    @RequestMapping(value = "locked", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    public void setSessionLocked() {
        log.info("User locked session");
        
    }
    
}
