package eu.bato.anyoffice.backend.dao;

import eu.bato.anyoffice.backend.model.Entity;
import eu.bato.anyoffice.backend.model.Resource;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Bato
 */
@Transactional
public interface ResourceDao extends Dao<Resource, Long> {

    Entity updateState(Long id, Long stateId);

}
