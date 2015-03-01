package eu.bato.anyoffice.frontend.rest.v1_0.controllers;

import eu.bato.anyoffice.serviceapi.dto.PersonState;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Bato
 */
@Transactional
@RestController("PersonStateResourceV1")
@RequestMapping(value = "/rest/status"/*, 
produces = Versions.V1_0, 
consumes = Versions.V1_0*/)
public class PersonStateResource {
    
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String getCurrentState() {
        return PersonState.AVAILABLE.name();
    }
    
}
