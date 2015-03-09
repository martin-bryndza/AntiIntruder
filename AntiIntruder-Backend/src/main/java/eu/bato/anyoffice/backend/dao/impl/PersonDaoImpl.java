package eu.bato.anyoffice.backend.dao.impl;

import eu.bato.anyoffice.backend.dao.PersonDao;
import eu.bato.anyoffice.backend.model.Person;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
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
public class PersonDaoImpl implements PersonDao{

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
    public Optional<Person> findOne(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid id: " + id);
        }
        try{
            return Optional.ofNullable(em.createQuery("SELECT e FROM Person e WHERE e.id = :pk", Person.class).setParameter("pk", id).getSingleResult());
        } catch (NoResultException e){
            return Optional.empty();
        }
    }
      
    @Override
    public Optional<Person> findOneByUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Invalid username: " + username);
        }
        try {
            return Optional.ofNullable(em.createQuery("SELECT e FROM Person e WHERE e.username = :username", Person.class).setParameter("username", username).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Person save(Person person) {
        if (person == null) {
            throw new IllegalArgumentException("Invalid entity (Person): " + person);
        }
        //if the password is empty, use the password that is already stored
        if (person.getPassword() == null || person.getPassword().isEmpty()){
            //if the person does not have stopred Entity, it is a new person
            if (person.getId() == null){
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
        Optional<Person> e = findOne(id);
        if (!e.isPresent()) {
            throw new IllegalArgumentException("Person with id " + id + " does not exist.");
        }
        return updateState(e, personState);
    }

    @Override
    public Person updateState(String username, PersonState personState) {
        Optional<Person> e = findOneByUsername(username);
        if (!e.isPresent()) {
            throw new IllegalArgumentException("Person with username " + username + " does not exist.");
        }
        return updateState(e, personState);
    }
    
    private Person updateState(Optional<Person> e, PersonState personState){
        if (e.get().getState().equals(personState)) {
            log.info("Actual and wanted states are the same. State of person " + e.get().getUsername() + " will not be changed.");
            return e.get();
        } else {
        }
        Person ent = e.get();
        ent.setState(personState);
        return ent;
    }
}
