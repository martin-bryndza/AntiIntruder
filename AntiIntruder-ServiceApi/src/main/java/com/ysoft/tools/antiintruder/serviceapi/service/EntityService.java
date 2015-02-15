package com.ysoft.tools.antiintruder.serviceapi.service;

import com.ysoft.tools.antiintruder.serviceapi.dto.EntityDto;

/**
 *
 * @author Bato
 */
public interface EntityService extends Service<EntityDto>{
    
    /**
     * Sets State of the Entity identified by id to the State identified by stateId.
     * @param id Id of the Entity
     * @param stateId Id of the State
     */
    void updateState(Long id, Long stateId);
    
}
