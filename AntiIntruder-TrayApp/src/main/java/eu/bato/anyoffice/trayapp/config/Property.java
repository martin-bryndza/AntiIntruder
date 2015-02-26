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
     * Unit: seconds
     * Default: 10
     */
    CHECK_INTERVAL(PropertyType.INTEGER, "10");
    
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
