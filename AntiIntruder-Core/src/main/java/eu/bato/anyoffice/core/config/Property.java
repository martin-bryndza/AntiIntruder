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
     * If client app of a person does not ping server within this time and
     * person is not in state AWAY, person state is set to UNKOWN Unit:
     * milliseconds Default: 3600000 (1 hour)
     */
    MAXIMUM_PING_DELAY(PropertyType.LONG, "3600000"),
    /**
     * If client app of a person does not ping server within this time, person
     * state is set to UNKOWN Unit: milliseconds Default: 10800000 (3 hours)
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
