package org.amupoti.sm.main.services.provider.player;

import org.amupoti.sm.main.repository.entity.PlayerEntity;
import org.amupoti.sm.main.repository.entity.PlayerId;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.XPatherException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Created by Marcel on 28/09/2015.
 */
public class MockPlayerDataService implements PlayerDataService{

    private static final int MAX_PLAYERS = 50;
    private Random random = new Random(System.currentTimeMillis());
    private final static Log log = LogFactory.getLog(MockPlayerDataService.class);
    @Override
    public List<PlayerId> getPlayerIds() throws IOException, XPatherException {
        List<PlayerId> list = new LinkedList<>();
        for (int i=0;i<MAX_PLAYERS;i++) {
            PlayerId playerId = new PlayerId("Pepito, Palo"+i);
            list.add(playerId);
        }
        return list;
    }

    @Override
    public List<PlayerEntity> getPlayersData(List<PlayerId> playerIdList) throws XPatherException, IOException, URISyntaxException, InterruptedException, ExecutionException {
        log.info(String.format("Populating mock data for %s players",playerIdList.size()));
        List<PlayerEntity> playerDataList = new LinkedList<>();

        for (PlayerId playerId:playerIdList){
            log.info("Adding player "+playerId);
            PlayerEntity playerEntity = new PlayerEntity();
            playerEntity.setId(playerId);
            playerEntity.setKeepBroker(getRand());
            playerEntity.setLocalMean(getRand());
            playerEntity.setVisitorMean(getRand());
            playerDataList.add(playerEntity);
            //Fire up all URL retrieval and value computation in new threads
        }
        return playerDataList;
    }

    private float getRand(){
        return random.nextInt(50)-10;
    }
}
