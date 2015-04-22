package eu.bato.anyoffice.backend.dao;

import eu.bato.anyoffice.backend.model.ConsultationRequest;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Bato
 */
@Transactional
public interface ConsultationRequestDao extends Dao<ConsultationRequest, Long> {

}
