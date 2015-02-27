package eu.bato.anyoffice.backend.dao;

import eu.bato.anyoffice.backend.model.Entity;
import eu.bato.anyoffice.backend.model.Resource;

/**
 *
 * @author Bato
 */
public interface ResourceDao extends Dao<Resource, Long>{
    
    Entity updateState(Long id, Long stateId);
    
}
