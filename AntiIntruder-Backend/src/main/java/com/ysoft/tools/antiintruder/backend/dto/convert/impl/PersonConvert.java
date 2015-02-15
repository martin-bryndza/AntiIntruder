package com.ysoft.tools.antiintruder.backend.dto.convert.impl;

import com.ysoft.tools.antiintruder.backend.dao.EntityDao;
import com.ysoft.tools.antiintruder.backend.model.Entity;
import com.ysoft.tools.antiintruder.backend.model.Person;
import com.ysoft.tools.antiintruder.serviceapi.dto.EntityDto;
import com.ysoft.tools.antiintruder.serviceapi.dto.PersonDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Bato
 */
@Component
public class PersonConvert{
    
    @Autowired
    private EntityConvert entityConvert;

    public Person fromDtoToEntity(PersonDto dto, String password) {
        Person e = new Person();
        EntityDto entDto = new EntityDto();
        entDto.setDescription(dto.getDescription());
        entDto.setDisplayName(dto.getDisplayName());
        entDto.setId(dto.getEntityId());
        entDto.setStateId(dto.getStateId());
        e.setEntity(entityConvert.fromDtoToEntity(entDto));
        e.setUsername(dto.getUsername());
        e.setRole(dto.getRole());
        e.setPassword(password); // the check for emptiness is done in DAO
        return e;
    }

    public static PersonDto fromEntityToDto (Person entity) {
        PersonDto dto = new PersonDto();
        dto.setEntityId(entity.getEntity().getId());
        dto.setUsername(entity.getUsername());
        dto.setRole(entity.getRole());
        dto.setStateId(entity.getEntity().getState().getId());
        dto.setDisplayName(entity.getEntity().getDisplayName());
        dto.setDescription(entity.getEntity().getDescription());
        return dto;
    }
    
}
