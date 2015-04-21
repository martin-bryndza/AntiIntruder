/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.serviceapi.dto;

import java.util.Date;

/**
 *
 * @author bryndza
 */
public class DisturbanceDto {

    private Long personId;
    private PersonState state;
    private Date time;
    private Boolean aoUser;

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

    public Boolean isAoUser() {
        return aoUser;
    }

    public void setAoUser(Boolean aoUser) {
        this.aoUser = aoUser;
    }

}
