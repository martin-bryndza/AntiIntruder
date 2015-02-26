/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.backend.model;

import com.ysoft.tools.antiintruder.serviceapi.dto.PersonRole;
import com.ysoft.tools.antiintruder.serviceapi.dto.PersonState;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

/**
 *
 * @author Bato
 */
@javax.persistence.Entity
@Table(name = "Person")
public class Person extends Entity{
    
    @Column(columnDefinition = "VARCHAR(100)", nullable = false, unique = true)
    private String username;
    @Column(columnDefinition = "VARCHAR(250)", nullable = false)
    private String password;
    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private PersonState state = PersonState.AVAILABLE;
    @Enumerated(EnumType.STRING)
    private PersonRole role;

    public PersonRole getRole() {
        return role;
    }

    public void setRole(PersonRole role) {
        this.role = role;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public PersonState getState() {
        return state;
    }

    public void setState(PersonState state) {
        final Long currentMillis = Calendar.getInstance().getTimeInMillis();
        super.setLastStateChange(new Date(currentMillis));
        this.state = state;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.username);
        hash = 47 * hash + Objects.hashCode(this.password);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Person other = (Person) obj;
        if (!Objects.equals(this.username, other.username)) {
            return false;
        }
        if (!Objects.equals(this.password, other.password)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Person{username=" + username + ", password=" + password + ", role=" + role + '}';
    }

}
