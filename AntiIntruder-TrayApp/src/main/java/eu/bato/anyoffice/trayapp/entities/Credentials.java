package eu.bato.anyoffice.trayapp.entities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Bato
 */
public class Credentials {

    private final String encodedAuthString;

    public Credentials(String username, char[] password) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(username.getBytes(Charset.forName("US-ASCII")));
        outputStream.write(':');
        for (int i = 0; i < password.length; i++) {
            outputStream.write(password[i]);
        }
        byte c[] = outputStream.toByteArray();
        this.encodedAuthString = DatatypeConverter.printBase64Binary(c);
    }

    public Credentials(String encodedAuthString) {
        this.encodedAuthString = encodedAuthString;
    }

    public String getEncodedAuthenticationString() {
        return encodedAuthString;
    }

}
