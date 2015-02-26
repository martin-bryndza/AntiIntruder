package com.ysoft.tools.antiintruder.backend.dto.convert.impl;

import com.ysoft.tools.antiintruder.backend.model.Person;
import com.ysoft.tools.antiintruder.serviceapi.dto.PersonDto;
import org.springframework.stereotype.Component;

/**
 *
 * @author Bato
 */
@Component
public class PersonConvert{
    
    public Person fromDtoToEntity(PersonDto dto, String password) {
        if (dto == null){
            return null;
        }
        Person e = new Person();
        e.setDescription(dto.getDescription());
        e.setDisplayName(dto.getDisplayName());
        e.setId(dto.getId());
        e.setState(dto.getState());
        e.setUsername(dto.getUsername());
        e.setRole(dto.getRole());
        e.setPassword(password); // the check for emptiness is done in DAO
        return e;
    }

    public static PersonDto fromEntityToDto (Person entity) {
        if (entity == null) {
            return null;
        }
        PersonDto dto = new PersonDto();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setRole(entity.getRole());
        dto.setState(entity.getState());
        dto.setDisplayName(entity.getDisplayName());
        dto.setDescription(entity.getDescription());
        dto.setLastStateChange(entity.getLastStateChange());
        return dto;
    }
    
}
