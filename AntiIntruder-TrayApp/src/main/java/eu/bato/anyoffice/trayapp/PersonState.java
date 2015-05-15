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
