package eu.bato.anyoffice.frontend.rest.v1_0.controllers;

import eu.bato.anyoffice.frontend.rest.Versions;
import eu.bato.anyoffice.frontend.rest.v1_0.data.PersonStateJson;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import eu.bato.anyoffice.serviceapi.service.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
//@PreAuthorize("hasAuthority('USER')") // For @PreAuthorize annotation to have effect there is a need to have @EnableGlobalMethodSecurity annotation on @Configuration bean somewhere
@RequestMapping(value = "/api/v1", 
produces = Versions.V1_0, 
consumes = Versions.V1_0)
public class PersonStateController{
    
    private static final Logger log = LoggerFactory.getLogger(PersonStateController.class);
    
    @Autowired
    PersonService personService;
    
    @RequestMapping(value="state", method = GET)
    @ResponseBody
    public String getCurrentState() {
        return personService.getState(1L).toString();
    }
    
    @RequestMapping(value = "state", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    public void setCurrentState(@RequestBody PersonStateJson newState) {
        log.info("Setting new state: " + newState);
        personService.setState(1L, PersonState.valueOf(newState.name()));
    }
    
//    @RequestMapping(value = "state", method = PUT)
//    @ResponseStatus(HttpStatus.OK)
//    public void setSessionLocked() {
//        log.info("User locked session");
//        personService.
//        personService.setState(1L, PersonState.valueOf(newState.name()));
//    }
    
}
