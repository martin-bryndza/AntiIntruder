package eu.bato.anyoffice.backend.dto.convert.impl;

import eu.bato.anyoffice.backend.model.Disturbance;
import eu.bato.anyoffice.serviceapi.dto.DisturbanceDto;
import org.springframework.stereotype.Component;

/**
 *
 * @author bryndza
 */
@Component
public class DisturbanceConvert {

    public static Disturbance fromDtoToEntity(DisturbanceDto dto) {
        if (dto == null) {
            return null;
        }
        Disturbance e = new Disturbance();
        e.setPersonId(dto.getPersonId());
        e.setState(dto.getState());
        e.setTime(dto.getTime());
        e.setAoUser(dto.isAoUser());
        return e;
    }

    public static DisturbanceDto fromEntityToDto(Disturbance entity) {
        if (entity == null) {
            return null;
        }
        DisturbanceDto dto = new DisturbanceDto();
        dto.setPersonId(entity.getPersonId());
        dto.setState(entity.getState());
        dto.setTime(entity.getTime());
        dto.setAoUser(entity.isAoUser());
        return dto;
    }

}
