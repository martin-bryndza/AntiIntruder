package eu.bato.anyoffice.backend.dao;

import eu.bato.anyoffice.backend.model.StateSwitch;
import java.util.Date;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Bato
 */
@Transactional
public interface StateSwitchDao {
    
    List<StateSwitch> findRangeForUser(Long id, Date from, Date to);

}
