/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.trayapp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 *
 * @author Bato
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum PersonState {
        
    DO_NOT_DISTURB("Do not disturb", "dnd.png"), AVAILABLE("Available", "available.png"), UNKNOWN("Unknown", "unknown.png"), AWAY("Away", "unknown.png");
    
    private static final String PREPOSITION = "Any Office - ";
    private static final String IMAGE_FOLDER = "images/";
    private final String displayName;
    private final String icon;

    private PersonState(String name, String icon) {
        this.displayName = name;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    @JsonValue
    public String getName(){
        return name();
    }
    
    public String getDescription() {
        return PREPOSITION + displayName;
    }

    public String getIconPath() {
        return IMAGE_FOLDER + icon;
    }
    
}
