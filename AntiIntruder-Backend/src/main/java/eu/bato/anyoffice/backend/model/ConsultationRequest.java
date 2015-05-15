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
public class ConsultationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false)
    private Long requesterId;
    @Column(nullable = false)
    private Long targetId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PersonState targetState;
    @Column(nullable = false)
    private Date time;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public PersonState getTargetState() {
        return targetState;
    }

    public void setTargetState(PersonState targetState) {
        this.targetState = targetState;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.id);
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
        final ConsultationRequest other = (ConsultationRequest) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ConsultationRequest{" + "id=" + id + ", requesterId=" + requesterId + ", targetId=" + targetId + ", targetState=" + targetState + ", time=" + time + '}';
    }

}
