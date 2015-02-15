/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.backend.service.impl;

import com.ysoft.tools.antiintruder.serviceapi.dto.EntityDto;
import com.ysoft.tools.antiintruder.backend.dao.EntityDao;
import com.ysoft.tools.antiintruder.backend.dao.PersonDao;
import com.ysoft.tools.antiintruder.backend.dto.convert.impl.EntityConvert;
import com.ysoft.tools.antiintruder.backend.dto.convert.impl.PersonConvert;
import com.ysoft.tools.antiintruder.backend.model.Entity;
import com.ysoft.tools.antiintruder.backend.model.Person;
import com.ysoft.tools.antiintruder.backend.service.common.DataAccessExceptionNonVoidTemplate;
import com.ysoft.tools.antiintruder.backend.service.common.DataAccessExceptionVoidTemplate;
import com.ysoft.tools.antiintruder.serviceapi.dto.PersonDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ysoft.tools.antiintruder.serviceapi.service.PersonService;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Bato
 */
@Service
@Transactional
public class PersonServiceImpl implements PersonService{
    
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
    public PersonDto findOne(Long entityId) {
        if (entityId == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid id in parameter: null");
            log.error("PersonServiceImpl.get() called on null parameter: Long id", iaex);
            throw iaex;
        }
        return (PersonDto) new DataAccessExceptionNonVoidTemplate(entityId) {
            @Override
            public PersonDto doMethod() {
                Optional<Person> entity = personDao.findOne((Long) getU());
                if (entity.isPresent()){
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
    public PersonDto login(String username, String password) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        new DataAccessExceptionNonVoidTemplate(username, hash(username, password)) {
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
                Person entity = personConvert.fromDtoToEntity(dto, hash(dto.getUsername(),(String) getV()));
                Person savedEntity = personDao.save(entity);
                return savedEntity.getEntity().getId();
            }
        }.tryMethod();
    }
    
    private String hash(String username, String password) {
        String string = password + "{" + username + "}";
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            log.error(ex.getMessage());
            return string;
        }
        md.update(string.getBytes());

        byte byteData[] = md.digest();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        log.debug("Produced hash: " + sb.toString());
        return sb.toString();
    }

}
