package com.ysoft.tools.antiintruder.core.scheduler;

import com.ysoft.tools.antiintruder.serviceapi.dto.ResourceDto;
import com.ysoft.tools.antiintruder.serviceapi.dto.StateDto;
import com.ysoft.tools.antiintruder.serviceapi.service.ResourceService;
import com.ysoft.tools.antiintruder.serviceapi.service.StateService;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Bato
 */
public class StateCheckTask extends TimerTask{
    
    final static Logger log = LoggerFactory.getLogger(StateCheckTask.class);
    
    @Autowired
    ResourceService resourceService;
    
    @Autowired
    StateService stateService;
   
    @Override
    public void run() {
        log.info("States check started...");
        Map<Long, Long> statesDefaultSuccessors = new HashMap<>();
        for (StateDto state : stateService.findAll()){ //TODO: change to find all states of EntityType
            statesDefaultSuccessors.put(state.getId(), state.getDefaultSuccessorId());
        } 
        List<ResourceDto> entities = resourceService.findAll(); //TODO: change to find all entities of EntityType
        Date currentDate = new Date(Calendar.getInstance().getTimeInMillis());
        for (ResourceDto entity : entities){
            if (entity.getStateExpiration()!=null && entity.getStateExpiration().compareTo(currentDate)<=0){
                entity.setStateId(statesDefaultSuccessors.get(entity.getStateId()));
                resourceService.updateState(entity.getId(), entity.getStateId());
                log.info("State of entity " + entity.getId() + " expired. New state is " + entity.getStateId());
            }
        }
        log.info("States check finished.");
    }
}
