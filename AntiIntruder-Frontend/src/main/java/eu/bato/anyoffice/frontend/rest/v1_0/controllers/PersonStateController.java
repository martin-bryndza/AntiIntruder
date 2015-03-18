package eu.bato.anyoffice.frontend.rest.v1_0.controllers;

import eu.bato.anyoffice.core.state.person.PersonStateManager;
import eu.bato.anyoffice.frontend.rest.Versions;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
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
import org.springframework.web.bind.annotation.RequestParam;
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
    PersonStateManager personStateManager;
    
    @RequestMapping(value = "login", method = GET)
    @ResponseStatus(HttpStatus.OK)
    public void validateCredentials(Authentication authentication) {
        log.info("User " + authentication.getName() + " connected.");
    }
    
    @RequestMapping(value="state", method = GET)
    public @ResponseBody PersonState getCurrentState(Authentication authentication) {
        return personStateManager.getCurrentState(authentication.getName());
    }
    
    @RequestMapping(value = "state", method = PUT)
    @ResponseBody
    public PersonState setCurrentState(@RequestBody PersonState newState, Authentication authentication) {
        log.info("Setting new state: " + newState + " to person " + authentication.getName());
        return personStateManager.setState(authentication.getName(), newState, false);
    }
    
    @RequestMapping(value = "locked", method = PUT)
    @ResponseBody
    public PersonState setSessionLocked(@RequestBody Boolean locked, Authentication authentication) {
        log.info("User " + authentication.getName() + (locked?" ":"started client or un") + "locked session");
        if (locked){
            return personStateManager.setState(authentication.getName(), PersonState.AWAY, false);
        } else {
            return personStateManager.returnFromAwayState(authentication.getName());
        }
    }
    
    @RequestMapping(value = "canchange", method = GET) 
    public @ResponseBody Boolean isStateChangePossible(@RequestParam String state, Authentication authentication) {
        return personStateManager.isStateChangePossible(authentication.getName(), PersonState.valueOf(state));
    }
    
}
