package eu.bato.anyoffice.backend.dao.impl;

import eu.bato.anyoffice.backend.dao.PersonStateSwitchDao;
import eu.bato.anyoffice.backend.model.PersonStateSwitch;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author bryndza
 */
@Repository
@Transactional
public class PersonStateSwitchDaoImpl implements PersonStateSwitchDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<PersonStateSwitch> findRangeForUser(Long id, Date from, Date to) {
        TypedQuery<PersonStateSwitch> query = em.createQuery("SELECT tbl FROM StateSwitch tbl WHERE tbl.personId = :id AND tbl.time >= :from AND tbl.time < :to", PersonStateSwitch.class)
                .setParameter("id", id)
                .setParameter("from", from)
                .setParameter("to", to);
        List<PersonStateSwitch> result = query.getResultList();
        return result;
    }

}
