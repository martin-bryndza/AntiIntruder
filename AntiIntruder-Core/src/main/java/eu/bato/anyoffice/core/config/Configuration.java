package eu.bato.anyoffice.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Bato
 */
public class Configuration {

    private static Properties props;
    private final static Logger log = LoggerFactory.getLogger(Configuration.class);
    private static Configuration instance = null;

    private Configuration() {
        InputStream is = null;
        try {
            is = this.getClass().getClassLoader().getResourceAsStream("core.properties");
            props = new Properties();
            props.load(is);
        } catch (IOException e) {
            log.error("Unable to load configuration. Default values will be used.", e);
        }
        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException ex) {
            log.warn("Unable to close InputStream.", ex);
        }
    }

    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    public String getProperty(Property p) {
        if (props == null) {
            log.warn("Configuration was not loaded successfuly. Using default value " + p.getDefaultValue() + " for property " + p.name());
            return p.getDefaultValue();
        }
        return props.getProperty(p.name(), p.getDefaultValue());
    }

    public Integer getIntegerProperty(Property p) {
        if (!p.getType().equals(PropertyType.INTEGER)) {
            String msg = "Property " + p.name() + " is not of type Integer. Type is " + p.getType().toString();
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (props == null) {
            log.warn("Configuration was not loaded successfuly. Using default value " + p.getDefaultValue() + " for property " + p.name());
            return Integer.parseInt(p.getDefaultValue());
        }
        String result = props.getProperty(p.name(), p.getDefaultValue());
        try {
            return Integer.parseInt(result);
        } catch (NumberFormatException e) {
            log.warn("The value of the property " + p.name() + " is not of type Integer. Current value: " + result + ". Using default: " + p.getDefaultValue());
            return Integer.parseInt(p.getDefaultValue());
        }
    }

    public Long getLongProperty(Property p) {
        if (!p.getType().equals(PropertyType.LONG)) {
            String msg = "Property " + p.name() + " is not of type Long. Type is " + p.getType().toString();
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (props == null) {
            log.warn("Configuration was not loaded successfuly. Using default value " + p.getDefaultValue() + " for property " + p.name());
            return Long.parseLong(p.getDefaultValue());
        }
        String result = props.getProperty(p.name(), p.getDefaultValue());
        try {
            return Long.parseLong(result);
        } catch (NumberFormatException e) {
            log.warn("The value of the property " + p.name() + " is not of type Long. Current value: " + result + ". Using default: " + p.getDefaultValue());
            return Long.parseLong(p.getDefaultValue());
        }
    }

    public Boolean getBooleanProperty(Property p) {
        if (!p.getType().equals(PropertyType.BOOLEAN)) {
            String msg = "Property " + p.name() + " is not of type Boolean. Type is " + p.getType().toString();
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (props == null) {
            log.warn("Configuration was not loaded successfuly. Using default value " + p.getDefaultValue() + " for property " + p.name());
            return Boolean.getBoolean(p.getDefaultValue());
        }
        String result = props.getProperty(p.name(), p.getDefaultValue());
        try {
            return Boolean.getBoolean(result);
        } catch (NumberFormatException e) {
            log.warn("The value of the property " + p.name() + " is not of type Integer. Current value: " + result + ". Using default: " + p.getDefaultValue());
            return Boolean.getBoolean(p.getDefaultValue());
        }
    }

}
