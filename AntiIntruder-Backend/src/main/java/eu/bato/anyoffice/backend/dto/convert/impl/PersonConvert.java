package eu.bato.anyoffice.backend.dto.convert.impl;

import eu.bato.anyoffice.backend.model.Person;
import eu.bato.anyoffice.serviceapi.dto.PersonDto;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 *
 * @author Bato
 */
@Component
public class PersonConvert {

    public Person fromDtoToEntity(PersonDto dto, String password) {
        if (dto == null) {
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
        e.setLocation(dto.getLocation());
        //awayStart, dndStart and dndEnd are set by separate method in PersonService
        //interaction entities are added one after another
        //it is not possible to change lastStateChange from outside Backend module
        return e;
    }

    public static PersonDto fromEntityToDto(Person entity) {
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
        dto.setLastStateChange(entity.getLastStateChange().getTime());
        dto.setLocation(entity.getLocation());
        dto.setAwayStart(entity.getAwayStart().isPresent() ? Optional.of(entity.getAwayStart().get().getTime()) : Optional.empty());
        dto.setDndEnd(entity.getDndEnd().getTime());
        dto.setDndStart(entity.getDndStart().getTime());
        dto.setInteractionEntitiesIds(entity.getInteractionEntities().stream().map(p -> p.getId()).collect(Collectors.toList()));
        return dto;
    }

}
