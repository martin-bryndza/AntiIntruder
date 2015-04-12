/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.trayapp.config;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Bato
 */
public class Configuration {

    private static Properties props;
    final static Logger log = LoggerFactory.getLogger(Configuration.class);
    private static Configuration instance = null;

    private Configuration() {
        File f = new File("conf/client.properties");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            props = new Properties();
            props.load(fis);
        } catch (IOException e) {
            log.error("Unable to load configuration. Default values will be used.", e);
        }
        try {
            if (fis != null) {
                fis.close();
            }
        } catch (IOException ex) {
            log.warn("Unable to close FileInputStream.", ex);
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
        }
        String result = props.getProperty(p.name(), p.getDefaultValue());
        try {
            return Boolean.getBoolean(result);
        } catch (NumberFormatException e) {
            log.warn("The value of the property " + p.name() + " is not of type Integer. Current value: " + result + ". Using default: " + p.getDefaultValue());
            return Boolean.getBoolean(p.getDefaultValue());
        }
    }

    public void setProperty(Property p, String value) {
        props.setProperty(p.name(), value);
        saveConfig();
    }

    private void saveConfig() {
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(new File("conf/client.properties")));
            props.store(os, "");
            log.debug("Saved new configuration.");
        } catch (IOException e) {
            log.error("Unable to save new configuration.", e);
        }
        try {
            if (os != null) {
                os.close();
            }
        } catch (IOException ex) {
            log.warn("Unable to close OutputStream.", ex);
        }
    }

}
