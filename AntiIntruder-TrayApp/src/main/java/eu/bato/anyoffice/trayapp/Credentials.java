package eu.bato.anyoffice.trayapp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;

/**
 *
 * @author Bato
 */
public class Credentials {
    
    private final String username;
    private final String encodedAuthString;

    public Credentials(String username, char[] password) throws IOException {
        this.username = username;
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(username.getBytes(Charset.forName("US-ASCII")));
        outputStream.write(':');
        for (int i = 0; i < password.length; i++) {
            outputStream.write(password[i]);
        }
        byte c[] = outputStream.toByteArray();
        this.encodedAuthString = Base64.getEncoder().encodeToString(c);
    }

    public String getUsername() {
        return username;
    }
    
    public String getEncodedAuthenticationString(){
        return encodedAuthString;
    }
    
}
