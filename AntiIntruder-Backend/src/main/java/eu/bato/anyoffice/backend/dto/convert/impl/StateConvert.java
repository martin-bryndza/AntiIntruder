package eu.bato.anyoffice.backend.dto.convert.impl;

import eu.bato.anyoffice.backend.dao.StateDao;
import eu.bato.anyoffice.backend.model.State;
import eu.bato.anyoffice.serviceapi.dto.StateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Bato
 */
@Component
public class StateConvert {

    @Autowired
    private StateDao stateDao;

    public State fromDtoToEntity(StateDto dto) {
        if (dto == null) {
            return null;
        }
        State s = new State();
        s.setId(dto.getId());
        s.setName(dto.getName());
        s.setMinDuration(dto.getMinDuration());
        s.setMaxDuration(dto.getMaxDuration());
        s.setDefaultSuccessor(stateDao.findOne(dto.getDefaultSuccessorId()));
        return s;
    }

    public StateDto fromEntityToDto(State entity) {
        if (entity == null) {
            return null;
        }
        StateDto dto = new StateDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setMinDuration(entity.getMinDuration());
        dto.setMaxDuration(entity.getMaxDuration());
        dto.setDefaultSuccessorId(entity.getDefaultSuccessor().getId());
        return dto;
    }

}
