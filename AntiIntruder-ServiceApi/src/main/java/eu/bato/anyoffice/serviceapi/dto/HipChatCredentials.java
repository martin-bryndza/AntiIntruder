package eu.bato.anyoffice.serviceapi.dto;

import java.util.Optional;

/**
 *
 * @author bryndza
 */
public class HipChatCredentials {

    private String email;
    private String token;

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Optional<String> getToken() {
        return Optional.ofNullable(token);
    }

    public void setToken(String token) {
        this.token = token;
    }

}
