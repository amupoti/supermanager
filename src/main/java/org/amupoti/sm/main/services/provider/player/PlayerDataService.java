package org.amupoti.sm.main.services.provider.player;

import org.amupoti.sm.main.repository.entity.PlayerEntity;
import org.amupoti.sm.main.repository.entity.PlayerId;
import org.htmlcleaner.XPatherException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Methods to be implemented by any source providing player data
 * Created by Marcel on 28/09/2015.
 */
public interface PlayerDataService {

    Set<PlayerId> getPlayerIds() throws IOException, XPatherException;

    List<PlayerEntity> getPlayersData(Set<PlayerId> playerIds) throws XPatherException, IOException, URISyntaxException, InterruptedException, ExecutionException;
}
