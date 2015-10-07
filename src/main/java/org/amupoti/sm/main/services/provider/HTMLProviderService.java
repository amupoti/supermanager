package org.amupoti.sm.main.services.provider;

import org.amupoti.sm.main.repository.entity.PlayerId;
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
    @Value("${url.players}")
    private  String ALL_PLAYERS;
    @Value("${url.team}")
    public  String TEAM_PAGE;


    public String getPlayerURL(PlayerId playerId) throws IOException, URISyntaxException {
        String url = (PLAYER_URL + playerId.getId())
                .replaceAll(" ", "%20");
        return IOUtils.toString((new URL(url)).openStream());

    }

    public String getAllPlayersURL() throws IOException {

        return get(ALL_PLAYERS);
    }


    public String getTeamURLBody(String teamName) throws IOException {

        return get(TEAM_PAGE+teamName);

    }

    public String get(String getUrl) throws IOException {
        URL url = new URL(getUrl);
        return IOUtils.toString((url).openStream());

    }
}
