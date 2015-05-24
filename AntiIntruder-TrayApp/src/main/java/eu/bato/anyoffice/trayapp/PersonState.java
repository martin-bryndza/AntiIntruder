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
package eu.bato.anyoffice.trayapp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import java.awt.Image;
import java.awt.Toolkit;

/**
 *
 * @author Bato
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum PersonState {

    DO_NOT_DISTURB("Do Not Disturb", "dnd.png", false), AVAILABLE("Available", "available.png", false), UNKNOWN("Unknown", "unknown.png", true), AWAY("Away", "unknown.png", true);

    private static final String PREPOSITION = "Any Office - ";
    private static final String IMAGE_FOLDER = "images/";
    private final String displayName;
    private final String icon;
    private final boolean awayState;

    private PersonState(String name, String icon, boolean awayState) {
        this.displayName = name;
        this.icon = icon;
        this.awayState = awayState;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonValue
    public String getName() {
        return name();
    }

    public String getDescription() {
        return PREPOSITION + displayName;
    }

    public Image getIcon() {
        return Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource(IMAGE_FOLDER + icon));
    }

    public boolean isAwayState() {
        return awayState;
    }

}
