package eu.bato.anyoffice.trayapp.entities;

import eu.bato.anyoffice.trayapp.PersonState;

/**
 *
 * @author Bato
 */
public class InteractionPerson {

    Long id;
    String displayName;
    String location;
    private PersonState state;
    private String username;
    private Long dndStart;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public Long getDndStart() {
        return dndStart;
    }

    public void setDndStart(Long dndStart) {
        this.dndStart = dndStart;
    }

}
