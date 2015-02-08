/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.backend.model;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 *
 * @author Bato
 */
@Entity
@Table(name = "entities")
public class Entitty implements Serializable{
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(columnDefinition = "VARCHAR(50)", nullable = false)
    private String username;
    @Column(columnDefinition = "VARCHAR(50)", nullable = false)
    private String displayName;
//    @Enumerated(EnumType.STRING)
//    private State state;

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

//    public State getState() {
//        return state;
//    }

    public Long getId() {
        return id;
    }

//    public void setState(State state) {
//        this.state = state;
//    }
    
    @Version
    private long version;

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.id);
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
        final Entitty other = (Entitty) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entitty{" + "id=" + id + ", username=" + username + ", displayName=" + displayName + ", state=" 
//                + state 
                + '}';
    }
    
    
    
}
