package eu.bato.anyoffice.backend.dao.impl;

import eu.bato.anyoffice.backend.dao.DisturbanceDao;
import eu.bato.anyoffice.backend.model.Disturbance;
import eu.bato.anyoffice.backend.model.Person;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Bato
 */
@Repository
@Transactional(noRollbackFor = NoResultException.class)
public class DisturbanceDaoImpl implements DisturbanceDao {

    final static Logger log = LoggerFactory.getLogger(DisturbanceDaoImpl.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid record: null or with no id.");
        }
        Person person = em.find(Person.class, id);
        if (person == null) {
            log.error("Person with id " + id + " is not in DB");
        }
        em.remove(person);
    }

    @Override
    public List<Disturbance> findAll() {
        return em.createQuery("SELECT tbl FROM Disturbance tbl", Disturbance.class).getResultList();
    }

    @Override
    public Disturbance findOne(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid id: " + id);
        }
        return em.createQuery("SELECT e FROM Disturbance e WHERE e.id = :pk", Disturbance.class).setParameter("pk", id).getSingleResult();

    }

    @Override
    public Disturbance save(Disturbance disturbance) {
        if (disturbance == null) {
            throw new IllegalArgumentException("Invalid entity (Disturbance): " + disturbance);
        }

        log.debug("Saving " + disturbance.toString());
        Disturbance modelDisturbance = em.merge(disturbance);
        log.info("Saved " + modelDisturbance.toString() + ".");
        log.debug(" Assigned entity id: " + modelDisturbance.getId());
        return modelDisturbance;
    }

}
