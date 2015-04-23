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
public class OtherPageObject {

    private final String name;
    private final String url;
    private final boolean loadsInFrame;

    public OtherPageObject(String name, String url, boolean loadsInFrame) {
        this.name = name;
        this.url = url;
        this.loadsInFrame = loadsInFrame;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public boolean isLoadsInFrame() {
        return loadsInFrame;
    }

}
