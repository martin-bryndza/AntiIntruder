package com.ysoft.tools.antiintruder.serviceapi.service;

import com.ysoft.tools.antiintruder.serviceapi.dto.ResourceDto;

/**
 *
 * @author Bato
 */
public interface ResourceService extends Service<ResourceDto>{
    
    /**
     * Sets State of the Resource identified by id to the State identified by stateId.
     * @param id Id of the Entity
     * @param stateId Id of the State
     */
    void updateState(Long id, Long stateId);
    
}
