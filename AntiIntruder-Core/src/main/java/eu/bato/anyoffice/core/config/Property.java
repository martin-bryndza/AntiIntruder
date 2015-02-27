/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.core.config;

/**
 *
 * @author Bato
 */
public enum Property {
    
    DND_TIME(PropertyType.INTEGER, "45"), 
    AVAILABLE_TIME(PropertyType.INTEGER, "15"),
    STOP_TIME_AWAY_DND(PropertyType.BOOLEAN, "false"),
    STOP_TIME_AWAY_AVAILABLE(PropertyType.BOOLEAN, "true"),
    /**
     * Interval of checking state expiration.
     * Unit: seconds
     * Default: 10
     */
    STATE_CHECK_INTERVAL(PropertyType.INTEGER, "10");
    
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
