package eu.bato.anyoffice.trayapp;

import eu.bato.anyoffice.trayapp.config.Configuration;
import eu.bato.anyoffice.trayapp.config.Property;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Bato
 */
class StateCheckService {
    
    final static Logger log = LoggerFactory.getLogger(StateCheckService.class);
        
    private static StateCheckService instance;
    
    private StateCheckService(){
        
    }
    
    static StateCheckService getInstance(){
        if (instance == null) {
            instance = new StateCheckService();
        }
        return instance;
    }
    
    void start(){
        TrayIconManager.getInstance().changeState(PersonStateManager.getInstance().workstationUnlock());
        Timer t = new Timer();
        t.scheduleAtFixedRate(new StateCheckTask(), 2000, Configuration.getInstance().getIntegerProperty(Property.CHECK_INTERVAL)* 1000);
        log.info("Scheduler service started.");
    }  
    
    private class StateCheckTask extends TimerTask {

        @Override
        public void run() {
            log.debug("States check started...");
            TrayIconManager.getInstance().changeState(PersonStateManager.getInstance().getStateFromServer());
            log.debug("States check finished.");
        }
    }
    
}
