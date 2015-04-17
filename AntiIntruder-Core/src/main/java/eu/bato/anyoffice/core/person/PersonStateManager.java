/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.core.person;

import eu.bato.anyoffice.core.config.Configuration;
import eu.bato.anyoffice.core.config.Property;
import eu.bato.anyoffice.serviceapi.dto.PersonDto;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import eu.bato.anyoffice.serviceapi.service.PersonService;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Bato
 */
@Service
public class PersonStateManager {

    private final static Logger log = LoggerFactory.getLogger(PersonStateManager.class);
    
    @Autowired
    PersonService personService;

    public PersonState setState(String username, PersonState state, boolean force) {
        PersonState current = getCurrentState(username);
        if (current.equals(state)) {
            log.info("Attempt to change state of user " + username + " to the currently set state " + current + " => no action.");
            return state;
        }
        if (current.equals(PersonState.AWAY) && !state.equals(PersonState.UNKNOWN)) {
            log.warn("Attemp to change state while AWAY. Returning from AWAY state first.");
            current = returnFromAwayState(username);
        }
        if (current.equals(PersonState.UNKNOWN) && !state.equals(PersonState.AWAY)) {
            log.warn("Attemp to change state while the state is UNKNOWN. Returning from UNKNOWN state first.");
            current = returnFromUnknownState(username);
        }
        //now the current is DND or AVAILABLE
        if (!force && !isStateChangePossible(username, state)) {
            log.warn("Unable to switch from state {} to state {} at the moment.", current, state);
            return current;
        }

        switch (state) {
            case AWAY:
            case UNKNOWN: // * -> AWAY/UNKNOWN
                setUnknownAwayState(username, state);
                return state;
            case AVAILABLE: // DND -> AVAILABLE
                setAvailableState(username);
                return PersonState.AVAILABLE;
            case DO_NOT_DISTURB: // AVAILABLE -> DND (check has been done before)
                setDndState(username);
                return PersonState.DO_NOT_DISTURB;
            default:
                throw new IllegalArgumentException("The state " + state + " is not recognized.");
        }
    }

    public PersonState setState(Long id, PersonState state, boolean force) {
        return setState(personService.getUsername(id), state, force);
    }

    public PersonState returnFromAwayState(String username) {
        PersonDto p = personService.findOneByUsername(username);
        Long now = new Date().getTime();
        PersonState current = getCurrentState(username);
        if (!current.equals(PersonState.AWAY) && !current.equals(PersonState.UNKNOWN)) {
            log.info("User " + username + " has not been AWAY.");
            return current;
        }
        if (p.getDndStart().compareTo(p.getDndEnd()) >= 0) {
            //previous state was AVAILABLE
            Long newDndStart = p.getDndStart() + (now - p.getAwayStart().orElseThrow(() -> {
                return new IllegalStateException("User " + username + " is in state AWAY/UNKNOWN but awayStart is empty.");
            }));
            personService.setTimers(username, Optional.of(new Date(newDndStart)), Optional.empty(), Optional.empty());
            personService.setState(username, PersonState.AVAILABLE);
            return PersonState.AVAILABLE;
        } else if (p.getDndStart().compareTo(p.getDndEnd()) < 0 && p.getDndEnd().compareTo(now) <= 0) {
            // previous state was DND but it's already over
            setAvailableState(username);
            return PersonState.AVAILABLE;
        } else {
            // previous state was DND and it's still valid
            personService.setState(username, PersonState.DO_NOT_DISTURB);
            return PersonState.DO_NOT_DISTURB;
        }
    }

    public PersonState returnFromUnknownState(String username) {
        return returnFromAwayState(username);
    }

    public PersonState getCurrentState(String username) {
        checkCurrentStateValidity(username);
        return personService.getState(username);
    }

    public void checkCurrentStatesValidity() {
        List<String> allUsernames = personService.findAllUsernames();
        if (allUsernames != null) {
            allUsernames.forEach(p -> checkCurrentStateValidity(p));
        }
    }

    public void checkCurrentStateValidity(String username) {
        PersonDto person = personService.findOneByUsername(username);
        if (person.getState().equals(PersonState.DO_NOT_DISTURB) && person.getDndEnd().compareTo(new Date().getTime()) <= 0) {
            // current state is DND and it should have ended
            personService.setState(username, PersonState.AVAILABLE);
        } else if (!person.getState().equals(PersonState.UNKNOWN)) {
            long fromLastPing = new Date().getTime() - person.getLastPing().orElse(0L);
            if (!person.getState().equals(PersonState.AWAY) && fromLastPing > Configuration.getInstance().getLongProperty(Property.MAXIMUM_PING_DELAY)){
                setUnknownAwayState(username, PersonState.UNKNOWN);
            } else if (person.getState().equals(PersonState.AWAY) && fromLastPing > Configuration.getInstance().getLongProperty(Property.MAXIMUM_AWAY_PING_DELAY)){
                setUnknownAwayState(username, PersonState.UNKNOWN);
            }
        }
    }

    public boolean isStateChangePossible(String username, PersonState toState) {
        if (PersonState.DO_NOT_DISTURB.equals(toState)) {
            PersonDto p = personService.findOneByUsername(username);
            return p.getDndStart().compareTo(new Date().getTime()) < 0; //DNDstart < NOW
        } else {
            return true;
        }
    }
    
    public long getDndStart(String username){
        //TODO make more effective
        return personService.findOneByUsername(username).getDndStart();
    }
    
    public long getDndEnd(String username){
        //TODO make more effective
        return personService.findOneByUsername(username).getDndEnd();
    }

    private void setAvailableState(String username) {
        Date now = new Date();
        Long minAvailableTime = Configuration.getInstance().getLongProperty(Property.MIN_AVAILABLE_TIME);
        Optional<Date> dndStart = Optional.of(new Date(now.getTime() + minAvailableTime));
        personService.setTimers(username, dndStart, Optional.of(now), Optional.empty());
        personService.setState(username, PersonState.AVAILABLE);
    }

    private void setDndState(String username) {
        Date now = new Date();
        Long maxDndTime = Configuration.getInstance().getLongProperty(Property.MAX_DND_TIME);
        Optional<Date> dndEnd = Optional.of(new Date(now.getTime() + maxDndTime));
        personService.setTimers(username, Optional.of(now), dndEnd, Optional.empty());
        personService.setState(username, PersonState.DO_NOT_DISTURB);
    }

    private void setUnknownAwayState(String username, PersonState state) {
        personService.setTimers(username, Optional.empty(), Optional.empty(), Optional.of(new Date()));
        personService.setState(username, state);
    }

}
