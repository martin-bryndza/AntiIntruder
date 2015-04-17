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

    /**
     * Maximum time in which a person can be DND Unit: miliseconds Default:
     * 2700000 (45 minutes)
     */
    MAX_DND_TIME(PropertyType.LONG, "2700000"),
    /**
     * Minimum time in which a person has to be AVAILABLE Unit: miliseconds
     * Default: 900000 (15 minutes)
     */
    MIN_AVAILABLE_TIME(PropertyType.LONG, "900000"),
    /**
     * Interval of checking state expiration. Unit: miliseconds Default: 10000
     */
    STATE_CHECK_INTERVAL(PropertyType.LONG, "10000"),
    /**
     * Interval of checking people's states expiration. Unit: miliseconds
     * Default: 10000
     */
    PERSON_STATE_CHECK_INTERVAL(PropertyType.LONG, "10000"),
    /**
     * If client app of a person does not ping server within this time and person is not in state AWAY, person state is set to UNKOWN
     * Unit: milliseconds
     * Default: 3600000 (1 hour)
     */
    MAXIMUM_PING_DELAY(PropertyType.LONG, "3600000"),
    /**
     * If client app of a person does not ping server within this time, person state is set to UNKOWN
     * Unit: milliseconds
     * Default: 10800000 (3 hours)
     */
    MAXIMUM_AWAY_PING_DELAY(PropertyType.LONG, "10800000");

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
