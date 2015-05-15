package eu.bato.anyoffice.serviceapi.dto;

/**
 * Represents an entity that can be interacted with by a person.
 *
 * @author Bato
 */
public abstract class InteractionEntityDto {

    Long id;
    String displayName;
    String location;

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

}
