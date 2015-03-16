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
     * Interval of checking current state on server.
     * Unit: milisecond
     * Default: 10000
     */
    CHECK_INTERVAL(PropertyType.LONG, "10000"),
    REST_SERVER_ADDRESS(PropertyType.STRING, "http://localhost:8080"),
    CURRENT_USER(PropertyType.STRING, ""),
    CURRENT_PASSWORD(PropertyType.STRING,"");
    
    private final PropertyType type;
    private final String defaultValue;

    private Property(PropertyType type, String defaultValue){
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
