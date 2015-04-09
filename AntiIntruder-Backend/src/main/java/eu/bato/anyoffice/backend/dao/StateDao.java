package eu.bato.anyoffice.backend.dao;

import eu.bato.anyoffice.backend.model.State;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Bato
 */
@Transactional
public interface StateDao extends Dao<State, Long> {

}
