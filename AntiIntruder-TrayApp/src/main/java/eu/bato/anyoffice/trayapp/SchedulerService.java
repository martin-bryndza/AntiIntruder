/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
class SchedulerService {
    
    final static Logger log = LoggerFactory.getLogger(SchedulerService.class);
        
    private static SchedulerService instance;
    
    private SchedulerService(){
        
    }
    
    static SchedulerService getInstance(){
        if (instance == null) {
            instance = new SchedulerService();
        }
        return instance;
    }
    
    void start(){
        Timer t = new Timer();
        t.scheduleAtFixedRate(new StateCheckTask(), 0, Configuration.getInstance().getIntegerProperty(Property.CHECK_INTERVAL)* 1000);
        log.info("Scheduler service started.");
    }  
    
    private class StateCheckTask extends TimerTask {

        @Override
        public void run() {
            log.info("States check started...");
            //TODO: ask server
            PersonStateManager.getInstance().setState(PersonState.AVAILABLE);
            log.info("States check finished.");
        }
    }
    
}
