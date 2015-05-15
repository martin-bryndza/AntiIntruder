package eu.bato.anyoffice.trayapp.entities;

/**
 *
 * @author bryndza
 */
public enum PersonLocation {

    MY_PLACE("My place"),
    MEETING("Meeting room"),
    RELAX_BRNO("Relax room Brno"),
    HOME("Home"),
    PRAGUE("Prague"),
    RELAX_PRAGUE("Relax room Prague"),
    BRNO("Brno"),
    RND_OPEN_SPACE("R&D Open Space"),
    RND("R&D"),
    RND_SMALL_OFFICE("R&D Small Office"),
    ROBOT("Robot room"),
    CSS("CSS office"),
    OTHER("Other");

    private String name;

    private PersonLocation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
