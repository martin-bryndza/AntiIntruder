package eu.bato.anyoffice.backend.dao.impl;

import eu.bato.anyoffice.backend.dao.PersonDao;
import eu.bato.anyoffice.backend.model.Entity;
import eu.bato.anyoffice.backend.model.Person;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
@Transactional
public class PersonDaoImpl implements PersonDao {

    final static Logger log = LoggerFactory.getLogger(PersonDaoImpl.class);

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
    public List<Person> findAll() {
        return em.createQuery("SELECT tbl FROM Person tbl", Person.class).getResultList();
    }

    @Override
    public Person findOne(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid id: " + id);
        }
            return em.createQuery("SELECT e FROM Person e WHERE e.id = :pk", Person.class).setParameter("pk", id).getSingleResult();
        
    }

    @Override
    public Person findOneByUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Invalid username: " + username);
        }
            return em.createQuery("SELECT e FROM Person e WHERE e.username = :username", Person.class).setParameter("username", username).getSingleResult();
    }

    @Override
    public Person save(Person person) {
        if (person == null) {
            throw new IllegalArgumentException("Invalid entity (Person): " + person);
        }
        //if the password is empty, use the password that is already stored
        if (person.getPassword() == null || person.getPassword().isEmpty()) {
            //if the person does not have stopred Entity, it is a new person
            if (person.getId() == null) {
                throw new IllegalArgumentException("Unable to create a new person with empty password.");
            }
            String currentPass;
            try {
                currentPass = em.createQuery("SELECT tbl.password FROM Person tbl WHERE tbl.id = "
                        + ":givenId", String.class).setParameter("givenId", person.getId()).getSingleResult();
            } catch (NoResultException e) {
                throw new IllegalArgumentException("Unable to create a new person with empty password.");
            }
            person.setPassword(currentPass);
        }
        log.info("Saving " + person.toString());
        Person modelPerson = em.merge(person);
        log.info("Saved " + modelPerson.toString() + ".");
        log.info(" Assigned entity id: " + modelPerson.getId());
        return modelPerson;
    }

    @Override
    public Person updateState(Long id, PersonState personState) {
        Person e = findOne(id);
        return updateState(e, personState);
    }

    @Override
    public Person updateState(String username, PersonState personState) {
        Person e = findOneByUsername(username);
        return updateState(e, personState);
    }

    private Person updateState(Person e, PersonState personState) {
        if (e.getState().equals(personState)) {
            log.info("Actual and wanted states are the same. State of person " + e.getUsername() + " will not be changed.");
            return e;
        } 
        e.setState(personState);
        return e;
    }

    @Override
    public void updateTimers(String username, Optional<Date> dndStart, Optional<Date> dndEnd, Optional<Date> awayStart) {
        if (dndStart == null || dndEnd == null || awayStart == null) {
            throw new IllegalArgumentException("Some of properties is null: dndStart=" + dndStart + ", dndEnd=" + dndEnd + ", awayStart = " + awayStart + ". Use Optional.empty instead.");
        }
        Person e = findOneByUsername(username);
        e.setAwayStart(awayStart);
        e.setDndEnd(dndEnd.orElse(e.getDndEnd()));
        e.setDndStart(dndStart.orElse(e.getDndStart()));
    }

    @Override
    public void addInteractionEntity(String username, Long interactionEntityId) {
        Person p1 = findOneByUsername(username);
        Person p2 = findOne(interactionEntityId); //TODO should be Entity
        p1.addInteractionEntity(p2);
    }

    @Override
    public void removeInteractionEntity(String username, Long interactionEntityId) {
        Person p1 = findOneByUsername(username);
        Person p2 = findOne(interactionEntityId); //TODO should be Entity
        p1.removeInteractionEntity(p2);
    }

    @Override
    public void removeAllInteractionEntities(String username) {
        Person p1 = findOneByUsername(username);
        p1.removeAllInteractionEntities();
    }

    @Override
    public void removeInteractionEntities(String username, Collection<Long> ids) {
        Person p1 = findOneByUsername(username);
        ids.stream().map((id) -> findOne(id)).forEach((p2) -> { //TODO should be Entity
            p1.removeInteractionEntity(p2);
        });
        p1.removeAllInteractionEntities();
    }

    @Override
    public List<Person> getInteractingPersons(String username) {
        Person person = findOneByUsername(username);
        return person.getInteractingPersons();
    }

    @Override
    public void removeAllInteractingPersons(String username) {
        Person person = findOneByUsername(username);
        person.removeAllInteractingPersons();
    }
    
    @Override
    public List<Person> getInteractionPersons(String username) {
        Person person = findOneByUsername(username);
        List<Entity> entities = person.getInteractionEntities();
        List<Person> persons = new LinkedList<>();
        entities.forEach((e) -> {
            if (Person.class.isInstance(e)){
                persons.add((Person) e);
            }
        });
        return persons;
    }
    
    @Override
    public boolean isTaken(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username is null.");
        }
        return em.createQuery("SELECT tbl.id FROM Person tbl WHERE tbl.username = "
                + ":givenUsername", Long.class).setParameter("givenUsername", username).getResultList().size() > 0;
    }

    @Override
    public void setLocation(String username, String location) {
        Person p1 = findOneByUsername(username);
        p1.setLocation(location==null?"":location);
    }

    @Override
    public String getLocation(String username) {
        String location = findOneByUsername(username).getLocation();
        return location == null ? "" : location;
    }

}
