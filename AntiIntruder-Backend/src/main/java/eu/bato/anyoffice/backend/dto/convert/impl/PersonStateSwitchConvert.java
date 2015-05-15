/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.bato.anyoffice.backend.dto.convert.impl;

import eu.bato.anyoffice.backend.model.PersonStateSwitch;
import eu.bato.anyoffice.serviceapi.dto.PersonStateSwitchDto;
import org.springframework.stereotype.Component;

/**
 *
 * @author bryndza
 */
@Component
public class PersonStateSwitchConvert {
    
    public static PersonStateSwitch fromDtoToEntity(PersonStateSwitchDto dto) {
        if (dto == null) {
            return null;
        }
        PersonStateSwitch e = new PersonStateSwitch();
        e.setPersonId(dto.getPersonId());
        e.setState(dto.getState());
        e.setTime(dto.getTime());
        return e;
    }

    public static PersonStateSwitchDto fromEntityToDto(PersonStateSwitch entity) {
        if (entity == null) {
            return null;
        }
        PersonStateSwitchDto dto = new PersonStateSwitchDto();
        dto.setPersonId(entity.getPersonId());
        dto.setState(entity.getState());
        dto.setTime(entity.getTime());
        return dto;
    }
    
}
