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

    public void start() {
        Timer t = new Timer();
        Long period = Configuration.getInstance().getLongProperty(Property.PERSON_STATE_CHECK_INTERVAL);
        if (period <= 0) {
            log.info("Scheduler service for people's states check is disabled.");
        } else {
            t.schedule(personStateCheckTask(), period, period);
            log.info("Scheduler service for people's states check started with interval " + period);
        }
    }

    @Bean
    public PersonStateCheckTask personStateCheckTask() {
        return new PersonStateCheckTask();
    }

}
