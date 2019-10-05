package org.amupoti.supermanager.parser.acb.privateleague;

/**
 * Created by amupoti on 30/09/2019.
 */

public enum PrivateLeagueCategory {
    ASSISTS("asistencias"), REBOUNDS("rebotes"), THREE_POINTERS("triples");

    private String pagePath;

    PrivateLeagueCategory(String pagePath) {

        this.pagePath = pagePath;
    }

    public String getPagePath() {
        return pagePath;
    }
}
