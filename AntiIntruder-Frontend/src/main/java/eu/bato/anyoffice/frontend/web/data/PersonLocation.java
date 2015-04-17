/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.bato.anyoffice.frontend.web.data;

/**
 *
 * @author bryndza
 */
public enum PersonLocation {
    
    MY_PLACE("My place"),
    RELAX_BRNO("Relax room Brno"),
    RELAX_PRAGUE("Relax room Prague"),
    HOME("Home"),
    MEETING("Meeting room"),
    PRAGUE("Prague"),
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
