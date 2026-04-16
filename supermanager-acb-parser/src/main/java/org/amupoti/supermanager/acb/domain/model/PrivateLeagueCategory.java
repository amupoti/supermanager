package org.amupoti.supermanager.acb.domain.model;

public enum PrivateLeagueCategory {
    ASSISTS("asistencias"), REBOUNDS("rebotes"), THREE_POINTERS("triples"), POINTS("puntos");

    private String pagePath;

    PrivateLeagueCategory(String pagePath) {
        this.pagePath = pagePath;
    }

    public String getPagePath() {
        return pagePath;
    }
}
