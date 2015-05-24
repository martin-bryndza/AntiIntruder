/* 
 * Copyright (c) 2015, Martin Bryndza
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package eu.bato.anyoffice.backend.service.impl;

import eu.bato.anyoffice.backend.dao.ResourceDao;
import eu.bato.anyoffice.backend.dto.convert.impl.ResourceConvert;
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

/**
 *
 * @author Bato
 */
@Service
@Transactional
public class ResourceServiceImpl implements ResourceService {

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
                if (dto.getStateId() == null) {
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
                Resource entity = resourceDao.findOne((Long) getU());
                return resourceConvert.fromEntityToDto(entity);
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
        entities.stream().forEach((entity) -> {
            result.add(resourceConvert.fromEntityToDto(entity));
        });
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
