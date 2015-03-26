/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.backend.service.impl;

import eu.bato.anyoffice.backend.dao.PersonDao;
import eu.bato.anyoffice.backend.dto.convert.impl.InteractionPersonConvert;
import eu.bato.anyoffice.backend.dto.convert.impl.InteractionResourceConvert;
import eu.bato.anyoffice.backend.dto.convert.impl.PersonConvert;
import eu.bato.anyoffice.backend.model.Entity;
import eu.bato.anyoffice.backend.model.Person;
import eu.bato.anyoffice.backend.model.Resource;
import eu.bato.anyoffice.backend.service.common.DataAccessExceptionNonVoidTemplate;
import eu.bato.anyoffice.backend.service.common.DataAccessExceptionVoidTemplate;
import eu.bato.anyoffice.serviceapi.dto.InteractionEntityDto;
import eu.bato.anyoffice.serviceapi.dto.InteractionPersonDto;
import eu.bato.anyoffice.serviceapi.dto.LoginDetailsDto;
import eu.bato.anyoffice.serviceapi.dto.PersonDto;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import eu.bato.anyoffice.serviceapi.service.PersonService;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.NoResultException;

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
                Person entity = personDao.findOne((Long) getU());
                return PersonConvert.fromEntityToDto(entity);
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
        entities.stream().forEach((entity) -> {
            result.add(PersonConvert.fromEntityToDto(entity));
        });
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
                Person person = personDao.findOneByUsername((String) getU());
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
                if (entity.getState()==null){
                    entity.setState(PersonState.UNKNOWN);
                }
                if (entity.getState().equals(PersonState.UNKNOWN) || entity.getState().equals(PersonState.AWAY)){
                    entity.setAwayStart(Optional.of(new Date()));
                }
                Person savedEntity = personDao.save(entity);
                return savedEntity.getId();
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
    public PersonDto findOneByUsername(String username) {
        if (username == null || username.isEmpty()) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid username in parameter: " + username);
            log.error("PersonServiceImpl.findOneByUsername() called on null or empty parameter: String username", iaex);
            throw iaex;
        }
        return (PersonDto) new DataAccessExceptionNonVoidTemplate(username) {
            @Override
            public PersonDto doMethod() {
                Person entity = personDao.findOneByUsername((String) getU());
                return PersonConvert.fromEntityToDto(entity);
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
        return findOneByUsername(username).getState();
    }

    @Override
    public void setTimers(String username, Optional<Date> dndStart, Optional<Date> dndEnd, Optional<Date> awayStart) {
        personDao.updateTimers(username, dndStart, dndEnd, awayStart);
    }

    @Override
    public String getUsername(Long id) {
        return findOne(id).getUsername();
    }

    @Override
    public List<String> findAllUsernames() {
        return findAll().stream().map(p -> p.getUsername()).collect(Collectors.toList());
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
                Person entity;
                try{
                    entity = personDao.findOneByUsername((String) getU());
                } catch (IllegalArgumentException | NoResultException e){
                    return Optional.empty();
                }
                return Optional.of(new LoginDetailsDto(entity.getPassword(), entity.getRole()));
            }
        }.tryMethod();
    }

    @Override
    public boolean isPresent(String username) {
        return personDao.isTaken(username);
    }

    @Override
    public void addInteractionEntity(String username, Long id) {
        if (username == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Username is null.");
            log.error("Username is null", iaex);
            throw iaex;
        } else if (id == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Id is null.");
            log.error("Id is null", iaex);
            throw iaex;
        }
        new DataAccessExceptionVoidTemplate(username, id) {
            @Override
            public void doMethod() {
                personDao.addInteractionEntity((String) getU(), (Long) getV());
            }
        }.tryMethod();
    }

    @Override
    public void removeInteractionEntity(String username, Long id) {
        if (username == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Username is null.");
            log.error("Username is null", iaex);
            throw iaex;
        } else if (id == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Id is null.");
            log.error("Id is null", iaex);
            throw iaex;
        }
        new DataAccessExceptionVoidTemplate(username, id) {
            @Override
            public void doMethod() {
                personDao.removeInteractionEntity((String) getU(), (Long) getV());
            }
        }.tryMethod();
    }

    @Override
    public void removeAllInteractionEntities(String username) {
        if (username == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Username is null.");
            log.error("Username is null", iaex);
            throw iaex;
        }
        new DataAccessExceptionVoidTemplate(username) {
            @Override
            public void doMethod() {
                personDao.removeAllInteractionEntities((String) getU());
            }
        }.tryMethod();
    }

    @Override
    public void removeInteractionEntities(String username, Collection<Long> ids) {
        if (username == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Username is null.");
            log.error("Username is null", iaex);
            throw iaex;
        }
        if (ids.isEmpty()){
            return;
        }
        new DataAccessExceptionVoidTemplate(username, ids) {
            @Override
            public void doMethod() {
                personDao.removeInteractionEntities((String) getU(), (Collection<Long>) getV());
            }
        }.tryMethod();
    }
    
    @Override
    public Integer getInteractingPersonsCount(String username) {
        if (username == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Username is null.");
            log.error("Username is null", iaex);
            throw iaex;
        }
        return (Integer) new DataAccessExceptionNonVoidTemplate(username) {
            @Override
            public Integer doMethod() {
                return personDao.getInteractingPersons((String) getU()).size();
            }
        }.tryMethod();
    }

    @Override
    public void removeAllInteractingPersons(String username) {
        if (username == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Username is null.");
            log.error("Username is null", iaex);
            throw iaex;
        }
        new DataAccessExceptionVoidTemplate(username) {
            @Override
            public void doMethod() {
                personDao.removeAllInteractingPersons((String) getU());
            }
        }.tryMethod();
    }

    @Override
    public void setLocation(String username, String location) {
        if (username == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Username is null.");
            log.error("Username is null", iaex);
            throw iaex;
        } else if (location == null) {
            location = "";
        }
        new DataAccessExceptionVoidTemplate(username, location) {
            @Override
            public void doMethod() {
                personDao.setLocation((String) getU(), (String) getV());
            }
        }.tryMethod();
    }

    @Override
    public String getLocation(String username) {
        if (username == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Username is null.");
            log.error("Username is null", iaex);
            throw iaex;
        }
        return (String) new DataAccessExceptionNonVoidTemplate(username) {
            @Override
            public String doMethod() {
                return personDao.getLocation((String) getU());
            }
        }.tryMethod();
    }

    @Override
    public List<InteractionEntityDto> getInteractionEntities(String username) {
        if (username == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Username is null.");
            log.error("Username is null", iaex);
            throw iaex;
        }
        Person person = (Person) new DataAccessExceptionNonVoidTemplate(username) {
            @Override
            public Person doMethod() {
                return personDao.findOneByUsername(username);
            }
        }.tryMethod();
        List<Entity> entities = person.getInteractionEntities();
        List<InteractionEntityDto> result = new LinkedList<>();
        entities.stream().forEach((entity) -> {
            if (Person.class.isInstance(entity)){
                result.add(InteractionPersonConvert.fromEntityToDto((Person) entity));
            } else if (Resource.class.isInstance(entity)){
                result.add(InteractionResourceConvert.fromEntityToDto((Resource) entity));
            } else {
                log.error("Unknown entity type: {}", entity);
            }
        });
        return result;
    }

    @Override
    public List<InteractionPersonDto> getInteractionPersons(String username) {
        if (username == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Username is null.");
            log.error("Username is null", iaex);
            throw iaex;
        }
        List<Person> persons = (List<Person>) new DataAccessExceptionNonVoidTemplate(username) {
            @Override
            public List<Person> doMethod() {
                return personDao.getInteractionPersons(username);
            }
        }.tryMethod();
        List<InteractionPersonDto> result = new LinkedList<>();
        persons.forEach((p) -> result.add(InteractionPersonConvert.fromEntityToDto(p)));
        return result;
    }

    @Override
    public List<InteractionPersonDto> getInteractionPersons(String username, PersonState state) {
        List<InteractionPersonDto> result = getInteractionPersons(username);
        result.removeIf((InteractionPersonDto t) -> {
            return !t.getState().equals(state);
        });
        return result;
    }
    
}
