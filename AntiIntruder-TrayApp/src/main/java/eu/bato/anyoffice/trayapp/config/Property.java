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

    CHECK_INTERVAL(PropertyType.LONG, "10000"),
    SERVER_ADDRESS(PropertyType.STRING, "http://localhost:8080"),
    WEB_ADDRESS(PropertyType.STRING, "http://localhost:8080"),
    CURRENT_USER(PropertyType.STRING, ""),
    GUID(PropertyType.STRING, ""),
    RUN_AT_STARTUP(PropertyType.BOOLEAN, "false"),
    POPUPS_ENABLED(PropertyType.BOOLEAN, "true"),
    FIRST_RUN(PropertyType.BOOLEAN, "true"),
    STATE_AUTO_SWITCH(PropertyType.BOOLEAN, "false"),
    DND_DEFAULT_PERIOD(PropertyType.LONG, "2700000"),
    DND_LAST_PERIOD(PropertyType.LONG, "2700000");

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
