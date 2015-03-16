package eu.bato.anyoffice.core.scheduler;

import eu.bato.anyoffice.core.state.person.PersonStateManager;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Bato
 */
public class PersonStateCheckTask extends TimerTask{
    
    final static Logger log = LoggerFactory.getLogger(PersonStateCheckTask.class);
    
    private static final PersonStateManager personStateManager = new PersonStateManager();
   
    @Override
    public void run() {
        log.info("People's states check started...");
        personStateManager.checkCurrentStatesValidity();
        log.info("People's states check finished.");
    }
}
