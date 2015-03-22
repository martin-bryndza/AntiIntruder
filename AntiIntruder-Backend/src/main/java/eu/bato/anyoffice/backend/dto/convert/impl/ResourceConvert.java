/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.backend.dto.convert.impl;

import eu.bato.anyoffice.backend.model.Resource;
import eu.bato.anyoffice.serviceapi.dto.ResourceDto;
import eu.bato.anyoffice.serviceapi.service.StateService;
import java.util.stream.Collectors;
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
        e.setLocation(dto.getLocation());
        //interaction entities are added one after another
        //it is not possible to change lastStateChange from outside Backend module
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
        dto.setLastStateChange(entity.getLastStateChange().getTime());
        dto.setNextPossibleStateChange(entity.getNextPossibleStateChange());
        dto.setStateExpiration(entity.getStateExpiration());
        dto.setLocation(entity.getLocation());
        return dto;
    }
    
}
