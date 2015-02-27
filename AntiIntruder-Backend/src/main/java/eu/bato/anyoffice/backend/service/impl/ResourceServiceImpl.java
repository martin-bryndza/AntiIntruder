/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.backend.service.impl;

import eu.bato.anyoffice.serviceapi.dto.EntityDto;
import eu.bato.anyoffice.backend.dao.ResourceDao;
import eu.bato.anyoffice.backend.dto.convert.impl.ResourceConvert;
import eu.bato.anyoffice.backend.model.Entity;
import eu.bato.anyoffice.backend.model.Resource;
import eu.bato.anyoffice.backend.service.common.DataAccessExceptionNonVoidTemplate;
import eu.bato.anyoffice.backend.service.common.DataAccessExceptionVoidTemplate;
import eu.bato.anyoffice.serviceapi.dto.ResourceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import eu.bato.anyoffice.serviceapi.service.ResourceService;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Bato
 */
@Service
@Transactional
public class ResourceServiceImpl implements ResourceService{
    
    final static Logger log = LoggerFactory.getLogger(ResourceServiceImpl.class);
    @Autowired
    private ResourceDao resourceDao;
    @Autowired
    private ResourceConvert resourceConvert;
    
    @Override
    @Transactional(readOnly = false)
    public Long save(ResourceDto dto) {
        return (Long) new DataAccessExceptionNonVoidTemplate(dto) {
            @Override
            public Long doMethod() {
                ResourceDto dto = (ResourceDto) getU();
                if (dto.getStateId() == null){
                    dto.setStateId(1L); // TODO: Replace with default state for entity type
                }
                Resource entity = resourceConvert.fromDtoToEntity((ResourceDto) getU());
                Resource savedEntity = resourceDao.save(entity);
                return savedEntity.getId();
            }
        }.tryMethod();
    }

    @Override
    public ResourceDto findOne(Long id) {
        if (id == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid id in parameter: null");
            log.error("ResourceServiceImpl.get() called on null parameter: Long id", iaex);
            throw iaex;
        }
        return (ResourceDto) new DataAccessExceptionNonVoidTemplate(id) {
            @Override
            public ResourceDto doMethod() {
                Optional<Resource> entity = resourceDao.findOne((Long) getU());
                if (entity.isPresent()){
                    return resourceConvert.fromEntityToDto(entity.get());
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
                    resourceDao.delete((Long) getU());
                }
            }.tryMethod();
        }
    }    

    //TODO: add paging
    @Override
    public List<ResourceDto> findAll() {
        List<Resource> entities = resourceDao.findAll();
        List<ResourceDto> result = new LinkedList<>();
        for (Resource entity : entities) {
            result.add(resourceConvert.fromEntityToDto(entity));
        }
        return result;
    }

    @Override
    public void updateState(Long id, Long stateId) {
        if (id == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Cannot update Resource that"
                    + " doesn't exist.");
            log.error("ID is null", iaex);
            throw iaex;
        } else if (stateId == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Cannot update Resource to state that"
                    + " doesn't exist.");
            log.error("stateId is null", iaex);
            throw iaex;
        } else {
            new DataAccessExceptionVoidTemplate(id, stateId) {
                @Override
                public void doMethod() {
                    resourceDao.updateState((Long) getU(), (Long) getV());
                }
            }.tryMethod();
        }
    }

}
