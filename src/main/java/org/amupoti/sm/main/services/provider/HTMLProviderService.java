package org.amupoti.sm.main.services.provider;

import org.amupoti.sm.main.repository.entity.PlayerId;
import org.amupoti.sm.main.services.PlayerPosition;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by Marcel on 05/08/2015.
 */
public class HTMLProviderService {

    @Value("${url.player}")
    private String PLAYER_URL;
    @Value("${url.players.base}")
    private  String ALL_PLAYERS_BASE;
    @Value("${url.players.alero}")
    private  String ALL_PLAYERS_ALERO;
    @Value("${url.players.pivot}")
    private  String ALL_PLAYERS_PIVOT;
    @Value("${url.team}")
    public  String TEAM_PAGE;


    public String getPlayerURL(PlayerId playerId) throws IOException, URISyntaxException {
        String url = (PLAYER_URL + playerId.getId())
                .replaceAll(" ", "%20");
        return IOUtils.toString((new URL(url)).openStream());

    }

    public String getAllPlayersURL(PlayerPosition playerPosition) throws IOException {

        String url = null;
        switch (playerPosition){
            case BASE:
                url=ALL_PLAYERS_BASE;
                break;
            case ALERO:
                url=ALL_PLAYERS_ALERO;
                break;
            case PIVOT:
                url=ALL_PLAYERS_PIVOT;
                break;


        }
        return get(url);
    }


    public String getTeamURLBody(String teamName) throws IOException {

        return get(TEAM_PAGE+teamName);

    }

    public String get(String getUrl) throws IOException {
        URL url = new URL(getUrl);
        return IOUtils.toString((url).openStream());

    }
}
