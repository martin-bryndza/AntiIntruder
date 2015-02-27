/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.core.scheduler;

import eu.bato.anyoffice.core.config.Configuration;
import eu.bato.anyoffice.core.config.Property;
import java.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

/**
 *
 * @author Bato
 */
@Service
@org.springframework.context.annotation.Configuration
public class SchedulerService {
    
    final static Logger log = LoggerFactory.getLogger(SchedulerService.class);
        
    public void start(){
        Timer t = new Timer();
        t.scheduleAtFixedRate(stateCheckTask(), 0, Configuration.getInstance().getIntegerProperty(Property.STATE_CHECK_INTERVAL)*1000);
        log.info("Scheduler service started.");
    }
    
    @Bean
    public StateCheckTask stateCheckTask() {
        return new StateCheckTask();
    }
    
    
}
