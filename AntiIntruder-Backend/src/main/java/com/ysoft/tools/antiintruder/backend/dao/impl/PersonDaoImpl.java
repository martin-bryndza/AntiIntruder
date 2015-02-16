package com.ysoft.tools.antiintruder.backend.dao.impl;

import com.ysoft.tools.antiintruder.backend.dao.EntityDao;
import com.ysoft.tools.antiintruder.backend.dao.PersonDao;
import com.ysoft.tools.antiintruder.backend.model.Entity;
import com.ysoft.tools.antiintruder.backend.model.Person;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Bato
 */
@Repository
public class PersonDaoImpl implements PersonDao{

    final static Logger log = LoggerFactory.getLogger(PersonDaoImpl.class);

    @Autowired
    private EntityDao entityDao;
    
    @PersistenceContext(name = "entityManagerFactory")
    private EntityManager em;
    
    @Override
    public void delete(Entity id) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid record: null or with no id.");
        }
        Person person = em.find(Person.class, id.getId());
        if (person == null) {
            log.error("Person with corresponding entity with id " + id.getId() + " is not in DB");
        }
        em.remove(person);
        entityDao.delete(id.getId());
    }
    
    @Override
    public void delete(Long entityId) {
        if (entityId == null) {
            throw new IllegalArgumentException("Invalid record: null or with no id.");
        }
        Person person = em.find(Person.class, entityId);
        if (person == null) {
            log.error("Person with corresponding entity with id " + entityId + " is not in DB");
        }
        em.remove(person);
        entityDao.delete(entityId);
    }

    @Override
    public List<Person> findAll() {    
        return em.createQuery("SELECT tbl FROM Person tbl", Person.class).getResultList();
    }

    @Override
    public Optional<Person> findOne(Entity id) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid id: " + id);
        }
        try{
            return Optional.ofNullable(em.createQuery("SELECT e FROM Person e WHERE e.entity_id = :pk", Person.class).setParameter("pk", id.getId()).getSingleResult());
        } catch (NoResultException e){
            throw new IllegalArgumentException("Invalid id: nonexistent", e);
        }
    }
    
    @Override
    public Optional<Person> findOne(Long entityId) {
        if (entityId == null) {
            throw new IllegalArgumentException("Invalid id: " + entityId);
        }
        try {
            return Optional.ofNullable(em.createQuery("SELECT e FROM Person e WHERE e.entity_id = :pk", Person.class).setParameter("pk", entityId).getSingleResult());
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Invalid id: nonexistent", e);
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
            throw new IllegalArgumentException("Invalid username: nonexistent", e);
        }
    }

    @Override
    public Person save(Person person) {
        if (person == null) {
            throw new IllegalArgumentException("Invalid entity (Person): " + person);
        }
        if (person.getPassword() == null || person.getPassword().isEmpty()){
            String currentPass;
            try {
                currentPass = em.createQuery("SELECT tbl.password FROM Person tbl WHERE tbl.entity_id = "
                        + ":givenId", String.class).setParameter("givenId", person.getEntity().getId()).getSingleResult();
            } catch (NoResultException e) {
                throw new IllegalArgumentException("Unable to create a new person with empty password.");
            }
            person.setPassword(currentPass);
        }
        Entity modelReferencedEntity = entityDao.save(person.getEntity());
        System.out.println(modelReferencedEntity);
        person.setEntity(modelReferencedEntity);
        log.info("Creating " + person.toString());
        Person modelPerson = em.merge(person);
        // modelPerson.entity is null again
        modelPerson.setEntity(modelReferencedEntity); //TODO: Why do I have to do this?
        log.info("Created " + modelPerson.toString() + ".");
        log.info(" Assigned entity id: " + modelReferencedEntity.getId());
        return modelPerson;
    }
    
    @Override
    public Person login(String username, String password) {
        if (username == null || password == null) {
            throw new IllegalArgumentException("Invalid username or password: null");
        }
        TypedQuery<Person> query;
        Person returnedUser;
        query = em.createQuery("SELECT tbl FROM Person tbl "
                + " WHERE tbl.username = :uname and tbl.password = :pword", Person.class);
        query.setParameter("uname", username);
        query.setParameter("pword", password);
        try {
            returnedUser = query.getSingleResult();
        } catch (NoResultException ex) {
            log.debug("Login failed for user " + username + ". Ivalid credentials or user does not exist.");
            return null;
        }
        return returnedUser;
    }
    
}
