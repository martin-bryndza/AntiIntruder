package com.ysoft.tools.antiintruder.backend.dao;

import com.ysoft.tools.antiintruder.backend.model.Entity;
import com.ysoft.tools.antiintruder.backend.model.Resource;

/**
 *
 * @author Bato
 */
public interface ResourceDao extends Dao<Resource, Long>{
    
    Entity updateState(Long id, Long stateId);
    
}
