package eu.bato.anyoffice.serviceapi.dto;

import java.util.Date;

/**
 *
 * @author bryndza
 */
public class PersonStateSwitchDto {
    
    private Long personId;
    private PersonState state;
    private Date time;

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public PersonState getState() {
        return state;
    }

    public void setState(PersonState state) {
        this.state = state;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
    
}
