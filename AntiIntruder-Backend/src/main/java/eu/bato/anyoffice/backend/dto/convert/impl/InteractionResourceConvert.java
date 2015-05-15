package eu.bato.anyoffice.backend.dto.convert.impl;

import eu.bato.anyoffice.backend.model.Resource;
import eu.bato.anyoffice.serviceapi.dto.InteractionResourceDto;
import org.springframework.stereotype.Component;

/**
 *
 * @author Bato
 */
@Component
public class InteractionResourceConvert {

    public static InteractionResourceDto fromEntityToDto(Resource entity) {
        if (entity == null) {
            return null;
        }
        InteractionResourceDto dto = new InteractionResourceDto();
        dto.setId(entity.getId());
        dto.setDisplayName(entity.getDisplayName());
        dto.setStateId(entity.getState().getId());
        dto.setStateExpiration(entity.getStateExpiration());
        dto.setLocation(entity.getLocation());
        return dto;
    }

}
