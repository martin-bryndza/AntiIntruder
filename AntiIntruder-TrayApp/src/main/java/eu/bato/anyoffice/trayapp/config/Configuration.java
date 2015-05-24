/* 
 * Copyright (c) 2015, Martin Bryndza
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package eu.bato.anyoffice.trayapp.config;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
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

    private static final String CONFIG_FILE_NAME = "anyoffice-client.properties";

    private Configuration() {
        //temporary part to convert from old tray app and keep settings
        //old versions of tray app stored properties file in the current folder
        File oldF = new File(CONFIG_FILE_NAME);
        File homedir = new File(System.getProperty("user.home") + "/anyoffice");
        File newF = new File(homedir.getPath() + "/" + CONFIG_FILE_NAME);
        if (!newF.exists()) {
            homedir.mkdirs();
            if (oldF.exists()) {
                try {
                    Files.copy(oldF.toPath(), newF.toPath());
                } catch (IOException ex) {
                    log.error("Unable to copy old settings file. Settings will be reset.");
                }
                try {
                    Files.deleteIfExists(oldF.toPath());
                } catch (IOException ex) {
                    log.warn("Unable to delete old settings file.");
                }
            }
        }
        InputStream isCustom = null;
        if (newF.exists()) {
            try {
                isCustom = new FileInputStream(newF);
            } catch (FileNotFoundException ex) {
                log.warn("Unable to load configuration file. Default will be used.");
            }
        }
        InputStream isDefault;
        isDefault = this.getClass().getClassLoader().getResourceAsStream("conf/client.properties");
        try {
            props = new Properties();
            props.load(isDefault);
            if (isCustom != null) {
                props.load(isCustom);
            }
        } catch (IOException e) {
            log.error("Unable to load configuration. Default values will be used.", e);
        }
        try {
            isDefault.close();
            if (isCustom != null) {
                isCustom.close();
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
        return Boolean.parseBoolean(result);
    }

    public void setProperty(Property p, String value) {
        props.setProperty(p.name(), value);
        saveConfig();
    }

    private void saveConfig() {
        OutputStream os = null;
        try {
            File f = new File(System.getProperty("user.home") + "/anyoffice/" + CONFIG_FILE_NAME);
            if (!f.exists()) {
                f.createNewFile();
            }
            os = new BufferedOutputStream(new FileOutputStream(f));
            Properties customProps = new Properties();
            customProps.setProperty(Property.CURRENT_USER.name(), props.getProperty(Property.CURRENT_USER.name(), ""));
            customProps.setProperty(Property.GUID.name(), props.getProperty(Property.GUID.name(), ""));
            customProps.setProperty(Property.SERVER_ADDRESS.name(), props.getProperty(Property.SERVER_ADDRESS.name(), ""));
            customProps.setProperty(Property.WEB_ADDRESS.name(), props.getProperty(Property.WEB_ADDRESS.name(), ""));
            customProps.setProperty(Property.RUN_AT_STARTUP.name(), props.getProperty(Property.RUN_AT_STARTUP.name(), "false"));
            customProps.setProperty(Property.POPUPS_ENABLED.name(), props.getProperty(Property.POPUPS_ENABLED.name(), "true"));
            customProps.setProperty(Property.STATE_AUTO_SWITCH.name(), props.getProperty(Property.STATE_AUTO_SWITCH.name(), "false"));
            customProps.setProperty(Property.FIRST_RUN.name(), props.getProperty(Property.FIRST_RUN.name(), "false"));
            customProps.setProperty(Property.DND_DEFAULT_PERIOD.name(), props.getProperty(Property.DND_DEFAULT_PERIOD.name(), "2700000"));
            customProps.setProperty(Property.DND_LAST_PERIOD.name(), props.getProperty(Property.DND_LAST_PERIOD.name(), "2700000"));
            customProps.store(os, "Saved at ");
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
