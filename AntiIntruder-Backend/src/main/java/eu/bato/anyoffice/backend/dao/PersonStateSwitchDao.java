package eu.bato.anyoffice.backend.dao;

import eu.bato.anyoffice.backend.model.PersonStateSwitch;
import java.util.Date;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Bato
 */
@Transactional
public interface PersonStateSwitchDao {
    
    List<PersonStateSwitch> findRangeForUser(Long id, Date from, Date to);

}
