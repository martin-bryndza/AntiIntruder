/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.trayapp.config;

/**
 *
 * @author Bato
 */
public enum Property {

    /**
     * Interval of checking current state on server. Unit: milisecond Default:
     * 10000
     */
    CHECK_INTERVAL(PropertyType.LONG, "10000"),
    SERVER_ADDRESS(PropertyType.STRING, "http://localhost:8080"),
    WEB_ADDRESS(PropertyType.STRING, "http://localhost:8080"),
    CURRENT_USER(PropertyType.STRING, ""),
    GUID(PropertyType.STRING, ""),
    RUN_AT_STARTUP(PropertyType.BOOLEAN, "false"),
    POPUPS_ENABLED(PropertyType.BOOLEAN, "true");

    private final PropertyType type;
    private final String defaultValue;

    private Property(PropertyType type, String defaultValue) {
        this.type = type;
        this.defaultValue = defaultValue;
    }

    PropertyType getType() {
        return type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

}
