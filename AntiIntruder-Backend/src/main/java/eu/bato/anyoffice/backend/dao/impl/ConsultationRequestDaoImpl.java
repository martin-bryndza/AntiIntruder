package eu.bato.anyoffice.backend.dao.impl;

import eu.bato.anyoffice.backend.dao.ConsultationRequestDao;
import eu.bato.anyoffice.backend.model.ConsultationRequest;
import eu.bato.anyoffice.backend.model.Person;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author bryndza
 */
@Repository
@Transactional
public class ConsultationRequestDaoImpl implements ConsultationRequestDao {

    @PersistenceContext
    private EntityManager em;

    final static Logger log = LoggerFactory.getLogger(DisturbanceDaoImpl.class);

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid record: null or with no id.");
        }
        Person person = em.find(Person.class, id);
        if (person == null) {
            log.error("Consultation request with id " + id + " is not in DB");
        }
        em.remove(person);
    }

    @Override
    public List<ConsultationRequest> findAll() {
        return em.createQuery("SELECT tbl FROM ConsultationRequest tbl", ConsultationRequest.class).getResultList();
    }

    @Override
    public ConsultationRequest findOne(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid id: " + id);
        }
        return em.createQuery("SELECT e FROM ConsultationRequest e WHERE e.id = :pk", ConsultationRequest.class).setParameter("pk", id).getSingleResult();

    }

    @Override
    public ConsultationRequest save(ConsultationRequest consultationRequest) {
        if (consultationRequest == null) {
            throw new IllegalArgumentException("Invalid entity (ConsultationRequest): " + consultationRequest);
        }

        log.debug("Saving " + consultationRequest.toString());
        ConsultationRequest modelConsultationRequest = em.merge(consultationRequest);
        log.info("Saved " + modelConsultationRequest.toString() + ".");
        log.debug(" Assigned entity id: " + modelConsultationRequest.getId());
        return modelConsultationRequest;
    }

}
