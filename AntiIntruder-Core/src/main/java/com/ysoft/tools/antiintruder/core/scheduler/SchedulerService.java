/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.core.scheduler;

import java.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

/**
 *
 * @author Bato
 */
@Service
@Configuration
public class SchedulerService {
    
    final static Logger log = LoggerFactory.getLogger(SchedulerService.class);
        
    public void start(){
        Timer t = new Timer();
        t.scheduleAtFixedRate(stateCheckTask(), 0, 10000); //TODO: make timer configurable
        log.info("Scheduler service started.");
    }
    
    @Bean
    public StateCheckTask stateCheckTask() {
        return new StateCheckTask();
    }
    
    
}
