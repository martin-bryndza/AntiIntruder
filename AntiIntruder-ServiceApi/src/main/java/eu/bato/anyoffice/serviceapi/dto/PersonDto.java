package eu.bato.anyoffice.serviceapi.dto;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author Bato
 */
public class PersonDto extends EntityDto {

    private PersonState state;
    private String username;
    private PersonRole role;
    private Long dndStart;
    private Long dndEnd;
    private Optional<Long> awayStart = Optional.empty();
    private Optional<Long> lastPing = Optional.empty();
    private List<Long> interactionEntitiesIds;
    private String hipChatToken;
    private String hipChatEmail;

    public String getHipChatToken() {
        return hipChatToken;
    }

    public void setHipChatToken(String hipChatToken) {
        this.hipChatToken = hipChatToken;
    }

    public String getHipChatEmail() {
        return hipChatEmail;
    }

    public void setHipChatEmail(String hipChatEmail) {
        this.hipChatEmail = hipChatEmail;
    }

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

    public Long getDndStart() {
        return dndStart;
    }

    public void setDndStart(Long dndStart) {
        this.dndStart = dndStart;
    }

    public Long getDndEnd() {
        return dndEnd;
    }

    public void setDndEnd(Long dndEnd) {
        this.dndEnd = dndEnd;
    }

    public Optional<Long> getAwayStart() {
        return awayStart;
    }

    public void setAwayStart(Optional<Long> awayStart) {
        this.awayStart = awayStart;
    }

    public Optional<Long> getLastPing() {
        return lastPing;
    }

    public void setLastPing(Optional<Long> lastPing) {
        this.lastPing = lastPing;
    }

    /**
     * Returns IDs of all entities, that this person interacts with.
     *
     * @return list of IDs
     */
    public List<Long> getInteractionEntitiesIds() {
        return interactionEntitiesIds;
    }

    public void setInteractionEntitiesIds(List<Long> interactionEntitiesIds) {
        this.interactionEntitiesIds = interactionEntitiesIds;
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
