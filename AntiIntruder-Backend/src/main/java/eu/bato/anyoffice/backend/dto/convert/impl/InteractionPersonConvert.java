package eu.bato.anyoffice.backend.dto.convert.impl;

import eu.bato.anyoffice.backend.model.Person;
import eu.bato.anyoffice.serviceapi.dto.InteractionPersonDto;

/**
 *
 * @author Bato
 */
public class InteractionPersonConvert {
    
    public static InteractionPersonDto fromEntityToDto(Person entity) {
        if (entity == null) {
            return null;
        }
        InteractionPersonDto dto = new InteractionPersonDto();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setState(entity.getState());
        dto.setDisplayName(entity.getDisplayName());
        dto.setLocation(entity.getLocation());
        dto.setDndStart(entity.getDndStart().getTime());
        return dto;
    }
    
}
