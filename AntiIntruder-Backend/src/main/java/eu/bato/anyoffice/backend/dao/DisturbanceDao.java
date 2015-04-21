package eu.bato.anyoffice.backend.dao;

import eu.bato.anyoffice.backend.model.Disturbance;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Bato
 */
@Transactional
public interface DisturbanceDao extends Dao<Disturbance, Long> {

}
