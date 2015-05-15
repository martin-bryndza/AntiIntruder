package eu.bato.anyoffice.serviceapi.dto;

/**
 *
 * @author Bato
 */
public class LoginDetailsDto {

    private final String password;
    private final PersonRole role;

    public LoginDetailsDto(String password, PersonRole role) {
        this.password = password;
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public PersonRole getRole() {
        return role;
    }

}
