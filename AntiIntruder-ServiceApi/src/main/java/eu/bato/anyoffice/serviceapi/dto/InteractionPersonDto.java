package eu.bato.anyoffice.serviceapi.dto;

/**
 * Represents a person that can be interacted with by a person.
 *
 * @author Bato
 */
public class InteractionPersonDto extends InteractionEntityDto {

    private PersonState state;
    private String username;
    private Long dndStart;

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
