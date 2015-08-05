package org.amupoti.sm.main.services;

import org.amupoti.sm.main.services.bean.PlayerId;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by Marcel on 05/08/2015.
 */
@Service
public class HTMLProviderService {

    private final String PLAYER_URL = "http://www.rincondelmanager.com/smgr/stats.php?nombre=";
    private final String ALL_PLAYERS = "http://www.rincondelmanager.com/smgr/valoracion.php?pos=135";

    public String getPlayerURL(PlayerId playerId) throws IOException, URISyntaxException {
        String url = (PLAYER_URL + playerId.getId())
                .replaceAll(" ", "%20");
        return IOUtils.toString((new URL(url)).openStream());

    }

    public String getAllPlayersURL() throws IOException {

        URL url = new URL(ALL_PLAYERS);
        return IOUtils.toString((url).openStream());
    }
}
