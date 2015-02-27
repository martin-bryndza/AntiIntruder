package eu.bato.anyoffice.serviceapi.dto;

import java.util.Objects;

/**
 *
 * @author Bato
 */
public class PersonDto extends EntityDto{
    
    private PersonState state;
    private String username;
    private PersonRole role;

    public PersonState getState() {
        return state;
    }

    public void setState(PersonState state) {
        this.state = state;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public PersonRole getRole() {
        return role;
    }

    public void setRole(PersonRole role) {
        this.role = role;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + Objects.hashCode(super.getId());
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
        final EntityDto other = (EntityDto) obj;
        return Objects.equals(super.getId(), other.getId());
    }

    @Override
    public String toString() {
        return "PersonDto{" + "state=" + state + ", username=" + username + ", role=" + role + '}';
    } 
    
}
