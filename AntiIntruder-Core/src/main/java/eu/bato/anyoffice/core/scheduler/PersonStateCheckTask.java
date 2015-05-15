package eu.bato.anyoffice.core.scheduler;

import eu.bato.anyoffice.core.person.PersonStateManager;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Task to regularly check validity of states of users.
 * @author Bato
 */
public class PersonStateCheckTask extends TimerTask {

    final static Logger log = LoggerFactory.getLogger(PersonStateCheckTask.class);

    @Autowired
    private PersonStateManager personStateManager;

    @Override
    public void run() {
        try {
            log.debug("People's states check started...");
            personStateManager.checkCurrentStatesValidity();
            log.debug("People's states check finished.");
        } catch (Exception e) {
            // because this must never fail or at least try again next time
            log.error("An exception was caught in PersonStateCheckTask.run",e);
        }
    }
}
