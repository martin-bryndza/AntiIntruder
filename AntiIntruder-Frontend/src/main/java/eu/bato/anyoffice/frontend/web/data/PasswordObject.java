package eu.bato.anyoffice.frontend.web.data;

import org.springframework.security.crypto.password.StandardPasswordEncoder;

/**
 * Helper class for getting password as a model attribute.
 * @author Bato
 */
public class PasswordObject {

    private String value;
    private static final StandardPasswordEncoder encoder = new StandardPasswordEncoder();

    public PasswordObject() {
        this.value = "";
    }

    public String getValue() {
        return value;
    }

    /**
     * <b>Encodes</b> and sets value parameter of this PasswordObject.
     * @param value 
     */
    public void setValue(String value) {
        this.value = encoder.encode(value);
    }

}
