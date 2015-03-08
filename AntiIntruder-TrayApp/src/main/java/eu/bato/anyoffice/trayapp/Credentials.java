package eu.bato.anyoffice.trayapp;

/**
 *
 * @author Bato
 */
public class Credentials {
    
    private final String username;
    private final char[] password;

    public Credentials(String username, char[] password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public char[] getPassword() {
        return password;
    }
    
}
