package eu.bato.anyoffice.frontend.rest.v1_0.controllers;

import eu.bato.anyoffice.core.person.PersonInteractionsManager;
import eu.bato.anyoffice.core.person.PersonStateManager;
import eu.bato.anyoffice.frontend.rest.Versions;
import eu.bato.anyoffice.serviceapi.dto.DisturbanceDto;
import eu.bato.anyoffice.serviceapi.dto.InteractionPersonDto;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import eu.bato.anyoffice.serviceapi.service.PersonService;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
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
public class PersonStateController {

    private static final Logger log = LoggerFactory.getLogger(PersonStateController.class);

    @Autowired
    PersonStateManager personStateManager;

    @Autowired
    PersonInteractionsManager personInteractionsManager;

    @Autowired
    PersonService personService;

    @RequestMapping(value = "login", method = GET)
    @ResponseStatus(HttpStatus.OK)
    public void validateCredentials(Authentication authentication) {
        log.info("User " + authentication.getName() + " connected.");
        ping(authentication); // temporal, until all clients are updated
    }

    @RequestMapping(value = "ping", method = PUT)
    public @ResponseBody
    String ping(Authentication authentication) {
        personService.setLastPing(authentication.getName(), new Date());
        return "pong";
    }

    @RequestMapping(value = "state", method = GET)
    public @ResponseBody
    PersonState getCurrentState(Authentication authentication) {
        ping(authentication); // temporal, until all clients are updated
        return personStateManager.getCurrentState(authentication.getName());
    }

    @RequestMapping(value = "state", method = PUT)
    @ResponseBody
    public PersonState setCurrentState(@RequestBody PersonState newState, Authentication authentication) {
        log.info("Setting new state: " + newState + " to person " + authentication.getName());
        return personStateManager.setState(authentication.getName(), newState, false);
    }

    @RequestMapping(value = "statednd", method = PUT)
    @ResponseBody
    public PersonState setDndState(@RequestBody Long period, Authentication authentication) {
        log.info("Setting new state: {} to person {}", PersonState.DO_NOT_DISTURB, authentication.getName());
        return personStateManager.setDndState(authentication.getName(), false, period);
    }

    @RequestMapping(value = "dndmax", method = GET)
    public @ResponseBody
    Long getDndMaxTime() {
        return personStateManager.getDndMaxTime();
    }

    @RequestMapping(value = "locked", method = PUT)
    @ResponseBody
    public PersonState setSessionLocked(@RequestBody Boolean locked, Authentication authentication) {
        log.info("User " + authentication.getName() + (locked ? " " : " started client or un") + "locked session");
        if (locked) {
            return personStateManager.setState(authentication.getName(), PersonState.AWAY, false);
        } else {
            return personStateManager.returnFromAwayState(authentication.getName());
        }
    }

    @RequestMapping(value = "canchange", method = GET)
    public @ResponseBody
    Boolean isStateChangePossible(@RequestParam String state, Authentication authentication) {
        return personStateManager.isStateChangePossible(authentication.getName(), PersonState.valueOf(state));
    }

    @RequestMapping(value = "location", method = GET)
    public @ResponseBody
    String getCurrentLocation(Authentication authentication) {
        return personService.getLocation(authentication.getName());
    }

    @RequestMapping(value = "location", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    public void setCurrentLocation(@RequestBody String location, Authentication authentication) {
        log.info("Setting new location: " + location + " to person " + authentication.getName());
        personService.setLocation(authentication.getName(), location);
    }

    @RequestMapping(value = "requests", method = GET)
    public @ResponseBody
    Integer getNumberOfRequests(Authentication authentication) {
        Set<InteractionPersonDto> interactingPersons = personInteractionsManager.getInteractingPersons(authentication.getName());
        int result = interactingPersons.size();
        log.debug("GET requests for user {}, response: {}", authentication.getName(), result);
        // by now, the confirmation of the fact, that the interacting persons have been see, is done here. In the future this should be done by a call from client
        personInteractionsManager.seenInteractingEntities(authentication.getName(), interactingPersons.stream().map(p -> p.getId()).collect(Collectors.toSet()));
        return result;
    }

    /**
     * Returns list of all persons, that have been requested for interaction and
     * are available now. The interaction request is canceled automatically
     * after performing this operation.
     *
     * @param authentication
     * @return map (username: displayName, location, dndStart)
     */
    @RequestMapping(value = "availableInteractionPersons", method = GET)
    public @ResponseBody
    Set<InteractionPersonDto> getNewAvailableInteractionPersons(Authentication authentication) {
        Set<InteractionPersonDto> interactionPersons = personInteractionsManager.getInteractionPersons(authentication.getName(), PersonState.AVAILABLE);
        log.debug("GET availableInteractionPersons for user {}, response size: {}", authentication.getName(), interactionPersons.size());
        // by now, the confirmation of the fact, that the interaction persons have been see, is done here. In the future this should be done by a call from client
        personInteractionsManager.seenInteractionEntities(authentication.getName(), interactionPersons.stream().map(p -> p.getId()).collect(Collectors.toSet()));
        return interactionPersons;
    }

    @RequestMapping(value = "dndStart", method = GET)
    public @ResponseBody
    Long getdndStart(Authentication authentication) {
        long result = personStateManager.getDndStart(authentication.getName());
        log.debug("GET dndStart for user {}, response: {}", authentication.getName(), result);
        return result;
    }

    @RequestMapping(value = "dndEnd", method = GET)
    public @ResponseBody
    Long getdndEnd(Authentication authentication) {
        long result = personStateManager.getDndEnd(authentication.getName());
        log.debug("GET dndEnd for user {}, response: {}", authentication.getName(), result);
        return result;
    }

    @RequestMapping(value = "disturbance", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    public void noteDisturbance(@RequestBody Boolean aoUser, Authentication authentication) {
        log.info("Noting disturbance by AnyOffice user: " + aoUser + " to person " + authentication.getName());
        personService.noteDisturbance(authentication.getName(), aoUser);
    }

}
