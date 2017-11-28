package org.amupoti.sm.rdm.parser.provider;

import org.amupoti.sm.rdm.parser.repository.entity.PlayerId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by Marcel on 05/08/2015.
 */
public class HTMLProviderServiceV2 {

    @Value("${rdm.new.url.player}")
    private String PLAYER_URL;
    @Value("${rdm.new.url.allPlayers}")
    private String ALL_PLAYERS;
    @Value("${rdm.new.url.team}")
    public String TEAM_PAGE;

    @Autowired
    private RestTemplate restTemplate;

    public String getPlayerURL(PlayerId playerId) throws IOException, URISyntaxException {
        String url = (PLAYER_URL + playerId.getId());
        return get(url);
    }

    public String getAllPlayersURL() throws IOException {

        return get(ALL_PLAYERS);
    }

    public String getTeamURLBody(String teamName) throws IOException {

        return get(TEAM_PAGE + teamName);

    }

    public String get(String getUrl) throws IOException {
        URL url = new URL(getUrl);
        //return IOUtils.toString((url).openStream());
        return restTemplate.getForObject(getUrl, String.class);

    }
}
