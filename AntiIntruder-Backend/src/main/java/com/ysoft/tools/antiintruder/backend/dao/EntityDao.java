package com.ysoft.tools.antiintruder.backend.dao;

import com.ysoft.tools.antiintruder.backend.model.Entity;

/**
 *
 * @author Bato
 */
public interface EntityDao extends Dao<Entity, Long>{
    
    Entity updateState(Long id, Long stateId);
    
}
