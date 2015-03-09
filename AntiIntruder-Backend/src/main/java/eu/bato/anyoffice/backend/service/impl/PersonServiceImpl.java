/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.backend.service.impl;

import eu.bato.anyoffice.backend.dao.PersonDao;
import eu.bato.anyoffice.backend.dto.convert.impl.PersonConvert;
import eu.bato.anyoffice.backend.model.Person;
import eu.bato.anyoffice.backend.service.common.DataAccessExceptionNonVoidTemplate;
import eu.bato.anyoffice.backend.service.common.DataAccessExceptionVoidTemplate;
import eu.bato.anyoffice.serviceapi.dto.LoginDetailsDto;
import eu.bato.anyoffice.serviceapi.dto.PersonDto;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import eu.bato.anyoffice.serviceapi.service.PersonService;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Bato
 */
@Service
@Transactional
public class PersonServiceImpl implements PersonService {

    final static Logger log = LoggerFactory.getLogger(PersonServiceImpl.class);
    @Autowired
    private PersonDao personDao;
    @Autowired
    private PersonConvert personConvert;

    @Override
    @Transactional(readOnly = false)
    public Long save(PersonDto dto) {
        return register(dto, null);
    }

    @Override
    public PersonDto findOne(Long id) {
        if (id == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid id in parameter: null");
            log.error("PersonServiceImpl.get() called on null parameter: Long id", iaex);
            throw iaex;
        }
        return (PersonDto) new DataAccessExceptionNonVoidTemplate(id) {
            @Override
            public PersonDto doMethod() {
                Optional<Person> entity = personDao.findOne((Long) getU());
                if (entity.isPresent()) {
                    return PersonConvert.fromEntityToDto(entity.get());
                } else {
                    return null;
                }
            }
        }.tryMethod();
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Cannot remove entity that"
                    + " doesn't exist.");
            log.error("ID is null", iaex);
            throw iaex;
        } else {
            new DataAccessExceptionVoidTemplate(id) {
                @Override
                public void doMethod() {
                    personDao.delete((Long) getU());
                }
            }.tryMethod();
        }
    }

    //TODO: add paging
    @Override
    public List<PersonDto> findAll() {
        List<Person> entities = personDao.findAll();
        List<PersonDto> result = new LinkedList<>();
        for (Person entity : entities) {
            result.add(PersonConvert.fromEntityToDto(entity));
        }
        return result;
    }

    @Override
    public void setPassword(String username, String password) {
        if (username == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid username in parameter: null");
            log.error("PersonServiceImpl.login() called on null parameter: String username or String password", iaex);
            throw iaex;
        }
        if (password == null || password.isEmpty()) {
            log.info("Password stays unchanged.");
            return;
        }
        new DataAccessExceptionNonVoidTemplate(username, password) {
            @Override
            public Object doMethod() {
                Person person = personDao.findOneByUsername((String) getU()).get();
                person.setPassword((String) getV());
                Person savedEntity = personDao.save(person);
                return savedEntity;
            }
        }.tryMethod();
    }

    @Override
    public Long register(PersonDto person, String password) {
        return (Long) new DataAccessExceptionNonVoidTemplate(person, password) {
            @Override
            public Long doMethod() {
                PersonDto dto = (PersonDto) getU();
                Person entity = personConvert.fromDtoToEntity(dto, (String) getV());
                Person savedEntity = personDao.save(entity);
                return savedEntity.getId();
            }
        }.tryMethod();
    }

    @Override
    public Optional<LoginDetailsDto> getLoginDetails(String username) {
        if (username == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid username in parameter: null");
            log.error("PersonServiceImpl.getPassword() called on null parameter: String username", iaex);
            throw iaex;
        }
        return (Optional<LoginDetailsDto>) new DataAccessExceptionNonVoidTemplate(username) {
            @Override
            public Optional<LoginDetailsDto> doMethod() {
                Optional<Person> entity = personDao.findOneByUsername((String) getU());
                if (entity.isPresent()) {
                    return Optional.of(new LoginDetailsDto(entity.get().getPassword(), entity.get().getRole()));
                } else {
                    return Optional.empty();
                }
            }
        }.tryMethod();
    }

    @Override
    public void setState(Long id, PersonState personState) {
        if (id == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Cannot update person that"
                    + " doesn't exist.");
            log.error("ID is null", iaex);
            throw iaex;
        } else if (personState == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Cannot update person to null state.");
            log.error("PersonState is null", iaex);
            throw iaex;
        }
        new DataAccessExceptionVoidTemplate(id, personState) {
            @Override
            public void doMethod() {
                personDao.updateState((Long) getU(), (PersonState) getV());
            }
        }.tryMethod();
    }

    @Override
    public Optional<PersonDto> findOneByUsername(String username) {
        if (username == null || username.isEmpty()) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid username in parameter: " + username);
            log.error("PersonServiceImpl.findOneByUsername() called on null or empty parameter: String username", iaex);
            throw iaex;
        }
        return (Optional<PersonDto>) new DataAccessExceptionNonVoidTemplate(username) {
            @Override
            public Optional<PersonDto> doMethod() {
                Optional<Person> entity = personDao.findOneByUsername((String) getU());
                if (entity.isPresent()) {
                    return Optional.of(PersonConvert.fromEntityToDto(entity.get()));
                } else {
                    return Optional.empty();
                }
            }
        }.tryMethod();
    }

    @Override
    public PersonState getState(Long id) {
        return findOne(id).getState();
    }

    @Override
    public void setState(String username, PersonState personState) {
        if (username == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Username is null.");
            log.error("Username is null", iaex);
            throw iaex;
        } else if (personState == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Cannot update person to null state.");
            log.error("PersonState is null", iaex);
            throw iaex;
        }
        new DataAccessExceptionVoidTemplate(username, personState) {
            @Override
            public void doMethod() {
                personDao.updateState((String) getU(), (PersonState) getV());
            }
        }.tryMethod();
    }

    @Override
    public PersonState getState(String username) {
        return findOneByUsername(username).get().getState();
    }
    
    

}
