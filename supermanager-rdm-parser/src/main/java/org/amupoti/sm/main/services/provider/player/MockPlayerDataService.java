package org.amupoti.sm.main.services.provider.player;

import org.amupoti.sm.main.config.SMConstants;
import org.amupoti.sm.main.repository.TeamRepository;
import org.amupoti.sm.main.repository.entity.PlayerEntity;
import org.amupoti.sm.main.repository.entity.PlayerId;
import org.amupoti.sm.main.repository.entity.TeamEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.XPatherException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by Marcel on 28/09/2015.
 */
public class MockPlayerDataService implements PlayerDataService{

    private static final int MAX_PLAYERS = 100;
    private Random random = new Random(System.currentTimeMillis());
    private final static Log log = LogFactory.getLog(MockPlayerDataService.class);
    @Autowired
    private TeamRepository teamRepository;


    @Override
    public Set<PlayerId> getPlayerIds() throws IOException, XPatherException {
        Set<PlayerId> set = new LinkedHashSet<>();
        for (int i=0;i<MAX_PLAYERS;i++) {
            PlayerId playerId = new PlayerId("Pepito, Palo"+i);
            set.add(playerId);
        }
        return set;
    }

    @Override
    public List<PlayerEntity> getPlayersData(Set<PlayerId> playerIdList) throws XPatherException, IOException, URISyntaxException, InterruptedException, ExecutionException {
        log.info(String.format("Populating mock data for %s players",playerIdList.size()));
        List<PlayerEntity> playerDataList = new LinkedList<>();

        for (PlayerId playerId:playerIdList){
            log.info("Adding player " + playerId);
            PlayerEntity playerEntity = new PlayerEntity();
            playerEntity.setPlayerId(playerId);
            playerEntity.setKeepBroker(getRand());
            playerEntity.setLocalMean(getRand());
            playerEntity.setVisitorMean(getRand());
            playerEntity.setVisitorMean(getRand());

            //Get a random team from the repo
            Random r = new Random();
            int team = r.nextInt(18);

            TeamEntity teamEntity = teamRepository.findByName(SMConstants.teamIds[team]);
            playerEntity.setTeam(teamEntity);
            playerDataList.add(playerEntity);

            //Fire up all URL retrieval and value computation in new threads
        }
        return playerDataList;
    }

    private float getRand(){
        return random.nextInt(50)-10;
    }
}
