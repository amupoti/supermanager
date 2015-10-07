package org.amupoti.sm.main.services.provider.player;

import org.amupoti.sm.main.repository.TeamRepository;
import org.amupoti.sm.main.repository.entity.PlayerEntity;
import org.amupoti.sm.main.repository.entity.PlayerId;
import org.amupoti.sm.main.repository.entity.TeamEntity;
import org.amupoti.sm.main.services.provider.HTMLProviderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Loads data from www.elrincondelmanager.es so it can be loaded in the local database
 * Data is obtained by retrieveing the full HTML page and performing Xpath transformations
 * Created by Marcel on 17/08/2015.
 */

public class RDMPlayerDataService implements PlayerDataService {

    public static final String VAL_MEDIA_LOCAL = "//*[@id=\"sm_central\"]/div[2]/table/tbody/tr[38]/td[9]/b";
    public static final String VAL_MEDIA_VISITANTE = "//*[@id=\"sm_central\"]/div[2]/table/tbody/tr[39]/td[9]/b";
    public static final String VAL_MANTENER_BROKER = "//*[@id=\"sm_central\"]/div[3]/table/tbody/tr[6]/td[2]";
    public static final String ALL_PLAYERS = "//*[@id=\"sm_central\"]/div/table/tbody/tr/td[1]/a";

    //*[@id="pr"]/div/table/tbody/tr[4]/td[2]/a
    //public static final String ALL_PLAYERS = "//*[@id=\"sm_central\"]/div/table/tbody/tr/td[1]/a";
    public static final String PLAYER_TEAM = "//*[@id=\"sm_central\"]/div[2]/table/tbody/tr[1]/td/b";


    public static final String BASE = "B";
    public static final String ALERO = "A";
    public static final String PIVOT = "P";


    private HtmlCleaner cleaner;

    private static final Log LOG = LogFactory.getLog(RDMPlayerDataService.class);

    @Autowired
    private HTMLProviderService htmlProviderService;

    @Autowired
    private TeamRepository teamRepository;

    @PostConstruct
    public void init() {
        cleaner = new HtmlCleaner();

    }


    /**
     * Returns the list of player Ids from the web
     * @return
     * @throws IOException
     * @throws XPatherException
     */
    public List<PlayerId> getPlayerIds() throws IOException, XPatherException {
        String html = htmlProviderService.getAllPlayersURL();
        TagNode node = cleaner.clean(html);
        Object[] objects = node.evaluateXPath(RDMPlayerDataService.ALL_PLAYERS);
        List<PlayerId> playerIds = new LinkedList<>();
        for (int i = 0; i < objects.length; i++) {
            TagNode tagNode = (TagNode) objects[i];
            String name = tagNode.getAllChildren().get(0).toString();
            if (name.contains(",")) {
                playerIds.add(new PlayerId(name));
            }
        }
        return playerIds;
    }

    public List<PlayerEntity> getPlayersData(List<PlayerId> playerIdList) throws XPatherException, IOException, URISyntaxException, InterruptedException, ExecutionException {
        List<Future<PlayerEntity>> futurePlayerDataList = new LinkedList<>();
        List<PlayerEntity> playerDataList = new LinkedList<>();


        //Fire up all URL retrieval and value computation in new threads
        for (int i=0;i<playerIdList.size();i++){
            LOG.info("Getting data for player "+playerIdList.get(i));
            Future<PlayerEntity> playerData = populatePlayerData(playerIdList.get(i));
            futurePlayerDataList.add(playerData);
            Thread.sleep(100);
        }

        for (int i=0;i<playerIdList.size();i++){
            if (!futurePlayerDataList.get(i).isDone()){
                LOG.info("Player "+i+" not ready, waiting...");
                Thread.sleep(1000);
                i--;
            }
            else{
                playerDataList.add(futurePlayerDataList.get(i).get());
            }
        }
        return playerDataList;
    }

    /**
     * Returns the data available for a player by checking the player page
     *
     * @param playerId
     * @return
     * @throws IOException
     * @throws XPatherException
     */
    @Cacheable("playerData")
    @Async
    private Future<PlayerEntity> populatePlayerData(PlayerId playerId) throws IOException, XPatherException, URISyntaxException {
        PlayerEntity playerEntity = new PlayerEntity();
        String html = htmlProviderService.getPlayerURL(playerId);
        String localMean = getValue(html, RDMPlayerDataService.VAL_MEDIA_LOCAL);
        String visitorMean = getValue(html, RDMPlayerDataService.VAL_MEDIA_VISITANTE);
        String keepBroker = getValue(html, RDMPlayerDataService.VAL_MANTENER_BROKER);

        //Parse team and store in player data
        String team = getValue(html, RDMPlayerDataService.PLAYER_TEAM);
        team=parseTeam(team);
        TeamEntity teamEntity = teamRepository.findByName(team);

        playerEntity.setId(playerId);
        playerEntity.setLocalMean(Float.parseFloat(localMean));
        playerEntity.setVisitorMean(Float.parseFloat(visitorMean));
        playerEntity.setKeepBroker(Float.parseFloat(keepBroker));
        playerEntity.setTeam(teamEntity);
        return new AsyncResult<>(playerEntity);
    }

    private String parseTeam(String team) {
        String[] split = team.split("\\(");
        return split[1].substring(0,3);

    }

    private String getValue(String html, String xPathExpression) {
        try{
            TagNode node = cleaner.clean(html);
            Object[] objects = node.evaluateXPath(xPathExpression);
            String s = ((TagNode) objects[0]).getAllChildren().get(0).toString();
            return s;
        }
        catch (Exception e){
            //TODO: this is a poor way to handle any problem we may have during parsing.
            return "-1";
        }
    }

}
