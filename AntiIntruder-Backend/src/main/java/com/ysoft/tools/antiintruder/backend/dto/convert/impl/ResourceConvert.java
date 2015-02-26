/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.backend.dto.convert.impl;

import com.ysoft.tools.antiintruder.backend.dao.StateDao;
import com.ysoft.tools.antiintruder.serviceapi.dto.EntityDto;
import com.ysoft.tools.antiintruder.backend.model.Entity;
import com.ysoft.tools.antiintruder.backend.model.Resource;
import com.ysoft.tools.antiintruder.serviceapi.dto.ResourceDto;
import com.ysoft.tools.antiintruder.serviceapi.service.StateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Bato
 */
@Component
public class ResourceConvert{
    
    @Autowired
    private StateService stateService;
    
    @Autowired
    private StateConvert stateConvert;

    public Resource fromDtoToEntity(ResourceDto dto) {
        if (dto == null){
            return null;
        } 
        Resource e = new Resource();
        e.setId(dto.getId());
        e.setDescription(dto.getDescription());
        e.setDisplayName(dto.getDisplayName());
        e.setState(stateConvert.fromDtoToEntity(stateService.findOne(dto.getStateId())));
        return e;
    }

    public ResourceDto fromEntityToDto (Resource entity) {
        if (entity == null){
            return null;
        }
        ResourceDto dto = new ResourceDto();
        dto.setId(entity.getId());
        dto.setDisplayName(entity.getDisplayName());
        dto.setDescription(entity.getDescription());
        dto.setStateId(entity.getState().getId());
        dto.setLastStateChange(entity.getLastStateChange());
        dto.setNextPossibleStateChange(entity.getNextPossibleStateChange());
        dto.setStateExpiration(entity.getStateExpiration());
        return dto;
    }
    
}
