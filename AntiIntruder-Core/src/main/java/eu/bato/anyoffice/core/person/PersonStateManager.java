package eu.bato.anyoffice.core.person;

import eu.bato.anyoffice.core.config.Configuration;
import eu.bato.anyoffice.core.config.Property;
import eu.bato.anyoffice.core.integration.hipchat.HipChatClient;
import eu.bato.anyoffice.serviceapi.dto.HipChatCredentials;
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

    @Autowired
    HipChatClient hipChatClient;

    /**
     * Validates the change of the state and sets the new person state if
     * possible
     *
     * @param username
     * @param state
     * @param force
     * @return
     */
    public PersonState setState(String username, PersonState state, boolean force) {
        PersonState checkState = beforeSwitchCheck(username, state, force);
        if (checkState != null) {
            return checkState;
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

    public PersonState setDndState(String username, boolean force, Long period) {
        PersonState checkState = beforeSwitchCheck(username, PersonState.DO_NOT_DISTURB, force);
        if (checkState != null) {
            return checkState;
        }
        setDndState(username, period);
        return PersonState.DO_NOT_DISTURB;
    }

    public PersonState setDndState(Long id, boolean force, Long period) {
        return setDndState(personService.getUsername(id), force, period);
    }

    /**
     *
     * @param username
     * @param state
     * @param force
     * @return the state that should be returned to user or null if everything
     * is OK
     */
    private PersonState beforeSwitchCheck(String username, PersonState state, boolean force) {
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
        return null;
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
            setHipChatState(username, PersonState.AVAILABLE);
            return PersonState.AVAILABLE;
        } else if (p.getDndStart().compareTo(p.getDndEnd()) < 0 && p.getDndEnd().compareTo(now) <= 0) {
            // previous state was DND but it's already over
            setAvailableState(username);
            return PersonState.AVAILABLE;
        } else {
            // previous state was DND and it's still valid
            personService.setState(username, PersonState.DO_NOT_DISTURB);
            setHipChatState(username, PersonState.DO_NOT_DISTURB);
            return PersonState.DO_NOT_DISTURB;
        }
    }

    public PersonState returnFromUnknownState(String username) {
        return returnFromAwayState(username);
    }

    public final PersonState getCurrentState(String username) {
        checkCurrentStateValidity(username);
        return personService.getState(username);
    }

    public void updateHipChatStatuses() {
        log.info("Updating statuses on HipChat...");
        List<String> allUsernames = personService.findAllUsernames();
        if (allUsernames != null) {
            allUsernames.forEach(p -> setHipChatState(p, getCurrentState(p)));
        }
        log.info("Updating statuses finished.");
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
            log.info("Setting state AVAILABLE by state validity check for user {}", username);
            setAvailableState(username);
        } else if (!person.getState().equals(PersonState.UNKNOWN)) {
            long fromLastPing = new Date().getTime() - person.getLastPing().orElse(0L);
            if (!person.getState().equals(PersonState.AWAY) && fromLastPing > Configuration.getInstance().getLongProperty(Property.MAXIMUM_PING_DELAY)) {
                log.info("Setting state UNKNOWN by state validity check for user {} - MAXIMUM_PING_DELAY has been reached.", username);
                setUnknownAwayState(username, PersonState.UNKNOWN);
            } else if (person.getState().equals(PersonState.AWAY) && fromLastPing > Configuration.getInstance().getLongProperty(Property.MAXIMUM_AWAY_PING_DELAY)) {
                log.info("Setting state UNKNOWN by state validity check for user {} - MAXIMUM_AWAY_PING_DELAY has been reached.", username);
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

    public long getDndStart(String username) {
        //TODO make more effective
        return personService.findOneByUsername(username).getDndStart();
    }

    public long getDndEnd(String username) {
        //TODO make more effective
        return personService.findOneByUsername(username).getDndEnd();
    }

    public long getDndMaxTime() {
        return Configuration.getInstance().getLongProperty(Property.MAX_DND_TIME);
    }

    /**
     *
     * @param username
     * @param millisToAdd
     * @return the new dndEnd in milliseconds
     */
    public long addDndTime(String username, long millisToAdd) {
        PersonState state = getCurrentState(username);
        if (!state.equals(PersonState.DO_NOT_DISTURB)) {
            throw new IllegalStateException("Unable to add time to DND state for person " + username + ". Person is in state " + state);
        }
        long dndStart = getDndStart(username);
        long dndEnd = getDndEnd(username) + millisToAdd;
        if (dndEnd - dndStart > getDndMaxTime()) {
            throw new IllegalArgumentException("Unable to add " + millisToAdd + " millis to DND state of " + username + ". It would exceed MAX_DND_TIME");
        }
        personService.setTimers(username, Optional.empty(), Optional.of(new Date(dndEnd)), Optional.empty());
        return dndEnd;
    }

    private void setAvailableState(String username) {
        Date now = new Date();
        Long minAvailableTime = Configuration.getInstance().getLongProperty(Property.MIN_AVAILABLE_TIME);
        Optional<Date> dndStart = Optional.of(new Date(now.getTime() + minAvailableTime));
        personService.setTimers(username, dndStart, Optional.of(now), Optional.empty());
        personService.setState(username, PersonState.AVAILABLE);
        setHipChatState(username, PersonState.AVAILABLE);
    }

    private void setDndState(String username, Long period) {
        Date now = new Date();
        Long maxDndTime = Configuration.getInstance().getLongProperty(Property.MAX_DND_TIME);
        if (period > maxDndTime) {
            throw new IllegalArgumentException("DND for user " + username + " can not be set for more than " + maxDndTime + ". Got: " + period);
        }
        Optional<Date> dndEnd = Optional.of(new Date(now.getTime() + period));
        personService.setTimers(username, Optional.of(now), dndEnd, Optional.empty());
        personService.setState(username, PersonState.DO_NOT_DISTURB);
        setHipChatState(username, PersonState.DO_NOT_DISTURB);
    }

    private void setDndState(String username) {
        Long maxDndTime = Configuration.getInstance().getLongProperty(Property.MAX_DND_TIME);
        setDndState(username, maxDndTime);
    }

    private void setUnknownAwayState(String username, PersonState state) {
        personService.setTimers(username, Optional.empty(), Optional.empty(), Optional.of(new Date()));
        personService.setState(username, state);
        setHipChatState(username, state);
    }

    private void setHipChatState(String username, PersonState state) {
        new Thread(() -> {
            HipChatCredentials hcc = personService.getHipChatCredentials(username);
            if (hcc.getEmail().isPresent() && hcc.getToken().isPresent() && !state.equals(PersonState.UNKNOWN)) {
                hipChatClient.setState(hcc.getToken().get(), hcc.getEmail().get(), state, "AnyOffice");
            }
        }).start();
    }

}
