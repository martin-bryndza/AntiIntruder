package eu.bato.anyoffice.backend.model;

import eu.bato.anyoffice.serviceapi.dto.PersonState;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 *
 * @author bryndza
 */
@javax.persistence.Entity
public class Disturbance {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false)
    private Long personId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PersonState state;
    @Column(nullable = false)
    private Date time;
    @Column(nullable = true, name = "AO_USER")
    private Boolean aoUser;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.id);
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
        final Disturbance other = (Disturbance) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Disturbance{" + "id=" + id + ", personId=" + personId + ", state=" + state + ", time=" + time + ", aoUser=" + aoUser + '}';
    }

}
